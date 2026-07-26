package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

/**
 * Bank-aware task preparation engine.  It keeps exact item variants so the UI
 * can point at the potion dose/tool the player actually owns, while matching
 * on canonical identity for stable ownership checks.
 */
class SmartSupplyAdvisor
{
	private final ItemManager itemManager;
	private final SlayerGearAdvisorConfig config;
	private final ConfigManager configManager;

	@Inject
	SmartSupplyAdvisor(ItemManager itemManager, SlayerGearAdvisorConfig config, ConfigManager configManager)
	{
		this.itemManager = itemManager;
		this.config = config;
		this.configManager = configManager;
	}

	SmartSupplyAdvisor(ItemManager itemManager, SlayerGearAdvisorConfig config)
	{
		this(itemManager, config, null);
	}
	List<SupplyRecommendation> recommend(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		int taskAmount,
		Item[] bankItems,
		Item[] packedItems)
	{
		List<SupplyRule> rules = buildRules(profile, strategy);
		int plannedKills = plannedKillCount(taskAmount);
		Map<Integer, OwnedItem> bank = collect(bankItems);
		Map<Integer, OwnedItem> packed = collect(packedItems);
		// Cannon cosmetics are not interchangeable: regular and ornamented parts
		// cannot be mixed. Preserve exact item variants separately from the
		// canonical map used by ordinary supply matching.
		List<OwnedItem> exactBank = collectExact(bankItems);
		List<OwnedItem> exactPacked = collectExact(packedItems);
		List<SupplyRecommendation> recommendations = new ArrayList<>();
		Set<Integer> usedCanonicalIds = new HashSet<>();

		if (isCannon(strategy))
		{
			addCannonSetRecommendations(recommendations, exactBank, exactPacked, usedCanonicalIds);
		}

		for (SupplyRule rule : rules)
		{
			boolean potionEstimateDisabled = !quantityTargetEnabled(config, rule.category);
			int automaticQuantity = potionEstimateDisabled ? 0 : applySupplyLevel(
				rule.category, recommendedQuantity(rule.category, plannedKills));
			int recommendedQuantity = potionEstimateDisabled
				? 0
				: quantityOverride(profile, rule.category, automaticQuantity);
			String quantityUnit = quantityUnit(rule.category);
			int packedQuantity = matchingQuantity(rule, exactPacked, quantityUnit);
			int bankQuantity = matchingQuantity(rule, exactBank, quantityUnit);
			// Resolve inventory/equipment and bank independently. A consumable that is
			// already packed can still have more doses/food available in the bank.
			// Keeping both states prevents the filtered bank row from disappearing after
			// the first withdrawal.
			OwnedItem packedMatch = findBest(rule, packed.values(), usedCanonicalIds);
			OwnedItem bankMatch = findBest(rule, bank.values(), usedCanonicalIds);

			if (packedMatch != null && bankMatch != null)
			{
				usedCanonicalIds.add(packedMatch.canonicalItemId);
				usedCanonicalIds.add(bankMatch.canonicalItemId);
				// Use the bank variant for the recommendation so the filtered-bank widget
				// points at the exact stack/dose that can still be withdrawn.
				recommendations.add(toRecommendation(rule, bankMatch, SupplyStatus.PACKED_BANKED,
					automaticQuantity, recommendedQuantity, packedQuantity, bankQuantity, quantityUnit));
				continue;
			}

			if (packedMatch != null)
			{
				usedCanonicalIds.add(packedMatch.canonicalItemId);
				recommendations.add(toRecommendation(rule, packedMatch, SupplyStatus.PACKED,
					automaticQuantity, recommendedQuantity, packedQuantity, bankQuantity, quantityUnit));
				continue;
			}

			if (bankMatch != null)
			{
				usedCanonicalIds.add(bankMatch.canonicalItemId);
				recommendations.add(toRecommendation(rule, bankMatch, SupplyStatus.BANKED,
					automaticQuantity, recommendedQuantity, packedQuantity, bankQuantity, quantityUnit));
				continue;
			}

			if (rule.required)
			{
				recommendations.add(new SupplyRecommendation(
					0,
					0,
					rule.displayFallback,
					rule.category,
					rule.reason,
					SupplyStatus.MISSING,
					true,
					automaticQuantity,
					recommendedQuantity,
					packedQuantity,
					bankQuantity,
					quantityUnit));
			}
		}
		return recommendations;
	}

