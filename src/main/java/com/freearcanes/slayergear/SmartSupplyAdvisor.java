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
		return recommend(
			profile,
			strategy,
			null,
			taskAmount,
			bankItems,
			packedItems,
			false,
			null);
	}

	List<SupplyRecommendation> recommend(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		String assignedLocation,
		int taskAmount,
		Item[] bankItems,
		Item[] packedItems)
	{
		return recommend(
			profile,
			strategy,
			assignedLocation,
			taskAmount,
			bankItems,
			packedItems,
			false,
			null);
	}

	List<SupplyRecommendation> recommend(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		String assignedLocation,
		int taskAmount,
		Item[] bankItems,
		Item[] packedItems,
		boolean allowRadasBlessing)
	{
		return recommend(profile, strategy, assignedLocation, taskAmount,
			bankItems, packedItems, allowRadasBlessing, null);
	}

	List<SupplyRecommendation> recommend(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		String assignedLocation,
		int taskAmount,
		Item[] bankItems,
		Item[] packedItems,
		boolean allowRadasBlessing,
		GearPriority objective)
	{
		return recommend(profile, strategy, assignedLocation, taskAmount,
			bankItems, packedItems, allowRadasBlessing, objective, null);
	}

	List<SupplyRecommendation> recommend(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		String assignedLocation,
		int taskAmount,
		Item[] bankItems,
		Item[] packedItems,
		boolean allowRadasBlessing,
		GearPriority objective,
		PotionEstimationContext potionContext)
	{
		String encounterName = potionContext == null ? "" : potionContext.getTaskName();
		List<SupplyRule> rules = buildRules(
			profile, strategy, assignedLocation, encounterName);
		boolean wildernessTask = isWildernessTask(assignedLocation, strategy);
		int plannedKills = plannedKillCount(taskAmount);
		OwnedItems bank = collectOwnedItems(bankItems);
		OwnedItems packed = collectOwnedItems(packedItems);
		// Cannon cosmetics are not interchangeable: regular and ornamented parts
		// cannot be mixed. Preserve exact item variants separately from the
		// canonical map used by ordinary supply matching.
		List<SupplyRecommendation> recommendations = new ArrayList<>();
		Set<Integer> usedCanonicalIds = new HashSet<>();

		if (isCannon(strategy))
		{
			addCannonSetRecommendations(
				recommendations, bank.exact, packed.exact, usedCanonicalIds);
		}

		for (SupplyRule rule : rules)
		{
			boolean potionEstimateDisabled = !quantityTargetEnabled(config, rule.category);
			int estimatedQuantity = potionEstimateDisabled ? 0 : PotionDoseEstimator.estimate(
				rule.category, plannedKills, profile, strategy, potionContext, config);
			int objectiveQuantity = potionEstimateDisabled ? 0 : applyObjectiveQuantity(
				rule.category, estimatedQuantity, objective);
			int automaticQuantity = potionEstimateDisabled ? 0 : applySupplyLevel(
				rule.category, objectiveQuantity);
			int recommendedQuantity = potionEstimateDisabled
				? 0
				: quantityOverride(profile, rule.category, automaticQuantity);
			String quantityUnit = quantityUnit(rule.category);
			int packedQuantity = matchingQuantity(
				rule, packed.exact, quantityUnit, wildernessTask, allowRadasBlessing);
			int bankQuantity = matchingQuantity(
				rule, bank.exact, quantityUnit, wildernessTask, allowRadasBlessing);
			// Resolve inventory/equipment and bank independently. A consumable that is
			// already packed can still have more doses/food available in the bank.
			// Keeping both states prevents the filtered bank row from disappearing after
			// the first withdrawal.
			OwnedItem packedMatch = findBest(
				rule, packed.byCanonical.values(), usedCanonicalIds, wildernessTask, allowRadasBlessing);
			OwnedItem bankMatch = findBest(
				rule, bank.byCanonical.values(), usedCanonicalIds, wildernessTask, allowRadasBlessing);

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

			if (rule.required || rule.showWhenMissing)
			{
				recommendations.add(new SupplyRecommendation(
					0,
					0,
					rule.displayFallback,
					rule.category,
					rule.reason,
					SupplyStatus.MISSING,
					rule.required,
					automaticQuantity,
					recommendedQuantity,
					packedQuantity,
					bankQuantity,
					quantityUnit));
			}
		}
		if (config.bossWeaponSwitches())
		{
			// The owned Araxyte weapon is already rendered in the dedicated boss-
			// switch section. Retain only a missing required preference here; ammo
			// and the optional Ranged boost remain normal trip supplies.
			recommendations.removeIf(recommendation ->
				"Araxyte switch".equals(recommendation.getCategory())
					&& recommendation.getStatus() != SupplyStatus.MISSING);
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
		return buildRules(profile, strategy, null);
	}

	List<SupplyRule> buildRules(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		String assignedLocation)
	{
		return buildRules(profile, strategy, assignedLocation, "");
	}

	List<SupplyRule> buildRules(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		String assignedLocation,
		String encounterName)
	{
		List<SupplyRule> rules = new ArrayList<>();
		String profileKey = profile == null
			? "" : profile.getKey().toLowerCase(Locale.ENGLISH);
		String encounterKey = NameMatcher.normalize(encounterName);
		String key = profileKey + " " + encounterKey;
		String location = NameMatcher.normalize(assignedLocation == null || assignedLocation.trim().isEmpty()
			? strategy == null ? "" : strategy.getLocation()
			: assignedLocation);
		boolean ancientAoe = strategy != null && strategy.isAncientAoe();
		boolean venator = strategy != null && isVenator(strategy);
		boolean wildernessTask = isWildernessTask(assignedLocation, strategy);
		boolean turaelAyaSpeed = TuraelSpeedProfiles.isSpeedStrategy(strategy);
		boolean bossPvm = key.contains("boss")
			|| BossSlayerCatalog.contains(encounterName)
			|| (profile != null && BossSlayerCatalog.contains(profile.getDisplayName()));

		if (bossPvm && config.useBossThralls())
		{
			rules.add(rule("Thrall book",
				"Required in the inventory or off-hand to cast Arceuus resurrection spells",
				true, "Book of the dead", "book of the dead"));
			rules.add(rule("Thrall runes",
				"Carries the selected Greater Thrall runes; verify the pouch contents before leaving",
				true, "Rune pouch", "divine rune pouch", "rune pouch"));
		}

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
		if (turaelAyaSpeed)
		{
			rules.add(suggestedRule("Slayer bracelet",
				"An Expeditious bracelet shortens Turael/Aya assignments and speeds streak progression",
				"Expeditious bracelet", "expeditious bracelet"));
		}
		else if (config.useSlayerBracelet())
		{
			SlayerBraceletPreference preference = config.slayerBraceletPreference();
			if (preference == SlayerBraceletPreference.SLAUGHTER
				|| preference == SlayerBraceletPreference.BOTH)
			{
				rules.add(suggestedRule("Slayer bracelet",
					"A Bracelet of slaughter can extend the assignment and is packed as a glove switch",
					"Bracelet of slaughter", "bracelet of slaughter"));
			}
			if (preference == SlayerBraceletPreference.EXPEDITIOUS
				|| preference == SlayerBraceletPreference.BOTH)
			{
				rules.add(suggestedRule("Slayer bracelet",
					"An Expeditious bracelet can shorten the assignment and is packed as a glove switch",
					"Expeditious bracelet", "expeditious bracelet"));
			}
		}

		addPrayerRemainsTools(rules, key, config.prayerRemainsPreference());

		if (ancientAoe)
		{
			rules.add(suggestedRule("Magic boost", "Boosts Magic damage or preserves spell access",
				"Magic boost", "saturated heart", "imbued heart", "forgotten brew", "ancient brew", "magic potion"));
			rules.add(suggestedRule("Rune pouch", "Compact Ancient Magicks rune storage; verify the required runes are loaded",
				"Rune pouch", "divine rune pouch", "rune pouch"));
		}
		if (!ancientAoe && usesCombatStyle(strategy, CombatStyle.MELEE))
		{
			rules.add(config.preferDivineBoosts()
				? suggestedRule("Combat boost", "Improves melee task speed",
					"Combat potion(4)", "divine super combat potion", "divine combat potion",
					"super combat potion", "combat potion")
				: suggestedRule("Combat boost", "Improves melee task speed",
					"Combat potion(4)", "super combat potion", "combat potion",
					"divine super combat potion", "divine combat potion"));
		}
		if (!ancientAoe && usesCombatStyle(strategy, CombatStyle.RANGED))
		{
			rules.add(config.preferDivineBoosts()
				? suggestedRule("Ranged boost", "Improves ranged task speed",
					"Ranging potion(4)", "divine bastion potion", "bastion potion",
					"divine ranging potion", "ranging potion")
				: suggestedRule("Ranged boost", "Improves ranged task speed",
					"Ranging potion(4)", "bastion potion", "ranging potion",
					"divine bastion potion", "divine ranging potion"));
		}
		if (!ancientAoe && usesCombatStyle(strategy, CombatStyle.MAGIC))
		{
			rules.add(suggestedRule("Magic boost", "Improves Magic task speed",
				"Magic boost", "saturated heart", "imbued heart", "forgotten brew", "ancient brew", "magic potion"));
		}

		if (contains(key, "araxytes", "araxxor", "zulrah", "vorkath"))
		{
			String venomReason = contains(key, "vorkath")
				? "Vorkath's venomous dragonfire can inflict venom"
				: contains(key, "zulrah")
					? "Zulrah and its snakelings can inflict venom"
					: "Araxytes can inflict venom";
			rules.add(0, rule("Venom protection", venomReason, true,
				"Anti-venom(4)", "extended anti-venom+", "anti-venom+", "anti-venom"));
		}

		if (contains(key, "araxxor") && strategy != null
			&& (NameMatcher.normalize(strategy.getName()).contains("crush melee")
				|| NameMatcher.normalize(strategy.getName()).contains("heavy ballista switch")))
		{
			AraxxorSwitchPreference preference = config.araxxorSwitchPreference();
			if (preference == AraxxorSwitchPreference.HEAVY_BALLISTA
				|| NameMatcher.normalize(strategy.getName()).contains("heavy ballista switch"))
			{
				rules.add(rule("Araxyte switch",
					"Chosen safe one-hit Mirrorback/Araxyte weapon; verify your Ranged max hit",
					true, "Heavy ballista", "heavy ballista"));
				rules.add(rule("Araxyte ammunition",
					"Heavy ballista requires dragon javelins for the intended one-hit setup",
					true, "Dragon javelin", "dragon javelin"));
				rules.add(suggestedRule("Araxyte ranged boost",
					"The one-hit threshold may require a Ranged potion, Rigour, or additional Ranged switches",
					"Ranging potion(4)", "divine ranging potion", "divine bastion potion",
					"ranging potion", "bastion potion"));
			}
			else
			{
				boolean required = preference == AraxxorSwitchPreference.NOXIOUS_HALBERD;
				rules.add((required ? rule("Araxyte switch",
					"Chosen safe hatched-araxyte and Mirrorback switch", true,
					"Noxious halberd", "noxious halberd") : suggestedRule("Araxyte switch",
					"Noxious halberd is a safe hatched-araxyte and mirrorback switch, not the default main weapon",
					"Noxious halberd", "noxious halberd")));
			}
		}
		if (contains(key, "kalphite", "cave-crawlers", "cave-slimes", "lizardmen",
			"king black dragon", "k'ril tsutsaroth", "kril tsutsaroth", "hydra"))
		{
			rules.add(0, suggestedRule("Poison protection",
				poisonReason(key),
				"Antipoison(4)", "antidote++", "antidote+", "superantipoison",
				"sanfew serum", "antipoison", "extended anti-venom+",
				"anti-venom+", "anti-venom"));
		}
		if (location.contains("lumbridge swamp cave")
			|| contains(key, "cave-horrors")
			|| (turaelAyaSpeed && contains(key, "cave-bugs", "cave-slimes")))
		{
			rules.add(0, suggestedRule("Light source", "A stable enclosed light source is recommended for this cave",
				"Bullseye lantern", "bullseye lantern", "emerald lantern", "sapphire lantern",
				"oil lantern", "candle lantern"));
		}
		if (location.contains("lumbridge swamp cave"))
		{
			rules.add(suggestedRule("Cave access", "Needed for the surface entrance until permanent access is established",
				"Rope", "rope"));
			rules.add(suggestedRule("Light backup", "Relights an extinguished cave lantern",
				"Tinderbox", "tinderbox"));
		}
		if (location.contains("kalphite lair"))
		{
			rules.add(suggestedRule("Cave access", "Needed until both Kalphite Lair ropes are permanently installed",
				"Rope", "rope"));
		}
		if (contains(key, "brine-rats"))
		{
			rules.add(rule("Cave access", "A spade is required every time you enter the Brine Rat Cavern", true,
				"Spade", "spade"));
		}
		addEncounterItems(rules, profileKey, encounterKey, strategy);
		if (contains(key, "lizards") || location.contains("kharidian desert"))
		{
			rules.add(suggestedRule("Desert hydration", "Protection from desert heat while travelling and fighting",
				"Waterskin(4)", "waterskin"));
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
		if (contains(key, "grotesque guardians"))
		{
			rules.add(rule("Finisher",
				"A rock hammer, rock thrownhammer, or granite hammer is required to finish the Guardians",
				true, "Rock hammer", "rock hammer", "rock thrownhammer", "granite hammer"));
		}
		if (contains(key, "warped-creatures"))
		{
			rules.add(0, rule("Task tool",
				"A Crystal chime is required to damage warped terrorbirds and warped tortoises",
				true, "Crystal chime", "crystal chime"));
		}
		if (isCannon(strategy))
		{
			rules.add(rule("Cannon ammo", "A cannon method needs ammunition before leaving the bank", true,
				"Cannonballs", "granite cannonball", "steel cannonball", "cannonball"));
		}
		if (contains(key, "blue-dragons", "black-dragons", "green-dragons", "red-dragons",
			"metal-dragons", "frost-dragons", "lava-dragons", "dragon-boss"))
		{
			boolean advancedDragonfire = contains(key, "vorkath", "king black dragon");
			rules.add(rule("Antifire", advancedDragonfire
					? "Boss dragonfire needs layered protection; use super antifire or combine antifire with a protective shield"
					: "Dragonfire protection is required unless the selected off-hand provides it",
				true, "Antifire potion(4)", "extended super antifire potion", "super antifire potion",
				"extended antifire", "antifire potion"));
		}

		for (TravelItemAdvisor.TravelRule travel
			: TravelItemAdvisor.recommend(assignedLocation, strategy, config))
		{
			rules.add(suggestedRule(
				"Travel",
				travel.getReason(),
				travel.getFallback(),
				travel.getPreferredNames()));
		}

		if (config.lowRiskMode())
		{
			rules.add(suggestedRule("Escape", "Low-risk mode: keep a fast escape option packed",
				"Emergency teleport", "royal seed pod", "amulet of glory", "ring of wealth", "teleport to house"));
		}

		// Core trip preparation is visible for every task, even when the account
		// does not currently own a matching item. These remain non-blocking.
		if (strategy != null)
		{
			PrayerRestorePreference preference = config.prayerRestorePreference();
			if (preference == PrayerRestorePreference.SUPER_RESTORE)
			{
				rules.add(wildernessTask
					? suggestedRule("Prayer", "Wilderness-only sustain for protection or offensive prayers",
						"Blighted super restore(4)", "blighted super restore", "super restore")
					: suggestedRule("Prayer", "Useful sustain for protection or offensive prayers",
						"Super restore(4)", "super restore"));
			}
			else
			{
				rules.add(suggestedRule("Prayer", "Useful sustain for protection or offensive prayers",
					"Prayer potion(4)", "prayer potion"));
			}
		}

		rules.add(suggestedRule("Food", "Emergency healing for the trip",
			"Food", "anglerfish", "manta ray", "dark crab", "shark", "cooked karambwan", "sea turtle", "monkfish"));
		rules.add(suggestedRule("Run energy", "Optional travel and repositioning sustain",
			"Stamina potion(4)", "stamina potion", "super energy potion", "energy potion"));
		return rules;
	}

	private static boolean usesCombatStyle(GearStrategy strategy, CombatStyle style)
	{
		return strategy != null && strategy.getCombatStyles().contains(style);
	}

	private static String poisonReason(String key)
	{
		if (contains(key, "kalphites")) return "Suggested for poisonous Kalphite soldiers and guardians";
		if (contains(key, "lizardmen")) return "Suggested for Lizardman and shaman poison attacks";
		return "This Slayer target can inflict poison";
	}

	private static void addCannonSetRecommendations(
		List<SupplyRecommendation> recommendations,
		Iterable<OwnedItem> bank,
		Iterable<OwnedItem> packed,
		Set<Integer> usedCanonicalIds)
	{
		String[] parts = {"cannon base", "cannon stand", "cannon barrels", "cannon furnace"};
		OwnedItem packedSet = findExact("dwarf cannon set", packed);
		OwnedItem bankSet = findExact("dwarf cannon set", bank);
		if (packedSet != null || bankSet != null)
		{
			OwnedItem display = bankSet != null ? bankSet : packedSet;
			usedCanonicalIds.add(display.canonicalItemId);
			recommendations.add(new SupplyRecommendation(
				display.itemId,
				display.canonicalItemId,
				display.name,
				"Cannon set",
				"Exchange this boxed set with a Grand Exchange clerk to obtain the four usable cannon parts",
				resolveStatus(packedSet != null, bankSet != null),
				false));
		}
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

	private OwnedItems collectOwnedItems(Item[] items)
	{
		OwnedItems result = new OwnedItems();
		if (items == null) return result;
		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0) continue;
			ItemComposition composition = itemManager.getItemComposition(item.getId());
			if (composition == null || composition.getPlaceholderTemplateId() != -1 || invalidName(composition.getName())) continue;
			int canonical = itemManager.canonicalize(item.getId());
			OwnedItem candidate = new OwnedItem(item.getId(), canonical, composition.getName(), item.getQuantity());
			result.exact.add(candidate);
			OwnedItem existing = result.byCanonical.get(canonical);
			if (existing == null || doseScore(candidate.name) > doseScore(existing.name))
			{
				if (existing != null)
				{
					candidate = new OwnedItem(candidate.itemId, candidate.canonicalItemId,
						candidate.name, candidate.quantity + existing.quantity);
				}
				result.byCanonical.put(canonical, candidate);
			}
			else
			{
				result.byCanonical.put(canonical, new OwnedItem(existing.itemId, existing.canonicalItemId,
					existing.name, existing.quantity + candidate.quantity));
			}
		}
		return result;
	}

	private static OwnedItem findBest(
		SupplyRule rule,
		Iterable<OwnedItem> items,
		Set<Integer> used,
		boolean wildernessTask,
		boolean allowRadasBlessing)
	{
		for (String preferred : rule.preferredNames)
		{
			OwnedItem best = null;
			for (OwnedItem item : items)
			{
				if (used.contains(item.canonicalItemId)) continue;
				String normalizedName = NameMatcher.normalize(item.name);
				if (isUnavailableForContext(
					rule.category, normalizedName, wildernessTask, allowRadasBlessing)) continue;
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
		if (preferred.startsWith("="))
		{
			return normalizedName.equals(preferred.substring(1));
		}
		// Only the base Max cape retains the Max cape utility teleports. Combat
		// variants such as imbued god/max capes contain the same words but do not
		// satisfy the selected home-teleport preference.
		if ("max cape".equals(preferred))
		{
			return "max cape".equals(normalizedName);
		}
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
		return isUnsafeFoodName(normalizedName, true);
	}

	static boolean isUnsafeFoodName(String normalizedName, boolean wildernessTask)
	{
		return normalizedName != null
			&& (normalizedName.startsWith("raw ")
				|| normalizedName.startsWith("burnt ")
				|| (!wildernessTask && normalizedName.startsWith("blighted ")));
	}

	static boolean isWildernessTask(String assignedLocation, GearStrategy strategy)
	{
		if (strategy != null && strategy.getTargetTraits().contains(TargetTrait.WILDERNESS))
		{
			return true;
		}
		String location = NameMatcher.normalize(assignedLocation);
		return location.contains("wilderness") || location.contains("revenant cave");
	}

	static boolean isUnavailableForContext(
		String category,
		String normalizedName,
		boolean wildernessTask,
		boolean allowRadasBlessing)
	{
		// Blighted supplies only function in the Wilderness. Keeping this at the
		// shared matching boundary prevents a generic name such as "super restore"
		// from accidentally selecting its blighted variant for an ordinary task.
		if (!wildernessTask && normalizedName != null
			&& normalizedName.startsWith("blighted "))
		{
			return true;
		}
		if ("Food".equals(category)
			&& isUnsafeFoodName(normalizedName, wildernessTask))
		{
			return true;
		}
		return "Travel".equals(category)
			&& normalizedName.contains("rada's blessing")
			&& !allowRadasBlessing;
	}

	private static void addPrayerRemainsTools(
		List<SupplyRule> rules,
		String taskKey,
		PrayerRemainsPreference preference)
	{
		PrayerRemainsPreference selected = preference == null
			? PrayerRemainsPreference.OFF : preference;
		boolean bonecrusher = selected == PrayerRemainsPreference.BONECRUSHER
			|| selected == PrayerRemainsPreference.BOTH;
		boolean ashSanctifier = selected == PrayerRemainsPreference.ASH_SANCTIFIER
			|| selected == PrayerRemainsPreference.BOTH;

		if (selected == PrayerRemainsPreference.AUTOMATIC)
		{
			if (contains(taskKey, "boss", "demon-boss", "dragon-boss"))
			{
				bonecrusher = true;
				ashSanctifier = true;
			}
			else if (contains(taskKey, "abyssal-demons", "black-demons", "bloodveld",
				"greater-demons", "hellhounds", "nechryael"))
			{
				ashSanctifier = true;
			}
			else if (!contains(taskKey, "smoke-devils"))
			{
				// Most remaining Slayer targets have bones as their normal remains.
				// Smoke devils drop ordinary ashes, which neither tool processes.
				bonecrusher = true;
			}
		}

		if (bonecrusher)
		{
			rules.add(suggestedRule("Prayer remains",
				"Automatically crushes eligible bone drops when charged",
				"Bonecrusher", "bonecrusher", "bonecrusher necklace"));
		}
		if (ashSanctifier)
		{
			rules.add(suggestedRule("Prayer remains",
				"Automatically sanctifies eligible demonic ash drops when charged",
				"Ash sanctifier", "ash sanctifier"));
		}
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
			case "Fishing explosives":
				return taskAmount;
			case "Cannon ammo":
				return Math.max(100, taskAmount * 8);
			case "Araxyte ammunition":
				return Math.max(10, taskAmount * 10);
			case "Food":
				return clamp(2, 12, (taskAmount + 19) / 20);
			case "Prayer":
				return clamp(4, 12, ((taskAmount + 59) / 60) * 4);
			case "Antifire":
			case "Venom protection":
			case "Poison protection":
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
		// Quantities are expressed as doses, not whole four-dose bottles. The bank
		// planner still withdraws the best owned dose variant and correctly treats
		// one four-dose potion as satisfying up to four requested doses.
		String unit = quantityUnit(category);
		return applySupplyLevel(automaticQuantity, level,
			"shots".equals(unit) ? 4 : 1);
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
			case "Poison protection":
			case "Combat boost":
			case "Ranged boost":
			case "Magic boost":
			case "Prayer regen":
			case "Run energy":
			case "Goading":
				return "doses";
			case "Cannon ammo":
			case "Araxyte ammunition":
				return "shots";
			case "Fishing explosives":
				return "uses";
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

	private static int matchingQuantity(
		SupplyRule rule,
		Iterable<OwnedItem> items,
		String unit,
		boolean wildernessTask,
		boolean allowRadasBlessing)
	{
		int total = 0;
		for (OwnedItem item : items)
		{
			String normalizedName = NameMatcher.normalize(item.name);
			if (isUnavailableForContext(
				rule.category, normalizedName, wildernessTask, allowRadasBlessing)) continue;
			boolean matches = false;
			for (String preferred : rule.preferredNames)
			{
				if (matchesPreferredSupply(normalizedName, preferred))
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
		return new SupplyRule(category, reason, required, false, fallback, normalized);
	}

	private static SupplyRule suggestedRule(
		String category,
		String reason,
		String fallback,
		String... preferred)
	{
		List<String> normalized = new ArrayList<>();
		for (String value : preferred) normalized.add(NameMatcher.normalize(value));
		return new SupplyRule(category, reason, false, true, fallback, normalized);
	}

	private static void addEncounterItems(
		List<SupplyRule> rules,
		String profileKey,
		String encounterKey,
		GearStrategy strategy)
	{
		String method = NameMatcher.normalize(strategy == null ? "" : strategy.getName());
		boolean krakenBoss = "kraken".equals(encounterKey)
			|| (profileKey.contains("cave-kraken") && method.contains("kraken boss"));
		if (krakenBoss)
		{
			rules.add(0, rule("Fishing explosives",
				"One fishing explosive per kill instantly awakens the Kraken and all four tentacles",
				true, "Fishing explosive", "fishing explosive"));
		}

		boolean kree = encounterKey.contains("kree'arra")
			|| encounterKey.contains("kree arra");
		boolean aviansies = profileKey.contains("aviansies")
			|| encounterKey.contains("aviansie");
		if (kree || aviansies)
		{
			String reason = kree
				? "Armadyl's Eyrie requires a Mith grapple every time you cross"
				: "Required only when using the Armadyl's Eyrie or Kree'arra route";
			rules.add(kree
				? rule("Armadyl access", reason, true, "Mith grapple", "mith grapple")
				: suggestedRule("Armadyl access", reason, "Mith grapple", "mith grapple"));
			String crossbowReason = kree
				? "A crossbow is required to fire the Mith grapple into Armadyl's Eyrie"
				: "Required with the Mith grapple only when entering Armadyl's Eyrie";
			rules.add(kree
				? rule("Armadyl access", crossbowReason, true, "Crossbow", "crossbow")
				: suggestedRule("Armadyl access", crossbowReason, "Crossbow", "crossbow"));
		}

		if (encounterKey.contains("general graardor"))
		{
			rules.add(rule("Bandos access",
				"Bandos' Stronghold door requires a hammer, warhammer, or Elder maul",
				true, "Hammer", "elder maul", "imcando hammer", "warhammer", "=hammer"));
		}
		if (encounterKey.contains("skotizo"))
		{
			rules.add(rule("Boss access",
				"A Dark totem is consumed when entering Skotizo's lair",
				true, "Dark totem", "dark totem"));
		}
		if (encounterKey.contains("bryophyta"))
		{
			rules.add(rule("Boss access",
				"A Mossy key is consumed when entering Bryophyta's lair",
				true, "Mossy key", "mossy key"));
			rules.add(rule("Encounter tool",
				"A Woodcutting axe or magic secateurs finishes Bryophyta's growthlings",
				true, "Woodcutting axe", "magic secateurs", "3rd age axe",
				"crystal axe", "infernal axe", "dragon axe", "rune axe",
				"adamant axe", "mithril axe", "black axe", "steel axe",
				"iron axe", "bronze axe"));
		}
		if (encounterKey.contains("obor"))
		{
			rules.add(rule("Boss access",
				"A Giant key is consumed when entering Obor's lair",
				true, "Giant key", "giant key"));
		}
		if (encounterKey.contains("barrows"))
		{
			rules.add(rule("Encounter tool",
				"A spade is required to enter the Barrows crypts",
				true, "Spade", "spade"));
			rules.add(suggestedRule("Crypt utility",
				"A Strange old lockpick can shorten routes through the crypt",
				"Strange old lockpick", "strange old lockpick"));
		}
		if (encounterKey.contains("duke sucellus"))
		{
			rules.add(suggestedRule("Encounter tool",
				"A better pickaxe gathers Duke's salax salt faster; an iron pickaxe is available in the arena",
				"Pickaxe", "crystal pickaxe", "infernal pickaxe", "dragon pickaxe",
				"rune pickaxe", "adamant pickaxe", "mithril pickaxe", "black pickaxe",
				"steel pickaxe", "iron pickaxe", "bronze pickaxe"));
		}
		if (encounterKey.contains("brutus"))
		{
			rules.add(suggestedRule("Encounter utility",
				"The Cowbell amulet teleports to Brutus and shortens his respawn when rung",
				"Cowbell amulet", "cowbell amulet"));
		}
		if (encounterKey.contains("branda") || encounterKey.contains("eldric"))
		{
			rules.add(suggestedRule("Boss travel",
				"The Giantsoul amulet teleports directly outside the Royal Titans' tunnel",
				"Giantsoul amulet", "giantsoul amulet"));
		}
		if (encounterKey.contains("sarachnis"))
		{
			rules.add(suggestedRule("Web cutter",
				"A slash weapon cuts dungeon webs unless Aranea boots bypass them",
				"Slash weapon", "wilderness sword", "knife", "scimitar", "longsword",
				"whip", "claws", "halberd"));
		}
		if (encounterKey.contains("dagannoth kings"))
		{
			rules.add(suggestedRule("Waterbirth access",
				"Needed on the standard route; the 85 Agility shortcut bypasses it",
				"Pet rock", "pet rock"));
			rules.add(suggestedRule("Waterbirth access",
				"Needed on the standard route; the 85 Agility shortcut bypasses it",
				"Rune thrownaxe", "rune thrownaxe"));
		}
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
		private final boolean showWhenMissing;
		private final String displayFallback;
		private final List<String> preferredNames;

		private SupplyRule(
			String category,
			String reason,
			boolean required,
			boolean showWhenMissing,
			String displayFallback,
			List<String> preferredNames)
		{
			this.category = category;
			this.reason = reason;
			this.required = required;
			this.showWhenMissing = showWhenMissing;
			this.displayFallback = displayFallback;
			this.preferredNames = preferredNames;
		}

		String getCategory() { return category; }
		List<String> getPreferredNames() { return preferredNames; }
		boolean isRequired() { return required; }
		boolean isShownWhenMissing() { return showWhenMissing; }
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

	static int applyObjectiveQuantity(String category, int quantity,
		GearPriority objective)
	{
		if (quantity <= 0 || objective == null) return Math.max(0, quantity);
		double multiplier = 1.0;
		int unit = "Food".equals(category) ? 1 : 4;
		switch (objective)
		{
			case PRAYER_FIRST:
				if ("Prayer".equals(category) || "Prayer regen".equals(category))
				{
					multiplier = 1.5;
				}
				break;
			case DEFENCE_FIRST:
				if ("Food".equals(category)) multiplier = 1.5;
				break;
			case VALUE:
				if ("Combat boost".equals(category)
					|| "Ranged boost".equals(category)
					|| "Prayer regen".equals(category)
					|| "Goading".equals(category))
				{
					multiplier = 0.5;
				}
				break;
			case BALANCED:
			default:
				if ("Combat boost".equals(category)
					|| "Ranged boost".equals(category)
					|| "Goading".equals(category))
				{
					multiplier = 1.5;
				}
				break;
		}
		if (multiplier == 1.0) return quantity;
		int scaled = (int) Math.ceil(quantity * multiplier);
		return Math.max(unit, ((scaled + unit - 1) / unit) * unit);
	}

	private static final class OwnedItems
	{
		private final List<OwnedItem> exact = new ArrayList<>();
		private final Map<Integer, OwnedItem> byCanonical = new HashMap<>();
	}
}
