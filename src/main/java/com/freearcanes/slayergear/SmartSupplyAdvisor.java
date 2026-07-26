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

	@Inject
	SmartSupplyAdvisor(ItemManager itemManager, SlayerGearAdvisorConfig config)
	{
		this.itemManager = itemManager;
		this.config = config;
	}

	List<SupplyRecommendation> recommend(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		Item[] bankItems,
		Item[] packedItems)
	{
		List<SupplyRule> rules = buildRules(profile, strategy);
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
				recommendations.add(toRecommendation(rule, bankMatch, SupplyStatus.PACKED_BANKED));
				continue;
			}

			if (packedMatch != null)
			{
				usedCanonicalIds.add(packedMatch.canonicalItemId);
				recommendations.add(toRecommendation(rule, packedMatch, SupplyStatus.PACKED));
				continue;
			}

			if (bankMatch != null)
			{
				usedCanonicalIds.add(bankMatch.canonicalItemId);
				recommendations.add(toRecommendation(rule, bankMatch, SupplyStatus.BANKED));
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
					true));
			}
		}
		return recommendations;
	}

	private SupplyRecommendation toRecommendation(SupplyRule rule, OwnedItem item, SupplyStatus status)
	{
		return new SupplyRecommendation(
			item.itemId,
			item.canonicalItemId,
			item.name,
			rule.category,
			rule.reason,
			status,
			rule.required);
	}

	private List<SupplyRule> buildRules(SlayerTaskProfile profile, GearStrategy strategy)
	{
		List<SupplyRule> rules = new ArrayList<>();
		String key = profile == null ? "" : profile.getKey().toLowerCase(Locale.ENGLISH);

		if (strategy != null && strategy.isAncientAoe())
		{
			rules.add(rule("Goading", "Keeps targets aggressive so stacks stay together", false,
				"Goading potion", "goading potion"));
			rules.add(rule("Prayer regen", "Passive Prayer sustain during long multi-target trips", false,
				"Prayer regeneration potion", "prayer regeneration potion"));
			rules.add(rule("Prayer", "Protection and offensive prayer sustain", false,
				"Prayer potion / restore", "prayer potion", "super restore", "sanfew serum"));
			rules.add(rule("Magic boost", "Boosts Magic damage or preserves spell access", false,
				"Magic boost", "saturated heart", "imbued heart", "forgotten brew", "ancient brew", "magic potion"));
			rules.add(rule("Rune pouch", "Compact Ancient Magicks rune storage", false,
				"Rune pouch", "divine rune pouch", "rune pouch"));
		}
		else if (strategy != null && isVenator(strategy))
		{
			rules.add(rule("Goading", "Keeps targets aggressive so Venator chains keep bouncing", false,
				"Goading potion", "goading potion"));
			rules.add(rule("Prayer regen", "Passive Prayer sustain during long multi-target trips", false,
				"Prayer regeneration potion", "prayer regeneration potion"));
			rules.add(rule("Ranged boost", "Improves ranged accuracy and damage", false,
				"Ranging potion", "divine ranging potion", "ranging potion", "bastion potion"));
			rules.add(rule("Prayer", "Protection and offensive prayer sustain", false,
				"Prayer potion / restore", "prayer potion", "super restore", "sanfew serum"));
		}
		else if (strategy != null && strategy.getCombatStyle() == CombatStyle.MELEE)
		{
			rules.add(rule("Combat boost", "Improves melee task speed", false,
				"Combat potion", "divine super combat potion", "super combat potion", "divine combat potion", "combat potion"));
		}
		else if (strategy != null && strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			rules.add(rule("Ranged boost", "Improves ranged task speed", false,
				"Ranging potion", "divine ranging potion", "ranging potion", "bastion potion"));
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
		if (strategy != null && !strategy.isAncientAoe() && !isVenator(strategy))
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
			result.add(new OwnedItem(item.getId(), itemManager.canonicalize(item.getId()), composition.getName()));
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
			OwnedItem candidate = new OwnedItem(item.getId(), canonical, composition.getName());
			OwnedItem existing = result.get(canonical);
			if (existing == null || doseScore(candidate.name) > doseScore(existing.name))
			{
				result.put(canonical, candidate);
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
				if (normalizedName.contains(preferred))
				{
					if (best == null || doseScore(item.name) > doseScore(best.name)) best = item;
				}
			}
			if (best != null) return best;
		}
		return null;
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

	private static final class SupplyRule
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
	}

	private static final class OwnedItem
	{
		private final int itemId;
		private final int canonicalItemId;
		private final String name;

		private OwnedItem(int itemId, int canonicalItemId, String name)
		{
			this.itemId = itemId;
			this.canonicalItemId = canonicalItemId;
			this.name = name;
		}
	}
}