	private SupplyRecommendation toRecommendation(
		SupplyRule rule,
		OwnedItem item,
		SupplyStatus status,
		int automaticQuantity,
		int recommendedQuantity,
		int packedQuantity,
		int bankQuantity,
		String quantityUnit)
	{
		return new SupplyRecommendation(
			item.itemId,
			item.canonicalItemId,
			item.name,
			rule.category,
			rule.reason,
			status,
			rule.required,
			automaticQuantity,
			recommendedQuantity,
			packedQuantity,
			bankQuantity,
			quantityUnit);
	}

	List<SupplyRule> buildRules(SlayerTaskProfile profile, GearStrategy strategy)
	{
		List<SupplyRule> rules = new ArrayList<>();
		String key = profile == null ? "" : profile.getKey().toLowerCase(Locale.ENGLISH);
		boolean ancientAoe = strategy != null && strategy.isAncientAoe();
		boolean venator = strategy != null && isVenator(strategy);

		// These are useful owned trip accelerators across Slayer methods, not only
		// Ancient AoE and Venator. They remain optional and therefore appear only
		// when the account owns them.
		if (config.useGoading())
		{
			rules.add(rule("Goading",
				ancientAoe || venator
					? "Keeps multi-target groups aggressive and close together"
					: "Keeps Slayer targets aggressive during the trip",
				false, "Goading potion", "goading potion"));
		}
		if (config.usePrayerRegen())
		{
			rules.add(rule("Prayer regen", "Passive Prayer sustain during longer Slayer trips", false,
				"Prayer regeneration potion", "prayer regeneration potion"));
		}

		if (ancientAoe)
		{
			rules.add(rule("Magic boost", "Boosts Magic damage or preserves spell access", false,
				"Magic boost", "saturated heart", "imbued heart", "forgotten brew", "ancient brew", "magic potion"));
			rules.add(rule("Rune pouch", "Compact Ancient Magicks rune storage", false,
				"Rune pouch", "divine rune pouch", "rune pouch"));
		}
		else if (strategy != null && strategy.getCombatStyle() == CombatStyle.MELEE)
		{
			rules.add(config.preferDivineBoosts()
				? rule("Combat boost", "Improves melee task speed", false,
					"Combat potion", "divine super combat potion", "divine combat potion",
					"super combat potion", "combat potion")
				: rule("Combat boost", "Improves melee task speed", false,
					"Combat potion", "super combat potion", "combat potion",
					"divine super combat potion", "divine combat potion"));
		}
		else if (strategy != null && strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			rules.add(config.preferDivineBoosts()
				? rule("Ranged boost", "Improves ranged task speed", false,
					"Bastion / ranging potion", "divine bastion potion", "bastion potion",
					"divine ranging potion", "ranging potion")
				: rule("Ranged boost", "Improves ranged task speed", false,
					"Bastion / ranging potion", "bastion potion", "ranging potion",
					"divine bastion potion", "divine ranging potion"));
		}
		else if (strategy != null && strategy.getCombatStyle() == CombatStyle.MAGIC)
		{
			rules.add(rule("Magic boost", "Improves Magic task speed", false,
				"Magic boost", "saturated heart", "imbued heart", "forgotten brew", "ancient brew", "magic potion"));
		}

		if (contains(key, "araxytes"))
		{
			rules.add(0, rule("Venom protection", "Araxytes can inflict venom", true,
				"Anti-venom", "extended anti-venom+", "anti-venom+", "anti-venom"));
		}
		if (contains(key, "gargoyles"))
		{
			rules.add(rule("Finisher", "A rock hammer is needed to finish gargoyles (including with auto-smash)", true,
				"Rock hammer", "rock hammer", "rock thrownhammer", "granite hammer"));
		}
		if (contains(key, "mutated-zygomites"))
		{
			rules.add(0, rule("Finisher", "Fungicide is used to finish zygomites", true,
				"Fungicide spray", "fungicide spray"));
		}
		if (contains(key, "lizards"))
		{
			rules.add(rule("Finisher", "Ice coolers finish desert lizards", true,
				"Ice cooler", "ice cooler"));
		}
		if (contains(key, "rockslugs"))
		{
			rules.add(rule("Finisher", "Salt is used to finish rockslugs", true,
				"Bag of salt", "bag of salt"));
		}
		if (contains(key, "harpie"))
		{
			rules.add(0, rule("Task tool", "A lit bug lantern is required to damage harpie bug swarms", true,
				"Lit bug lantern", "lit bug lantern"));
		}
		if (contains(key, "mogres"))
		{
			rules.add(rule("Task tool", "Fishing explosives lure mogres out of the water", true,
				"Fishing explosive", "fishing explosive"));
		}
		if (contains(key, "molanisks"))
		{
			rules.add(0, rule("Task tool", "A Slayer bell dislodges Molanisks before combat", true,
				"Slayer bell", "slayer bell"));
		}
		if (isCannon(strategy))
		{
			rules.add(rule("Cannon ammo", "A cannon method needs ammunition before leaving the bank", true,
				"Cannonballs", "granite cannonball", "steel cannonball", "cannonball"));
		}
		if (contains(key, "blue-dragons", "black-dragons", "red-dragons", "metal-dragons", "frost-dragons"))
		{
			rules.add(rule("Antifire", "Dragonfire protection is required unless the selected off-hand provides it", true,
				"Antifire potion / dragonfire shield", "extended super antifire potion", "super antifire potion", "extended antifire", "antifire potion"));
		}


		if (config.lowRiskMode())
		{
			rules.add(rule("Escape", "Low-risk mode: keep a fast escape option packed", false,
				"Emergency teleport", "royal seed pod", "amulet of glory", "ring of wealth", "teleport to house"));
		}

		// Helpful sustain, but only surface it when the user actually owns it.
		if (strategy != null)
		{
			rules.add(rule("Prayer", "Useful sustain for protection or offensive prayers", false,
				"Prayer potion / restore", "prayer potion", "super restore", "sanfew serum"));
		}

		// General trip prep is deliberately optional: it enriches the sidebar when
		// the account owns these supplies without blocking Ready-to-leave-bank.
		rules.add(rule("Food", "Emergency healing for the trip", false,
			"Food", "anglerfish", "manta ray", "dark crab", "shark", "cooked karambwan", "sea turtle", "monkfish"));
		rules.add(rule("Run energy", "Optional travel and repositioning sustain", false,
			"Stamina potion", "stamina potion", "super energy potion", "energy potion"));
		return rules;
	}

	private static void addCannonSetRecommendations(
		List<SupplyRecommendation> recommendations,
		Iterable<OwnedItem> bank,
		Iterable<OwnedItem> packed,
		Set<Integer> usedCanonicalIds)
	{
		String[] parts = {"cannon base", "cannon stand", "cannon barrels", "cannon furnace"};
		int regularOwned = cannonPartsOwned(parts, false, bank, packed);
		int ornamentOwned = cannonPartsOwned(parts, true, bank, packed);
		boolean ornamented = ornamentOwned > regularOwned;

		for (String part : parts)
		{
			String expected = ornamented ? part + " (or)" : part;
			OwnedItem packedMatch = findExact(expected, packed);
			OwnedItem bankMatch = findExact(expected, bank);
			SupplyStatus status = resolveStatus(packedMatch != null, bankMatch != null);
			OwnedItem display = bankMatch != null ? bankMatch : packedMatch;
			if (display != null)
			{
				usedCanonicalIds.add(display.canonicalItemId);
				recommendations.add(new SupplyRecommendation(
					display.itemId,
					display.canonicalItemId,
					display.name,
					"Cannon setup",
					"Required part of the Dwarf multicannon for the selected cannon method",
					status,
					true));
			}
			else
			{
				recommendations.add(new SupplyRecommendation(
					0,
					0,
					formatCannonPart(expected),
					"Cannon setup",
					"Required part of the Dwarf multicannon for the selected cannon method",
					SupplyStatus.MISSING,
					true));
			}
		}
	}

	private static int cannonPartsOwned(String[] parts, boolean ornamented, Iterable<OwnedItem> bank, Iterable<OwnedItem> packed)
	{
		int count = 0;
		for (String part : parts)
		{
			String expected = ornamented ? part + " (or)" : part;
			if (findExact(expected, packed) != null || findExact(expected, bank) != null) count++;
		}
		return count;
	}

	private static OwnedItem findExact(String expectedName, Iterable<OwnedItem> items)
	{
		String expected = NameMatcher.normalize(expectedName);
		for (OwnedItem item : items)
		{
			if (expected.equals(NameMatcher.normalize(item.name))) return item;
		}
		return null;
	}

	private static String formatCannonPart(String normalized)
	{
		if (normalized == null || normalized.isEmpty()) return "Cannon part";
		String[] words = normalized.split(" ");
		StringBuilder result = new StringBuilder();
		for (String word : words)
		{
			if (word.isEmpty()) continue;
			if (result.length() > 0) result.append(' ');
			if ("(or)".equals(word)) result.append(word);
			else result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return result.toString();
	}

	private List<OwnedItem> collectExact(Item[] items)
	{
		List<OwnedItem> result = new ArrayList<>();
		if (items == null) return result;
		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0) continue;
			ItemComposition composition = itemManager.getItemComposition(item.getId());
			if (composition == null || composition.getPlaceholderTemplateId() != -1 || invalidName(composition.getName())) continue;
			result.add(new OwnedItem(
				item.getId(), itemManager.canonicalize(item.getId()), composition.getName(), item.getQuantity()));
		}
		return result;
	}

	private Map<Integer, OwnedItem> collect(Item[] items)
	{
		Map<Integer, OwnedItem> result = new HashMap<>();
		if (items == null) return result;
		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0) continue;
			ItemComposition composition = itemManager.getItemComposition(item.getId());
			if (composition == null || composition.getPlaceholderTemplateId() != -1 || invalidName(composition.getName())) continue;
			int canonical = itemManager.canonicalize(item.getId());
			OwnedItem candidate = new OwnedItem(item.getId(), canonical, composition.getName(), item.getQuantity());
			OwnedItem existing = result.get(canonical);
			if (existing == null || doseScore(candidate.name) > doseScore(existing.name))
			{
				if (existing != null)
				{
					candidate = new OwnedItem(candidate.itemId, candidate.canonicalItemId,
						candidate.name, candidate.quantity + existing.quantity);
				}
				result.put(canonical, candidate);
			}
			else
			{
				result.put(canonical, new OwnedItem(existing.itemId, existing.canonicalItemId,
					existing.name, existing.quantity + candidate.quantity));
			}
		}
		return result;
	}

	private static OwnedItem findBest(SupplyRule rule, Iterable<OwnedItem> items, Set<Integer> used)
	{
		for (String preferred : rule.preferredNames)
		{
			OwnedItem best = null;
			for (OwnedItem item : items)
			{
				if (used.contains(item.canonicalItemId)) continue;
				String normalizedName = NameMatcher.normalize(item.name);
				if ("Food".equals(rule.category) && isUnsafeFoodName(normalizedName)) continue;
				if (matchesPreferredSupply(normalizedName, preferred))
				{
					if (best == null || doseScore(item.name) > doseScore(best.name)) best = item;
				}
			}
			if (best != null) return best;
		}
		return null;
	}

	static boolean matchesPreferredSupply(String normalizedName, String preferred)
	{
		if (normalizedName == null || preferred == null) return false;
		if (!preferred.startsWith("divine ")
			&& normalizedName.startsWith("divine ")
			&& normalizedName.contains(preferred))
		{
			return false;
		}
		return normalizedName.contains(preferred);
	}

	static boolean isUnsafeFoodName(String normalizedName)
	{
		return normalizedName != null
			&& (normalizedName.startsWith("raw ") || normalizedName.startsWith("burnt "));
	}

	static SupplyStatus resolveStatus(boolean packed, boolean banked)
	{
		if (packed && banked) return SupplyStatus.PACKED_BANKED;
		if (packed) return SupplyStatus.PACKED;
		if (banked) return SupplyStatus.BANKED;
		return SupplyStatus.MISSING;
	}

	static boolean isVenator(GearStrategy strategy)
	{
		return strategy != null && NameMatcher.normalize(strategy.getName()).contains("venator");
	}

	static boolean isCannon(GearStrategy strategy)
	{
		return strategy != null && NameMatcher.normalize(strategy.getName()).contains("cannon");
	}

	static int doseScore(String name)
	{
		if (name == null) return 0;
		for (int dose = 6; dose >= 1; dose--)
		{
			if (name.endsWith("(" + dose + ")")) return dose;
		}
		return 0;
	}

	static int recommendedQuantity(String category, int taskAmount)
	{
		if (taskAmount <= 0) return 0;
		switch (category)
		{
			case "Cannon ammo":
				return Math.max(100, taskAmount * 8);
			case "Food":
				return clamp(2, 12, (taskAmount + 19) / 20);
			case "Prayer":
				return clamp(4, 12, ((taskAmount + 59) / 60) * 4);
			case "Antifire":
			case "Venom protection":
				return clamp(4, 16, ((taskAmount + 39) / 40) * 4);
			case "Combat boost":
			case "Ranged boost":
			case "Prayer regen":
			case "Goading":
				return clamp(4, 12, ((taskAmount + 49) / 50) * 4);
			case "Run energy":
				return clamp(4, 8, ((taskAmount + 79) / 80) * 4);
			case "Magic boost":
				// This category can resolve to reusable hearts as well as potions.
				// Keep it presence-based until the selected item is modeled separately.
				return 0;
			default:
				return 0;
		}
	}

	private int plannedKillCount(int remainingTask)
	{
		return plannedKillCount(
			config.tripPlan(), remainingTask, config.customTripKills());
	}

	static int plannedKillCount(TripPlan plan, int remainingTask, int customKills)
	{
		TripPlan effective = plan == null ? TripPlan.FULL_ASSIGNMENT : plan;
		int remaining = Math.max(0, remainingTask);
		switch (effective)
		{
			case SHORT_TRIP:
				return remaining > 0 ? Math.min(remaining, 40) : 40;
			case CUSTOM_KILLS:
				int custom = Math.max(1, customKills);
				return remaining > 0 ? Math.min(remaining, custom) : custom;
			case FULL_ASSIGNMENT:
			default:
				return remaining;
		}
	}

	private int applySupplyLevel(String category, int automaticQuantity)
	{
		if (automaticQuantity <= 0) return automaticQuantity;
		SupplyLevel level = "Food".equals(category)
			? config.foodSafety()
			: ("Prayer".equals(category) || "Prayer regen".equals(category))
				? config.prayerSafety()
				: SupplyLevel.NORMAL;
		return applySupplyLevel(automaticQuantity, level, "Food".equals(category) ? 1 : 4);
	}

	static int applySupplyLevel(int automaticQuantity, SupplyLevel level, int unitSize)
	{
		if (automaticQuantity <= 0) return 0;
		int unit = Math.max(1, unitSize);
		double multiplier = level == null ? 1.0 : level.getMultiplier();
		int scaled = (int) Math.ceil(automaticQuantity * multiplier);
		return Math.max(unit, ((scaled + unit - 1) / unit) * unit);
	}

	private int quantityOverride(SlayerTaskProfile profile, String category, int automaticQuantity)
	{
		if (configManager == null || profile == null) return automaticQuantity;
		String value = configManager.getRSProfileConfiguration(
			SlayerGearAdvisorConfig.GROUP, quantityOverrideKey(profile.getKey(), category));
		if (value == null || value.trim().isEmpty()) return automaticQuantity;
		try
		{
			return Math.max(0, Integer.parseInt(value.trim()));
		}
		catch (NumberFormatException ignored)
		{
			return automaticQuantity;
		}
	}

	static String quantityOverrideKey(String taskKey, String category)
	{
		String task = NameMatcher.normalize(taskKey).replace(' ', '-');
		String supply = NameMatcher.normalize(category).replace(' ', '-');
		return "supply." + task + "." + supply;
	}

	static String quantityUnit(String category)
	{
		switch (category)
		{
			case "Prayer":
			case "Antifire":
			case "Venom protection":
			case "Combat boost":
			case "Ranged boost":
			case "Magic boost":
			case "Prayer regen":
			case "Run energy":
			case "Goading":
				return "doses";
			case "Cannon ammo":
				return "shots";
			default:
				return "items";
		}
	}

	static boolean isPotionQuantityCategory(String category)
	{
		return "doses".equals(quantityUnit(category));
	}

	static boolean quantityTargetEnabled(SlayerGearAdvisorConfig config, String category)
	{
		return config == null
			|| config.potionEstimatesEnabled()
			|| !isPotionQuantityCategory(category);
	}

	private static int matchingQuantity(SupplyRule rule, Iterable<OwnedItem> items, String unit)
	{
		int total = 0;
		for (OwnedItem item : items)
		{
			String normalizedName = NameMatcher.normalize(item.name);
			if ("Food".equals(rule.category) && isUnsafeFoodName(normalizedName)) continue;
			boolean matches = false;
			for (String preferred : rule.preferredNames)
			{
				if (normalizedName.contains(preferred))
				{
					matches = true;
					break;
				}
			}
			if (!matches) continue;
			if ("doses".equals(unit))
			{
				int doses = doseScore(item.name);
				total += item.quantity * Math.max(1, doses);
			}
			else
			{
				total += item.quantity;
			}
		}
		return total;
	}

	private static int clamp(int minimum, int maximum, int value)
	{
		return Math.max(minimum, Math.min(maximum, value));
	}

	private static SupplyRule rule(String category, String reason, boolean required, String fallback, String... preferred)
	{
		List<String> normalized = new ArrayList<>();
		for (String value : preferred) normalized.add(NameMatcher.normalize(value));
		return new SupplyRule(category, reason, required, fallback, normalized);
	}

	private static boolean contains(String key, String... values)
	{
		return Arrays.stream(values).anyMatch(key::contains);
	}

	private static boolean invalidName(String value)
	{
		return value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value);
	}

	static final class SupplyRule
	{
		private final String category;
		private final String reason;
		private final boolean required;
		private final String displayFallback;
		private final List<String> preferredNames;

		private SupplyRule(String category, String reason, boolean required, String displayFallback, List<String> preferredNames)
		{
			this.category = category;
			this.reason = reason;
			this.required = required;
			this.displayFallback = displayFallback;
			this.preferredNames = preferredNames;
		}

		String getCategory() { return category; }
		List<String> getPreferredNames() { return preferredNames; }
	}

	private static final class OwnedItem
	{
		private final int itemId;
		private final int canonicalItemId;
		private final String name;
		private final int quantity;

		private OwnedItem(int itemId, int canonicalItemId, String name, int quantity)
		{
			this.itemId = itemId;
			this.canonicalItemId = canonicalItemId;
			this.name = name;
			this.quantity = quantity;
		}
	}
}
