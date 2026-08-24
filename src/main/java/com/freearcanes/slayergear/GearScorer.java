package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

/** Scores owned equipment and builds coherent whole-loadout tiers. */
class GearScorer
{
	private static final Set<EquipmentInventorySlot> SUPPORTED_SLOTS = Set.of(
		EquipmentInventorySlot.HEAD, EquipmentInventorySlot.CAPE, EquipmentInventorySlot.AMULET,
		EquipmentInventorySlot.WEAPON, EquipmentInventorySlot.BODY, EquipmentInventorySlot.SHIELD,
		EquipmentInventorySlot.LEGS, EquipmentInventorySlot.GLOVES, EquipmentInventorySlot.BOOTS,
		EquipmentInventorySlot.RING, EquipmentInventorySlot.AMMO);
	private static final List<EquipmentInventorySlot> LOADOUT_SLOT_ORDER = List.of(
		EquipmentInventorySlot.WEAPON,
		EquipmentInventorySlot.HEAD,
		EquipmentInventorySlot.CAPE,
		EquipmentInventorySlot.AMULET,
		EquipmentInventorySlot.BODY,
		EquipmentInventorySlot.SHIELD,
		EquipmentInventorySlot.LEGS,
		EquipmentInventorySlot.GLOVES,
		EquipmentInventorySlot.BOOTS,
		EquipmentInventorySlot.RING,
		EquipmentInventorySlot.AMMO);
	private static final List<EquipmentInventorySlot> INQUISITOR_SET_SLOTS = List.of(
		EquipmentInventorySlot.HEAD,
		EquipmentInventorySlot.BODY,
		EquipmentInventorySlot.LEGS);

	/*
	 * Monster-family passives multiply the player's effective attack/max-hit
	 * rolls, not merely the visible bonuses printed on the weapon itself.
	 * These shared baselines represent the offensive value supplied by levels,
	 * the rest of the worn setup, ammo/spell base damage, prayers and boosts.
	 */
	private static final double WEAPON_SHARED_DAMAGE_BASE = 500.0;
	private static final double WEAPON_SHARED_ACCURACY_BASE = 100.0;
	// All defence styles are a light tie-breaker for otherwise similar armour.
	// Method-specific weights (for example Bloodvelds' Magic defence) are added
	// separately and must be explicitly selected by that strategy.
	private static final double BASE_DEFENCE_WEIGHT = 0.01;

	private final ItemManager itemManager;
	private final SmartSupplyAdvisor supplyAdvisor;

	@Inject
	GearScorer(ItemManager itemManager, SmartSupplyAdvisor supplyAdvisor)
	{
		this.itemManager = itemManager;
		this.supplyAdvisor = supplyAdvisor;
	}

	GearRecommendations score(String taskName, int taskAmount, SlayerTaskProfile profile,
		Item[] gearPool, Item[] bankItems, Item[] packedGearItems, Item[] packedSupplyItems,
		int alternativesPerSlot,
		int magicLevel, int rangedLevel, boolean kourendEliteComplete, boolean ancientSpellbookActive, String preferredStrategy, GearPriority gearPriority, String pinnedItems, String excludedItems, boolean lowRiskMode, int riskCapGp)
	{
		return score(
			taskName,
			taskAmount,
			null,
			profile,
			gearPool,
			bankItems,
			packedGearItems,
			packedSupplyItems,
			alternativesPerSlot,
			magicLevel,
			rangedLevel,
			kourendEliteComplete,
			ancientSpellbookActive,
			preferredStrategy,
			gearPriority,
			pinnedItems,
			excludedItems,
			lowRiskMode,
			riskCapGp);
	}

	GearRecommendations score(
		String taskName,
		int taskAmount,
		String assignedLocation,
		SlayerTaskProfile profile,
		Item[] gearPool,
		Item[] bankItems,
		Item[] packedGearItems,
		Item[] packedSupplyItems,
		int alternativesPerSlot,
		int magicLevel,
		int rangedLevel,
		boolean kourendEliteComplete,
		boolean ancientSpellbookActive,
		String preferredStrategy,
		GearPriority gearPriority,
		String pinnedItems,
		String excludedItems,
		boolean lowRiskMode,
		int riskCapGp)
	{
		return score(
			taskName, taskAmount, assignedLocation, profile, gearPool, bankItems,
			packedGearItems, packedSupplyItems, alternativesPerSlot, magicLevel,
			rangedLevel, kourendEliteComplete, ancientSpellbookActive,
			preferredStrategy, gearPriority, pinnedItems, excludedItems,
			lowRiskMode, riskCapGp, false, false);
	}

	GearRecommendations score(
		String taskName,
		int taskAmount,
		String assignedLocation,
		SlayerTaskProfile profile,
		Item[] gearPool,
		Item[] bankItems,
		Item[] packedGearItems,
		Item[] packedSupplyItems,
		int alternativesPerSlot,
		int magicLevel,
		int rangedLevel,
		boolean kourendEliteComplete,
		boolean ancientSpellbookActive,
		String preferredStrategy,
		GearPriority gearPriority,
		String pinnedItems,
		String excludedItems,
		boolean lowRiskMode,
		int riskCapGp,
		boolean loadedDizanasQuiver,
		boolean arceuusSpellbookActive)
	{
		return score(taskName, taskAmount, assignedLocation, profile, gearPool,
			bankItems, packedGearItems, packedSupplyItems, alternativesPerSlot,
			magicLevel, rangedLevel, kourendEliteComplete, ancientSpellbookActive,
			preferredStrategy, gearPriority, pinnedItems, excludedItems, lowRiskMode,
			riskCapGp, loadedDizanasQuiver, arceuusSpellbookActive,
			magicLevel, magicLevel);
	}

	GearRecommendations score(
		String taskName,
		int taskAmount,
		String assignedLocation,
		SlayerTaskProfile profile,
		Item[] gearPool,
		Item[] bankItems,
		Item[] packedGearItems,
		Item[] packedSupplyItems,
		int alternativesPerSlot,
		int magicLevel,
		int rangedLevel,
		boolean kourendEliteComplete,
		boolean ancientSpellbookActive,
		String preferredStrategy,
		GearPriority gearPriority,
		String pinnedItems,
		String excludedItems,
		boolean lowRiskMode,
		int riskCapGp,
		boolean loadedDizanasQuiver,
		boolean arceuusSpellbookActive,
		int attackLevel,
		int strengthLevel)
	{
		return score(taskName, taskAmount, assignedLocation, profile, gearPool,
			bankItems, packedGearItems, packedSupplyItems, alternativesPerSlot,
			magicLevel, rangedLevel, kourendEliteComplete, ancientSpellbookActive,
			preferredStrategy, gearPriority, pinnedItems, excludedItems, lowRiskMode,
			riskCapGp, loadedDizanasQuiver, arceuusSpellbookActive, attackLevel,
			strengthLevel, CombatLevelContext.unboosted(
				attackLevel, strengthLevel, magicLevel, rangedLevel));
	}

	GearRecommendations score(
		String taskName, int taskAmount, String assignedLocation,
		SlayerTaskProfile profile, Item[] gearPool, Item[] bankItems,
		Item[] packedGearItems, Item[] packedSupplyItems, int alternativesPerSlot,
		int magicLevel, int rangedLevel, boolean kourendEliteComplete,
		boolean ancientSpellbookActive, String preferredStrategy,
		GearPriority gearPriority, String pinnedItems, String excludedItems,
		boolean lowRiskMode, int riskCapGp, boolean loadedDizanasQuiver,
		boolean arceuusSpellbookActive, int attackLevel, int strengthLevel,
		CombatLevelContext combatLevels)
	{
		Map<Integer, Integer> canonicalByItemId = new HashMap<>();
		Set<Integer> bankCanonical = canonicalIds(bankItems, canonicalByItemId);
		Set<Integer> packedCanonical = canonicalIds(packedGearItems, canonicalByItemId);
		List<BankEquipment> equipment = collectEquipment(
			gearPool, bankCanonical, packedCanonical, canonicalByItemId);
		Set<String> ownedNames = collectOwnedNames(equipment);
		List<GearStrategy> eligible = eligibleStrategies(profile, ownedNames, magicLevel, rangedLevel);
		GearStrategy selected = selectStrategy(profile, eligible, preferredStrategy);
		List<GearStrategy> alternatives = new ArrayList<>(eligible);
		alternatives.remove(selected);

		Set<String> pinned = parsePreferenceTokens(pinnedItems);
		Set<String> excluded = parsePreferenceTokens(excludedItems);
		List<GearRequirement> requirements = TaskSafetyRules.gearRequirements(profile.getKey(), selected, kourendEliteComplete);
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates = buildCandidates(
			equipment, selected, requirements, gearPriority, pinned, excluded, lowRiskMode, riskCapGp);

		int tiersWanted = Math.max(1, Math.min(3, alternativesPerSlot));
		List<LoadoutTier> loadoutTiers = new ArrayList<>();
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot = new EnumMap<>(EquipmentInventorySlot.class);
		List<Map<EquipmentInventorySlot, GearRecommendation>> coherentLoadouts =
			buildCoherentLoadouts(
				tiersWanted,
				candidates,
				selected,
				requirements,
				pinned,
				lowRiskMode,
				riskCapGp);
		if (loadedDizanasQuiver)
		{
			usePrayerBlessings(coherentLoadouts, candidates, selected);
		}
		Map<Integer, BankEquipment> equipmentByCanonicalId = new HashMap<>();
		for (BankEquipment item : equipment)
		{
			equipmentByCanonicalId.put(item.canonicalItemId, item);
		}
		double bestOffenseEstimate = 0;
		List<TargetDefence> offenseTargets = TargetDefenceCatalog.find(
			taskName, profile.getKey(), selected);
		if ((gearPriority == GearPriority.BALANCED || gearPriority == GearPriority.VALUE)
			&& !offenseTargets.isEmpty())
		{
			coherentLoadouts.sort(Comparator.comparingDouble(
				(Map<EquipmentInventorySlot, GearRecommendation> loadout) -> averageOffense(
					loadout, equipmentByCanonicalId, selected, offenseTargets, combatLevels))
				.reversed());
		}
		for (int index = 0; index < coherentLoadouts.size(); index++)
		{
			int rank = index + 1;
			Map<EquipmentInventorySlot, GearRecommendation> loadout = new EnumMap<>(EquipmentInventorySlot.class);
			for (Map.Entry<EquipmentInventorySlot, GearRecommendation> entry
				: coherentLoadouts.get(index).entrySet())
			{
				loadout.put(entry.getKey(), entry.getValue().withRank(rank));
			}
			int loadoutRisk = lowRiskMode ? totalRecommendationGuidePrice(loadout) : 0;
			double minimumOffense = Double.POSITIVE_INFINITY;
			double maximumOffense = 0;
			double minimumSecondsPerKill = Double.POSITIVE_INFINITY;
			double maximumSecondsPerKill = 0;
			for (TargetDefence offenseTarget : offenseTargets)
			{
				double targetOffense = LoadoutOffenseEstimator.estimate(loadout,
					equipmentByCanonicalId, selected, offenseTarget, combatLevels);
				if (targetOffense > 0)
				{
					minimumOffense = Math.min(minimumOffense, targetOffense);
					maximumOffense = Math.max(maximumOffense, targetOffense);
					if (offenseTarget.getHitpoints() > 0)
					{
						double secondsPerKill = offenseTarget.getHitpoints() / targetOffense;
						minimumSecondsPerKill = Math.min(minimumSecondsPerKill, secondsPerKill);
						maximumSecondsPerKill = Math.max(maximumSecondsPerKill, secondsPerKill);
					}
				}
			}
			double offense = maximumOffense > 0 ? (minimumOffense + maximumOffense) / 2.0 : 0;
			String offenseMethod = offenseMethodName(
				loadout, equipmentByCanonicalId, selected, combatLevels);
			if (index == 0) bestOffenseEstimate = offense;
			double relativePercent = bestOffenseEstimate > 0
				? (offense / bestOffenseEstimate - 1.0) * 100.0 : 0;
			loadoutTiers.add(new LoadoutTier(
				rank, loadout, loadoutRisk, lowRiskMode ? riskCapGp : 0,
				offense > 0
					? LoadoutOffenseEstimate.range(minimumOffense, maximumOffense,
						relativePercent, offenseTargets.size() == 1
							? offenseTargets.get(0).getName()
							: offenseTargets.size() + " target variants", offenseMethod,
						minimumSecondsPerKill == Double.POSITIVE_INFINITY ? 0 : minimumSecondsPerKill,
						maximumSecondsPerKill)
					: LoadoutOffenseEstimate.unavailable()));
			for (Map.Entry<EquipmentInventorySlot, GearRecommendation> entry : loadout.entrySet())
			{
				List<GearRecommendation> slotRecommendations = bySlot.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>());
				boolean alreadyListed = slotRecommendations.stream()
					.anyMatch(existing -> existing.getCanonicalItemId() == entry.getValue().getCanonicalItemId());
				if (!alreadyListed)
				{
					slotRecommendations.add(entry.getValue());
				}
			}
		}

		Map<EquipmentInventorySlot, GearRecommendation> best = loadoutTiers.isEmpty()
			? Collections.emptyMap() : loadoutTiers.get(0).getItems();
		boolean bestUsesLoadedDizanasQuiver =
			loadedDizanasQuiver && usesDizanasQuiver(best);
		// Rada's blessing occupies the ammunition slot. It is a coherent travel
		// suggestion only when the selected Dizana's quiver already carries the
		// ranged weapon's ammunition.
		List<SupplyRecommendation> supplies = supplyAdvisor.recommend(
			profile,
			selected,
			assignedLocation,
			taskAmount,
			bankItems,
			packedSupplyItems,
			bestUsesLoadedDizanasQuiver,
			gearPriority);
		// A protective off-hand already satisfies dragonfire protection. Do not
		// simultaneously tell the player that antifire is still required.
		if (hasDragonfireProtection(best))
		{
			supplies = withoutCategory(supplies, "Antifire");
		}
		ReadinessReport readiness = readiness(
			best, requirements, supplies, selected, magicLevel,
			ancientSpellbookActive, arceuusSpellbookActive, bestUsesLoadedDizanasQuiver);
		List<GearRecommendation> weaponSwitches = bossWeaponSwitches(
			taskName, profile, selected, equipment, best, requirements,
			gearPriority, pinned, excluded);
		List<ObjectiveLoadoutComparison> objectiveComparisons = objectiveComparisons(
			equipment, selected, requirements, pinned, excluded, lowRiskMode,
			riskCapGp, loadedDizanasQuiver, best, gearPriority);

		return GearRecommendations.ready(taskName, taskAmount, profile, selected, alternatives,
			bySlot, loadoutTiers, weaponSwitches, supplies, readiness,
			equipment.size(), gearPriority, objectiveComparisons);
	}

	private List<ObjectiveLoadoutComparison> objectiveComparisons(
		List<BankEquipment> equipment,
		GearStrategy strategy,
		List<GearRequirement> requirements,
		Set<String> pinned,
		Set<String> excluded,
		boolean lowRiskMode,
		int riskCapGp,
		boolean loadedDizanasQuiver,
		Map<EquipmentInventorySlot, GearRecommendation> selectedLoadout,
		GearPriority selectedObjective)
	{
		List<ObjectiveLoadoutComparison> comparisons = new ArrayList<>();
		Map<EquipmentInventorySlot, GearRecommendation> dpsPreview = selectedLoadout;
		if (selectedObjective != GearPriority.BALANCED
			&& selectedObjective != GearPriority.VALUE)
		{
			dpsPreview = objectivePreview(equipment, strategy, requirements, pinned,
				excluded, lowRiskMode, riskCapGp, loadedDizanasQuiver,
				GearPriority.BALANCED);
		}
		for (GearPriority objective : GearPriority.values())
		{
			Map<EquipmentInventorySlot, GearRecommendation> preview = selectedLoadout;
			if (objective == GearPriority.BALANCED || objective == GearPriority.VALUE)
			{
				preview = dpsPreview;
			}
			else if (objective != selectedObjective)
			{
				preview = objectivePreview(equipment, strategy, requirements, pinned,
					excluded, lowRiskMode, riskCapGp, loadedDizanasQuiver, objective);
			}
			comparisons.add(new ObjectiveLoadoutComparison(objective,
				objectiveGearChanges(selectedLoadout, preview,
					objective == selectedObjective)));
		}
		return comparisons;
	}

	private Map<EquipmentInventorySlot, GearRecommendation> objectivePreview(
		List<BankEquipment> equipment, GearStrategy strategy,
		List<GearRequirement> requirements, Set<String> pinned, Set<String> excluded,
		boolean lowRiskMode, int riskCapGp, boolean loadedDizanasQuiver,
		GearPriority objective)
	{
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates = buildCandidates(
			equipment, strategy, requirements, objective, pinned, excluded,
			lowRiskMode, riskCapGp);
		List<Map<EquipmentInventorySlot, GearRecommendation>> loadouts = buildCoherentLoadouts(
			1, candidates, strategy, requirements, pinned, lowRiskMode, riskCapGp);
		if (loadedDizanasQuiver) usePrayerBlessings(loadouts, candidates, strategy);
		return loadouts.isEmpty() ? Collections.emptyMap() : loadouts.get(0);
	}

	static String objectiveGearChanges(
		Map<EquipmentInventorySlot, GearRecommendation> selected,
		Map<EquipmentInventorySlot, GearRecommendation> preview,
		boolean current)
	{
		if (current) return "Current Tier 1 loadout";
		List<String> changes = new ArrayList<>();
		for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			GearRecommendation before = selected.get(slot);
			GearRecommendation after = preview.get(slot);
			String beforeName = before == null ? "" : before.getItemName();
			String afterName = after == null ? "" : after.getItemName();
			if (!NameMatcher.normalize(beforeName).equals(NameMatcher.normalize(afterName)))
			{
				String slotName = slot.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
				changes.add(slotName + ": " + (afterName.isEmpty() ? "empty" : afterName));
			}
		}
		if (changes.isEmpty()) return "Same Tier 1 gear";
		String result = String.join(" · ", changes.subList(0, Math.min(3, changes.size())));
		return changes.size() > 3 ? result + " · +" + (changes.size() - 3) + " slots" : result;
	}

	private static double averageOffense(
		Map<EquipmentInventorySlot, GearRecommendation> loadout,
		Map<Integer, BankEquipment> equipmentByCanonicalId,
		GearStrategy strategy,
		List<TargetDefence> targets,
		CombatLevelContext levels)
	{
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = 0;
		for (TargetDefence target : targets)
		{
			double estimate = LoadoutOffenseEstimator.estimate(
				loadout, equipmentByCanonicalId, strategy, target, levels);
			if (estimate > 0)
			{
				minimum = Math.min(minimum, estimate);
				maximum = Math.max(maximum, estimate);
			}
		}
		return maximum > 0 ? (minimum + maximum) / 2.0 : 0;
	}

	private static String offenseMethodName(
		Map<EquipmentInventorySlot, GearRecommendation> loadout,
		Map<Integer, BankEquipment> equipmentByCanonicalId,
		GearStrategy strategy,
		CombatLevelContext levels)
	{
		if (strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			GearRecommendation rangedWeapon = loadout.get(EquipmentInventorySlot.WEAPON);
			BankEquipment rangedEquipment = rangedWeapon == null ? null
				: equipmentByCanonicalId.get(rangedWeapon.getCanonicalItemId());
			if (rangedEquipment != null
				&& NameMatcher.normalize(rangedEquipment.name).contains("twisted bow"))
			{
				return "Twisted bow target-Magic scaling";
			}
			if (rangedEquipment != null
				&& NameMatcher.normalize(rangedEquipment.name).contains("venator bow"))
			{
				return "Venator ricochet · 3 hits";
			}
			if (rangedEquipment != null
				&& NameMatcher.normalize(rangedEquipment.name).contains("eclipse atlatl"))
			{
				return "Eclipse atlatl · burn EV when full set";
			}
			return LoadoutOffenseEstimator.rangedEffectName(loadout, equipmentByCanonicalId);
		}
		if (strategy.getCombatStyle() != CombatStyle.MAGIC) return "";
		GearRecommendation weapon = loadout.get(EquipmentInventorySlot.WEAPON);
		BankEquipment equipment = weapon == null ? null
			: equipmentByCanonicalId.get(weapon.getCanonicalItemId());
		MagicCombatMethod method = MagicCombatMethod.resolve(strategy,
			equipment == null ? "" : equipment.name, levels.getBoostedMagic());
		return method == null ? "" : method.getName()
			+ (strategy.isAncientAoe() ? " · 9 targets" : "");
	}

	List<GearRecommendation> bossWeaponSwitches(
		String taskName,
		SlayerTaskProfile profile,
		GearStrategy strategy,
		List<BankEquipment> equipment,
		Map<EquipmentInventorySlot, GearRecommendation> best,
		List<GearRequirement> requirements,
		GearPriority gearPriority,
		Set<String> pinned,
		Set<String> excluded)
	{
		String key = NameMatcher.normalize(profile.getKey());
		String task = NameMatcher.normalize(taskName);
		boolean boss = key.contains("boss") || BossSlayerCatalog.contains(task);
		if (!boss) return Collections.emptyList();

		Set<Integer> selectedIds = best.values().stream()
			.map(GearRecommendation::getCanonicalItemId).collect(java.util.stream.Collectors.toSet());
		List<GearRecommendation> switches = new ArrayList<>();
		// Vardorvis is immune to defence reduction. Most other melee/ranged bosses
		// can benefit from one owned drain option; only the highest tier is packed.
		if (!task.contains("vardorvis") && strategy.getCombatStyle() != CombatStyle.MAGIC)
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Defence reduction", "elder maul", "dragon warhammer", "bandos godsword");
		}
		if (strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Damage special", "zaryte crossbow");
		}
		else if (strategy.getCombatStyle() == CombatStyle.MAGIC)
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Damage special", "volatile nightmare staff", "eldritch nightmare staff");
		}
		else if (strategy.getTargetTraits().contains(TargetTrait.DEMON))
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Damage special", "burning claws", "dragon claws", "voidwaker", "armadyl godsword");
		}
		else
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Damage special", "dragon claws", "burning claws", "voidwaker", "armadyl godsword");
		}
		if (task.contains("araxxor"))
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Spawn weapon", "noxious halberd", "heavy ballista", "dragon crossbow");
		}
		else if (task.contains("abyssal sire"))
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Phase weapon", "blood ancient sceptre", "ancient blood sceptre",
				"sanguinesti staff", "trident of the swamp", "trident of the seas");
		}
		else if (task.contains("kalphite queen"))
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Phase weapon", "bow of faerdhinen", "toxic blowpipe", "twisted bow",
				"dragon hunter crossbow");
		}
		else if (task.contains("grotesque guardians"))
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Phase weapon", "bow of faerdhinen", "toxic blowpipe", "eclipse atlatl");
		}
		else if (task.contains("demonic gorilla") || task.contains("tormented demon"))
		{
			if (strategy.getCombatStyle() == CombatStyle.MELEE)
				addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
					"Phase weapon", "scorching bow", "bow of faerdhinen", "toxic blowpipe");
			else
				addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
					"Phase weapon", "emberlight", "arclight", "darklight");
		}
		else if (task.contains("phantom muspah"))
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Phase weapon", "zaryte crossbow", "dragon crossbow", "armadyl crossbow");
		}
		else if (task.contains("zulrah"))
		{
			addFirstOwnedSwitch(switches, equipment, selectedIds, strategy,
				"Phase weapon", "tumeken's shadow", "sanguinesti staff", "trident of the swamp",
				"bow of faerdhinen", "twisted bow", "toxic blowpipe");
		}
		return includeApplicableOffhands(
			switches, equipment, best, requirements, gearPriority, pinned, excluded, strategy);
	}

	private List<GearRecommendation> includeApplicableOffhands(
		List<GearRecommendation> weaponSwitches,
		List<BankEquipment> equipment,
		Map<EquipmentInventorySlot, GearRecommendation> best,
		List<GearRequirement> requirements,
		GearPriority gearPriority,
		Set<String> pinned,
		Set<String> excluded,
		GearStrategy taskStrategy)
	{
		List<GearRecommendation> result = new ArrayList<>();
		Set<Integer> alreadyAvailable = best.values().stream()
			.map(GearRecommendation::getCanonicalItemId)
			.collect(java.util.stream.Collectors.toSet());
		for (GearRecommendation weapon : weaponSwitches)
		{
			result.add(weapon);
			alreadyAvailable.add(weapon.getCanonicalItemId());
			if (weapon.isTwoHanded()) continue;

			GearStrategy switchStrategy = offhandStrategy(taskStrategy, weapon.getItemName());
			BankEquipment bestOffhand = null;
			double bestScore = Double.NEGATIVE_INFINITY;
			for (BankEquipment item : equipment)
			{
				if (item.slot != EquipmentInventorySlot.SHIELD
					|| matchesAnyPreference(item.name, excluded)
					|| !satisfiesOffhandRequirements(item, requirements)) continue;
				double score = scoreStats(
					switchStrategy, item.name, item.slot, item.stats, gearPriority);
				if (matchesAnyPreference(item.name, pinned)) score += 5_000;
				if (bestOffhand == null || score > bestScore
					|| (score == bestScore && item.name.compareTo(bestOffhand.name) < 0))
				{
					bestOffhand = item;
					bestScore = score;
				}
			}
			if (bestOffhand == null || alreadyAvailable.contains(bestOffhand.canonicalItemId)) continue;

			result.add(recommendation(
				bestOffhand,
				1,
				switchStrategy,
				bestScore,
				weapon.getReason().split(" switch", 2)[0]
					+ " off-hand for " + weapon.getItemName() + " switch"));
			alreadyAvailable.add(bestOffhand.canonicalItemId);
		}
		return result;
	}

	private static boolean satisfiesOffhandRequirements(
		BankEquipment item,
		List<GearRequirement> requirements)
	{
		if (requirements == null) return true;
		for (GearRequirement requirement : requirements)
		{
			if (requirement.restricts(EquipmentInventorySlot.SHIELD)
				&& !requirement.matchesForSlot(EquipmentInventorySlot.SHIELD, item.name)) return false;
		}
		return true;
	}

	private static GearStrategy offhandStrategy(GearStrategy taskStrategy, String weaponName)
	{
		String name = NameMatcher.normalize(weaponName);
		CombatStyle style = has(name, "crossbow", "bow", "blowpipe", "ballista", "atlatl")
			? CombatStyle.RANGED
			: has(name, "staff", "sceptre", "trident", "tumeken's shadow")
				? CombatStyle.MAGIC
				: CombatStyle.MELEE;
		if (style == taskStrategy.getCombatStyle()) return taskStrategy;
		return GearStrategy.builder()
			.name("Boss switch off-hand")
			.combatStyle(style)
			.attackType(AttackType.BALANCED)
			.targetTraits(taskStrategy.getTargetTraits())
			.magicDefenceWeight(taskStrategy.getMagicDefenceWeight())
			.prayerWeight(taskStrategy.getPrayerWeight())
			.build();
	}

	private void addFirstOwnedSwitch(
		List<GearRecommendation> result,
		List<BankEquipment> equipment,
		Set<Integer> selectedIds,
		GearStrategy strategy,
		String reason,
		String... orderedNames)
	{
		for (String wanted : orderedNames)
		{
			for (BankEquipment item : equipment)
			{
				if (item.slot != EquipmentInventorySlot.WEAPON
					|| selectedIds.contains(item.canonicalItemId)
					|| !NameMatcher.normalize(item.name).contains(wanted)) continue;
				result.add(recommendation(item, 1, strategy, item.score,
					reason + " switch (best owned applicable tier)"));
				return;
			}
		}
	}

	void usePrayerBlessings(
		List<Map<EquipmentInventorySlot, GearRecommendation>> loadouts,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy)
	{
		List<BankEquipment> blessings = new ArrayList<>();
		for (BankEquipment item :
			candidates.getOrDefault(EquipmentInventorySlot.AMMO, Collections.emptyList()))
		{
			if (NameMatcher.normalize(item.name).contains("blessing"))
			{
				blessings.add(item);
			}
		}
		blessings.sort(Comparator
			.comparingInt((BankEquipment item) -> item.stats.getPrayer()).reversed()
			.thenComparing(Comparator.comparingDouble(
				(BankEquipment item) -> item.score).reversed()));
		int blessingIndex = 0;
		for (int index = 0;
			index < loadouts.size() && blessingIndex < blessings.size();
			index++)
		{
			Map<EquipmentInventorySlot, GearRecommendation> loadout = loadouts.get(index);
			if (!usesDizanasQuiver(loadout))
			{
				continue;
			}
			loadout.put(
				EquipmentInventorySlot.AMMO,
				recommendation(blessings.get(blessingIndex), index + 1, strategy));
			blessingIndex++;
		}
	}

	static boolean usesDizanasQuiver(
		Map<EquipmentInventorySlot, GearRecommendation> loadout)
	{
		GearRecommendation cape = loadout.get(EquipmentInventorySlot.CAPE);
		if (cape == null)
		{
			return false;
		}
		return isDizanasQuiverCapeName(cape.getItemName());
	}

	static boolean isDizanasQuiverCapeName(String itemName)
	{
		String name = NameMatcher.normalize(itemName);
		return name.contains("dizana")
			&& (name.contains("quiver") || name.contains("max cape"));
	}

	private List<GearStrategy> eligibleStrategies(SlayerTaskProfile profile, Set<String> names, int magic, int ranged)
	{
		List<GearStrategy> result = new ArrayList<>();
		for (GearStrategy s : profile.getStrategies()) if (isEligible(s, names, magic, ranged)) result.add(s);
		if (result.isEmpty() && !profile.getStrategies().isEmpty()) result.add(profile.getStrategies().get(profile.getStrategies().size() - 1));
		return result;
	}

	private static GearStrategy selectStrategy(SlayerTaskProfile profile, List<GearStrategy> eligible, String preferred)
	{
		if (preferred != null && !preferred.trim().isEmpty())
		{
			String want = NameMatcher.normalize(preferred);
			for (GearStrategy s : eligible) if (NameMatcher.normalize(s.getName()).equals(want)) return s;
		}
		return eligible.isEmpty() ? profile.getStrategies().get(0) : eligible.get(0);
	}

	Map<EquipmentInventorySlot, List<BankEquipment>> buildCandidates(List<BankEquipment> equipment,
		GearStrategy strategy, List<GearRequirement> requirements, GearPriority gearPriority, Set<String> pinned, Set<String> excluded,
		boolean lowRiskMode, int riskCapGp)
	{
		Map<EquipmentInventorySlot, List<BankEquipment>> result = new EnumMap<>(EquipmentInventorySlot.class);
		boolean shieldRequired = requiresMandatoryOffhand(requirements);
		for (BankEquipment item : equipment)
		{
			boolean explicitlyPinned = matchesAnyPreference(item.name, pinned);
			if (matchesAnyPreference(item.name, excluded) || !allowed(item, strategy)) continue;
			// Safety beats raw DPS: a mandatory off-hand makes every 2H weapon an
			// invalid candidate, otherwise the protection pass could create an
			// impossible weapon + shield loadout.
			if (shieldRequired && item.slot == EquipmentInventorySlot.WEAPON && item.stats.isTwoHanded()) continue;
			boolean blocked = false;
			for (GearRequirement req : requirements)
			{
				if (req.restricts(item.slot) && !req.matchesForSlot(item.slot, item.name)) { blocked = true; break; }
			}
			if (blocked) continue;
			item.score = scoreStats(strategy, item.name, item.slot, item.stats, gearPriority);
			item.guidePrice = lowRiskMode ? Math.max(0, itemManager.getItemPrice(item.itemId)) : 0;
			item.pinned = explicitlyPinned;
			if (explicitlyPinned) item.score += 5_000;
			result.computeIfAbsent(item.slot, ignored -> new ArrayList<>()).add(item);
		}
		for (List<BankEquipment> values : result.values())
			values.sort(Comparator.comparingDouble((BankEquipment i) -> i.score).reversed().thenComparing(i -> i.name));
		return result;
	}

	static boolean requiresMandatoryOffhand(List<GearRequirement> requirements)
	{
		return requirements != null && requirements.stream()
			.anyMatch(requirement -> requirement.restricts(EquipmentInventorySlot.SHIELD));
	}

	private Map<EquipmentInventorySlot, GearRecommendation> buildLoadout(int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		List<GearRequirement> requirements,
		boolean lowRiskMode,
		int riskCapGp)
	{
		EnumMap<EquipmentInventorySlot, GearRecommendation> selected = new EnumMap<>(EquipmentInventorySlot.class);
		if (lowRiskMode && riskCapGp > 0)
		{
			for (Map.Entry<EquipmentInventorySlot, BankEquipment> entry :
				selectRiskBudgetItems(rank, candidates, strategy, riskCapGp).entrySet())
			{
				selected.put(entry.getKey(), recommendation(entry.getValue(), rank, strategy));
			}
		}
		else
		{
			// Compare the complete main-hand/off-hand package. Selecting each slot
			// independently makes every two-handed weapon forfeit the value of the
			// independently selected shield after the ranking decision has already
			// been made.
			Map<EquipmentInventorySlot, BankEquipment> weaponPair =
				selectWeaponPair(rank, candidates, strategy);
			for (EquipmentInventorySlot slot : List.of(
				EquipmentInventorySlot.WEAPON, EquipmentInventorySlot.SHIELD))
			{
				BankEquipment item = weaponPair.get(slot);
				if (item == null) selected.remove(slot);
				else selected.put(slot, recommendation(item, rank, strategy));
			}

			BankEquipment selectedWeapon = weaponPair.get(EquipmentInventorySlot.WEAPON);
			String selectedWeaponName = selectedWeapon == null ? "" : selectedWeapon.name;
			for (EquipmentInventorySlot slot : SUPPORTED_SLOTS)
			{
				if (slot == EquipmentInventorySlot.WEAPON || slot == EquipmentInventorySlot.SHIELD) continue;
				List<BankEquipment> list = contextualCandidates(
					candidates.getOrDefault(slot, Collections.emptyList()),
					strategy,
					selectedWeaponName);
				if (rank <= list.size())
				{
					selected.put(slot, contextualRecommendation(
						list.get(rank - 1), rank, strategy, selectedWeaponName));
				}
			}
		}

		// Whole-loadout compatibility: a 2H weapon consumes the off-hand.
		GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
		if (weapon != null && weapon.isTwoHanded()) selected.remove(EquipmentInventorySlot.SHIELD);

		// Match ammo to weapon type; self-ammo weapons intentionally omit the ammo slot.
		if (weapon != null && strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			String w = NameMatcher.normalize(weapon.getItemName());
			if (usesNoAmmoSlot(w))
			{
				BankEquipment blessing = nthPrayerBlessing(
					candidates.getOrDefault(EquipmentInventorySlot.AMMO, Collections.emptyList()), rank);
				if (blessing == null) selected.remove(EquipmentInventorySlot.AMMO);
				else selected.put(EquipmentInventorySlot.AMMO, recommendation(blessing, rank, strategy));
			}
			else
			{
				List<BankEquipment> ammo = candidates.getOrDefault(EquipmentInventorySlot.AMMO, Collections.emptyList());
				BankEquipment compatible = nthCompatibleAmmo(ammo, w, rank);
				if (compatible == null) selected.remove(EquipmentInventorySlot.AMMO);
				else selected.put(EquipmentInventorySlot.AMMO, recommendation(compatible, rank, strategy));
			}
		}

		// Force mandatory protection into the cohesive loadout when it is owned.
		for (GearRequirement req : requirements)
		{
			if (req.isSatisfied(selected)) continue;
			for (GearRequirement.Option option : req.getOptions())
			{
				List<BankEquipment> list = candidates.getOrDefault(option.getSlot(), Collections.emptyList());
				for (BankEquipment item : list)
				{
					if (option.matches(item.name))
					{
						selected.put(option.getSlot(), recommendation(item, rank, strategy));
						break;
					}
				}
				if (req.isSatisfied(selected)) break;
			}
		}
		if (!lowRiskMode)
		{
			applyInquisitorSetIfBetter(
				selected, candidates, strategy, requirements, rank);
		}
		// Encounter safety takes precedence over the optional risk budget, just as
		// mandatory protection gear does. If the bank cannot reach the threshold,
		// readiness reports the exact selected weight instead.
		ensureMinimumEquippedWeight(selected, candidates, strategy, requirements, rank);
		return selected;
	}

	private void ensureMinimumEquippedWeight(
		Map<EquipmentInventorySlot, GearRecommendation> selected,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		List<GearRequirement> requirements,
		int rank)
	{
		double minimum = strategy.getMinimumEquippedWeightKg();
		if (minimum <= 0) return;

		while (totalEquippedWeight(selected) + 0.0001 < minimum)
		{
			WeightUpgrade best = null;
			GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
			String weaponName = weapon == null ? "" : weapon.getItemName();
			for (EquipmentInventorySlot slot : LOADOUT_SLOT_ORDER)
			{
				if (slot == EquipmentInventorySlot.WEAPON
					|| (slot == EquipmentInventorySlot.SHIELD
						&& weapon != null && weapon.isTwoHanded())) continue;

				GearRecommendation current = selected.get(slot);
				double currentWeight = current == null ? 0 : current.getWeightKg();
				double currentScore = current == null ? 0 : current.getScore();
				List<BankEquipment> slotCandidates = preferredPinnedChoices(
					candidates.getOrDefault(slot, Collections.emptyList()));
				for (BankEquipment candidate : slotCandidates)
				{
					if (current != null
						&& candidate.canonicalItemId == current.getCanonicalItemId()) continue;
					if (!preservesRequirements(selected, slot, candidate.name, requirements)) continue;
					double gain = candidate.weightKg - currentWeight;
					if (gain <= 0.0001) continue;
					double candidateScore = contextualScore(
						strategy, weaponName, candidate.name, slot,
						candidate.stats, candidate.score);
					double loss = currentScore - candidateScore;
					WeightUpgrade upgrade = new WeightUpgrade(
						slot, candidate, gain, loss,
						totalEquippedWeight(selected) + gain >= minimum);
					if (upgrade.isBetterThan(best)) best = upgrade;
				}
			}
			if (best == null) break;
			selected.put(best.slot, contextualRecommendation(
				best.candidate, rank, strategy, weaponName));
		}
	}

	private static boolean preservesRequirements(
		Map<EquipmentInventorySlot, GearRecommendation> selected,
		EquipmentInventorySlot replacedSlot,
		String replacementName,
		List<GearRequirement> requirements)
	{
		for (GearRequirement requirement : requirements)
		{
			boolean satisfied = false;
			for (GearRequirement.Option option : requirement.getOptions())
			{
				if (option.getSlot() == replacedSlot)
				{
					satisfied |= option.matches(replacementName);
				}
				else
				{
					GearRecommendation retained = selected.get(option.getSlot());
					satisfied |= retained != null && option.matches(retained.getItemName());
				}
				if (satisfied) break;
			}
			if (!satisfied) return false;
		}
		return true;
	}

	static double totalEquippedWeight(
		Map<EquipmentInventorySlot, GearRecommendation> selected)
	{
		double total = 0;
		if (selected != null)
		{
			for (GearRecommendation recommendation : selected.values())
			{
				total += recommendation.getWeightKg();
			}
		}
		return total;
	}

	private void applyInquisitorSetIfBetter(
		Map<EquipmentInventorySlot, GearRecommendation> selected,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		List<GearRequirement> requirements,
		int rank)
	{
		GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
		if (weapon == null) return;
		String weaponName = NameMatcher.normalize(weapon.getItemName());
		Map<EquipmentInventorySlot, BankEquipment> inquisitor =
			inquisitorSet(rank, candidates, strategy, weaponName);
		if (inquisitor.isEmpty()) return;

		double currentScore = 0;
		double setScore = inquisitorFullSetBonus();
		for (EquipmentInventorySlot slot : INQUISITOR_SET_SLOTS)
		{
			BankEquipment setPiece = inquisitor.get(slot);
			for (GearRequirement requirement : requirements)
			{
				if (requirement.restricts(slot)
					&& !requirement.matchesForSlot(slot, setPiece.name)) return;
			}
			GearRecommendation current = selected.get(slot);
			if (current != null) currentScore += current.getScore();
			setScore += contextualScore(
				strategy, weaponName, setPiece.name, slot,
				setPiece.stats, setPiece.score);
		}
		if (setScore <= currentScore) return;
		for (EquipmentInventorySlot slot : INQUISITOR_SET_SLOTS)
		{
			selected.put(slot, contextualRecommendation(
				inquisitor.get(slot), rank, strategy, weaponName));
		}
	}

	static Map<EquipmentInventorySlot, BankEquipment> selectWeaponPair(
		int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates)
	{
		return selectWeaponPair(rank, candidates, null);
	}

	private static Map<EquipmentInventorySlot, BankEquipment> selectWeaponPair(
		int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy)
	{
		List<BankEquipment> weapons = preferredPinnedChoices(rankedChoices(
			candidates.getOrDefault(EquipmentInventorySlot.WEAPON, Collections.emptyList()),
			rank));
		List<BankEquipment> shields = preferredPinnedChoices(rankedChoices(
			candidates.getOrDefault(EquipmentInventorySlot.SHIELD, Collections.emptyList()),
			rank));
		BankEquipment bestWeapon = null;
		BankEquipment bestShield = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (BankEquipment weapon : weapons)
		{
			BankEquipment shield = weapon.stats.isTwoHanded() || shields.isEmpty()
				? null
				: shields.get(0);
			double pairScore = weapon.score + (shield == null ? 0 : shield.score);
			if (strategy != null)
			{
				pairScore += contextualSupportingScore(rank, candidates, strategy, weapon);
			}
			if (pairScore > bestScore
				|| (pairScore == bestScore
					&& (bestWeapon == null || weapon.score > bestWeapon.score)))
			{
				bestWeapon = weapon;
				bestShield = shield;
				bestScore = pairScore;
			}
		}

		EnumMap<EquipmentInventorySlot, BankEquipment> result =
			new EnumMap<>(EquipmentInventorySlot.class);
		if (bestWeapon != null) result.put(EquipmentInventorySlot.WEAPON, bestWeapon);
		if (bestShield != null) result.put(EquipmentInventorySlot.SHIELD, bestShield);
		return result;
	}

	private static double contextualSupportingScore(
		int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		BankEquipment weapon)
	{
		double score = 0;
		String weaponName = NameMatcher.normalize(weapon.name);
		EnumMap<EquipmentInventorySlot, BankEquipment> selected =
			new EnumMap<>(EquipmentInventorySlot.class);
		for (EquipmentInventorySlot slot : SUPPORTED_SLOTS)
		{
			if (slot == EquipmentInventorySlot.WEAPON || slot == EquipmentInventorySlot.SHIELD) continue;
			List<BankEquipment> choices = candidates.getOrDefault(slot, Collections.emptyList());
			if (slot == EquipmentInventorySlot.AMMO && strategy.getCombatStyle() == CombatStyle.RANGED)
			{
				if (usesNoAmmoSlot(weaponName)) continue;
				choices = compatibleAmmo(choices, weaponName);
			}
			choices = contextualCandidates(choices, strategy, weaponName);
			if (rank <= choices.size())
			{
				BankEquipment item = choices.get(rank - 1);
				selected.put(slot, item);
				score += contextualScore(
					strategy, weaponName, item.name, item.slot, item.stats, item.score);
			}
		}
		score += inquisitorSetUpgradeScore(
			rank, candidates, strategy, weaponName, selected);
		return score;
	}

	private static double inquisitorSetUpgradeScore(
		int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		String selectedWeaponName,
		Map<EquipmentInventorySlot, BankEquipment> selected)
	{
		Map<EquipmentInventorySlot, BankEquipment> inquisitor =
			inquisitorSet(rank, candidates, strategy, selectedWeaponName);
		if (inquisitor.isEmpty()) return 0;

		double currentScore = 0;
		double setScore = inquisitorFullSetBonus();
		for (EquipmentInventorySlot slot : INQUISITOR_SET_SLOTS)
		{
			BankEquipment current = selected.get(slot);
			if (current != null)
			{
				currentScore += contextualScore(
					strategy, selectedWeaponName, current.name, slot,
					current.stats, current.score);
			}
			BankEquipment setPiece = inquisitor.get(slot);
			setScore += contextualScore(
				strategy, selectedWeaponName, setPiece.name, slot,
				setPiece.stats, setPiece.score);
		}
		return Math.max(0, setScore - currentScore);
	}

	private static Map<EquipmentInventorySlot, BankEquipment> inquisitorSet(
		int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		String selectedWeaponName)
	{
		if (!supportsInquisitorFullSet(strategy, selectedWeaponName))
		{
			return Collections.emptyMap();
		}
		EnumMap<EquipmentInventorySlot, BankEquipment> result =
			new EnumMap<>(EquipmentInventorySlot.class);
		for (EquipmentInventorySlot slot : INQUISITOR_SET_SLOTS)
		{
			List<BankEquipment> pieces = new ArrayList<>();
			for (BankEquipment item : candidates.getOrDefault(slot, Collections.emptyList()))
			{
				if (isInquisitorArmour(NameMatcher.normalize(item.name))) pieces.add(item);
			}
			pieces = contextualCandidates(pieces, strategy, selectedWeaponName);
			if (rank > pieces.size()) return Collections.emptyMap();
			result.put(slot, pieces.get(rank - 1));
		}
		return result;
	}

	private static boolean supportsInquisitorFullSet(
		GearStrategy strategy,
		String selectedWeaponName)
	{
		String weapon = NameMatcher.normalize(selectedWeaponName);
		return strategy.getCombatStyle() == CombatStyle.MELEE
			&& strategy.getAttackType() == AttackType.CRUSH
			&& WeaponCombatRules.supportsAttackType(weapon, AttackType.CRUSH)
			&& !weapon.contains("inquisitor's mace");
	}

	private static double inquisitorFullSetBonus()
	{
		return (WEAPON_SHARED_ACCURACY_BASE + WEAPON_SHARED_DAMAGE_BASE) * 0.01;
	}

	private static List<BankEquipment> contextualCandidates(
		List<BankEquipment> candidates,
		GearStrategy strategy,
		String selectedWeaponName)
	{
		List<BankEquipment> ranked = new ArrayList<>(candidates);
		ranked.sort(Comparator
			.comparingDouble((BankEquipment item) -> contextualScore(
				strategy, selectedWeaponName, item.name, item.slot, item.stats, item.score))
			.reversed()
			.thenComparing(item -> item.name));
		return ranked;
	}

	static double contextualScore(
		GearStrategy strategy,
		String selectedWeaponName,
		String itemName,
		EquipmentInventorySlot slot,
		ItemEquipmentStats stats,
		double baseScore)
	{
		String weapon = NameMatcher.normalize(selectedWeaponName);
		String item = NameMatcher.normalize(itemName);
		double score = baseScore;

		if (strategy.getCombatStyle() == CombatStyle.RANGED
			&& weapon.contains("eclipse atlatl")
			&& slot != EquipmentInventorySlot.WEAPON)
		{
			// Atlatl armour damage comes from Melee Strength, not Ranged Strength.
			score += (stats.getStr() - stats.getRstr()) * 5.0;
		}

		if (isCrystalBow(weapon) && isCrystalArmour(item))
		{
			double accuracyPercent;
			double damagePercent;
			if (item.contains("crystal body"))
			{
				accuracyPercent = 0.15;
				damagePercent = 0.075;
			}
			else if (item.contains("crystal legs"))
			{
				accuracyPercent = 0.10;
				damagePercent = 0.05;
			}
			else
			{
				accuracyPercent = 0.05;
				damagePercent = 0.025;
			}
			score += WEAPON_SHARED_ACCURACY_BASE * accuracyPercent
				+ WEAPON_SHARED_DAMAGE_BASE * damagePercent;
		}

		if (strategy.getCombatStyle() == CombatStyle.MELEE
			&& strategy.getAttackType() == AttackType.CRUSH
			&& WeaponCombatRules.supportsAttackType(weapon, AttackType.CRUSH)
			&& isInquisitorArmour(item))
		{
			double percent = weapon.contains("inquisitor's mace") ? 0.025 : 0.005;
			score += (WEAPON_SHARED_ACCURACY_BASE + WEAPON_SHARED_DAMAGE_BASE) * percent;
		}

		return score;
	}

	private static String contextualReason(
		GearStrategy strategy,
		String selectedWeaponName,
		String itemName,
		EquipmentInventorySlot slot,
		ItemEquipmentStats stats)
	{
		String weapon = NameMatcher.normalize(selectedWeaponName);
		String item = NameMatcher.normalize(itemName);
		if (strategy.getCombatStyle() == CombatStyle.RANGED
			&& weapon.contains("eclipse atlatl")
			&& slot != EquipmentInventorySlot.WEAPON
			&& stats.getStr() != stats.getRstr())
		{
			return "Eclipse atlatl uses Melee Strength";
		}
		if (isCrystalBow(weapon) && isCrystalArmour(item))
		{
			return "Crystal-bow armour accuracy/damage effect";
		}
		if (strategy.getCombatStyle() == CombatStyle.MELEE
			&& strategy.getAttackType() == AttackType.CRUSH
			&& WeaponCombatRules.supportsAttackType(weapon, AttackType.CRUSH)
			&& isInquisitorArmour(item))
		{
			return "Inquisitor Crush accuracy/damage effect";
		}
		return null;
	}

	private static boolean isCrystalBow(String normalizedName)
	{
		return normalizedName.contains("bow of faerdhinen")
			|| normalizedName.contains("crystal bow");
	}

	private static boolean isCrystalArmour(String normalizedName)
	{
		return normalizedName.contains("crystal helm")
			|| normalizedName.contains("crystal body")
			|| normalizedName.contains("crystal legs");
	}

	private static boolean isInquisitorArmour(String normalizedName)
	{
		return normalizedName.contains("inquisitor's great helm")
			|| normalizedName.contains("inquisitor's hauberk")
			|| normalizedName.contains("inquisitor's plateskirt");
	}

	List<Map<EquipmentInventorySlot, GearRecommendation>> buildCoherentLoadouts(
		int tiersWanted,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		List<GearRequirement> requirements,
		Set<String> pinned,
		boolean lowRiskMode,
		int riskCapGp)
	{
		List<Map<EquipmentInventorySlot, GearRecommendation>> result = new ArrayList<>();
		Map<EquipmentInventorySlot, GearRecommendation> tierOne =
			buildLoadout(1, candidates, strategy, requirements, lowRiskMode, riskCapGp);
		if (tierOne.isEmpty()) return result;

		result.add(rerankLoadout(tierOne, 1));
		long riskCeiling = lowRiskMode
			? Math.max((long) riskCapGp, totalRecommendationGuidePrice(tierOne))
			: Long.MAX_VALUE;
		Set<String> seen = new HashSet<>();
		seen.add(loadoutSignature(tierOne));
		PriorityQueue<LoadoutCandidate> queue = new PriorityQueue<>(
			Comparator.comparingDouble((LoadoutCandidate value) -> value.score).reversed()
				.thenComparingLong(value -> value.guidePrice));
		enqueueLoadoutNeighbors(
			tierOne, candidates, strategy, requirements, pinned,
			lowRiskMode, riskCeiling, seen, queue);

		while (result.size() < Math.max(1, tiersWanted) && !queue.isEmpty())
		{
			LoadoutCandidate next = queue.poll();
			int rank = result.size() + 1;
			result.add(rerankLoadout(next.items, rank));
			enqueueLoadoutNeighbors(
				next.items, candidates, strategy, requirements, pinned,
				lowRiskMode, riskCeiling, seen, queue);
		}
		return result;
	}

	private void enqueueLoadoutNeighbors(
		Map<EquipmentInventorySlot, GearRecommendation> current,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		List<GearRequirement> requirements,
		Set<String> pinned,
		boolean lowRiskMode,
		long riskCeiling,
		Set<String> seen,
		PriorityQueue<LoadoutCandidate> queue)
	{
		for (EquipmentInventorySlot slot : LOADOUT_SLOT_ORDER)
		{
			GearRecommendation existing = current.get(slot);
			if (existing == null || matchesAnyPreference(existing.getItemName(), pinned)) continue;

			List<BankEquipment> slotCandidates =
				candidates.getOrDefault(slot, Collections.emptyList());
			GearRecommendation currentWeapon = current.get(EquipmentInventorySlot.WEAPON);
			String currentWeaponName = currentWeapon == null
				? ""
				: NameMatcher.normalize(currentWeapon.getItemName());
			if (slot == EquipmentInventorySlot.AMMO
				&& strategy.getCombatStyle() == CombatStyle.RANGED)
			{
				GearRecommendation weapon = current.get(EquipmentInventorySlot.WEAPON);
				if (weapon == null || usesNoAmmoSlot(NameMatcher.normalize(weapon.getItemName()))) continue;
				slotCandidates = compatibleAmmo(
					slotCandidates, NameMatcher.normalize(weapon.getItemName()));
			}
			else if (slot != EquipmentInventorySlot.WEAPON
				&& slot != EquipmentInventorySlot.SHIELD)
			{
				slotCandidates = contextualCandidates(
					slotCandidates, strategy, currentWeaponName);
			}

			int currentIndex = candidateIndex(slotCandidates, existing.getCanonicalItemId());
			if (currentIndex < 0) continue;
			// Tier 1 can legitimately use a lower individually ranked one-handed
			// weapon because its off-hand makes the package stronger. Search the
			// complete weapon list so the individually stronger two-hander still
			// appears as a coherent alternative tier.
			int firstCandidate = slot == EquipmentInventorySlot.WEAPON
				? 0
				: currentIndex + 1;
			for (int candidateIndex = firstCandidate;
				candidateIndex < slotCandidates.size();
				candidateIndex++)
			{
				if (candidateIndex == currentIndex) continue;
				EnumMap<EquipmentInventorySlot, GearRecommendation> neighbor =
					new EnumMap<>(EquipmentInventorySlot.class);
				neighbor.putAll(current);
				BankEquipment replacement = slotCandidates.get(candidateIndex);
				if (slot == EquipmentInventorySlot.WEAPON
					|| slot == EquipmentInventorySlot.SHIELD
					|| slot == EquipmentInventorySlot.AMMO)
				{
					neighbor.put(slot, recommendation(replacement, 1, strategy));
				}
				else
				{
					neighbor.put(slot, contextualRecommendation(
						replacement, 1, strategy, currentWeaponName));
				}
				if (slot == EquipmentInventorySlot.WEAPON)
				{
					rebuildWeaponDependentSlots(
						neighbor, candidates, strategy, requirements, pinned,
						lowRiskMode);
				}
				normalizeLoadout(neighbor, candidates, strategy, requirements);
				if (slot == EquipmentInventorySlot.WEAPON && !lowRiskMode)
				{
					applyInquisitorSetIfBetter(
						neighbor, candidates, strategy, requirements, 1);
				}
				ensureMinimumEquippedWeight(neighbor, candidates, strategy, requirements, 1);
				if (!isCoherentLoadout(neighbor, strategy, requirements)) continue;

				long guidePrice = lowRiskMode
					? totalRecommendationGuidePrice(neighbor)
					: 0;
				if (lowRiskMode && guidePrice > riskCeiling) continue;
				String signature = loadoutSignature(neighbor);
				if (!seen.add(signature)) continue;
				queue.add(new LoadoutCandidate(
					neighbor, loadoutScore(neighbor, strategy), guidePrice));
				break;
			}
		}
	}

	private void rebuildWeaponDependentSlots(
		Map<EquipmentInventorySlot, GearRecommendation> selected,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		List<GearRequirement> requirements,
		Set<String> pinned,
		boolean preserveRiskBudgetChoices)
	{
		GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
		if (weapon == null) return;
		String weaponName = NameMatcher.normalize(weapon.getItemName());
		for (EquipmentInventorySlot slot : SUPPORTED_SLOTS)
		{
			if (slot == EquipmentInventorySlot.WEAPON
				|| slot == EquipmentInventorySlot.SHIELD
				|| slot == EquipmentInventorySlot.AMMO) continue;
			List<BankEquipment> ranked = contextualCandidates(
				candidates.getOrDefault(slot, Collections.emptyList()),
				strategy,
				weaponName);
			if (ranked.isEmpty()) continue;

			GearRecommendation existing = selected.get(slot);
			BankEquipment choice = null;
			boolean preserveExisting = existing != null
				&& (preserveRiskBudgetChoices
					|| matchesAnyPreference(existing.getItemName(), pinned));
			if (existing != null)
			{
				for (GearRequirement requirement : requirements)
				{
					if (requirement.restricts(slot)) preserveExisting = true;
				}
			}
			if (preserveExisting)
			{
				for (BankEquipment candidate : ranked)
				{
					if (candidate.canonicalItemId == existing.getCanonicalItemId())
					{
						choice = candidate;
						break;
					}
				}
			}
			if (choice == null) choice = ranked.get(0);
			selected.put(slot, contextualRecommendation(
				choice, 1, strategy, weaponName));
		}
	}

	private void normalizeLoadout(
		Map<EquipmentInventorySlot, GearRecommendation> selected,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		List<GearRequirement> requirements)
	{
		GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
		if (weapon != null && weapon.isTwoHanded())
		{
			selected.remove(EquipmentInventorySlot.SHIELD);
		}
		else if (weapon != null && !selected.containsKey(EquipmentInventorySlot.SHIELD))
		{
			List<BankEquipment> shields =
				candidates.getOrDefault(EquipmentInventorySlot.SHIELD, Collections.emptyList());
			if (!shields.isEmpty())
			{
				selected.put(
					EquipmentInventorySlot.SHIELD,
					recommendation(shields.get(0), 1, strategy));
			}
		}

		if (weapon != null && strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			String weaponName = NameMatcher.normalize(weapon.getItemName());
			if (usesNoAmmoSlot(weaponName))
			{
				BankEquipment blessing = nthPrayerBlessing(
					candidates.getOrDefault(EquipmentInventorySlot.AMMO, Collections.emptyList()), 1);
				if (blessing == null) selected.remove(EquipmentInventorySlot.AMMO);
				else selected.put(EquipmentInventorySlot.AMMO, recommendation(blessing, 1, strategy));
			}
			else
			{
				GearRecommendation ammo = selected.get(EquipmentInventorySlot.AMMO);
				if (ammo == null || !isCompatibleAmmo(ammo.getItemName(), weaponName))
				{
					List<BankEquipment> compatible = compatibleAmmo(
						candidates.getOrDefault(
							EquipmentInventorySlot.AMMO, Collections.emptyList()),
						weaponName);
					if (compatible.isEmpty()) selected.remove(EquipmentInventorySlot.AMMO);
					else selected.put(
						EquipmentInventorySlot.AMMO,
						recommendation(compatible.get(0), 1, strategy));
				}
			}
		}

		for (GearRequirement requirement : requirements)
		{
			if (requirement.isSatisfied(selected)) continue;
			for (GearRequirement.Option option : requirement.getOptions())
			{
				for (BankEquipment item :
					candidates.getOrDefault(option.getSlot(), Collections.emptyList()))
				{
					if (option.matches(item.name))
					{
						selected.put(
							option.getSlot(),
							recommendation(item, 1, strategy));
						break;
					}
				}
				if (requirement.isSatisfied(selected)) break;
			}
		}
	}

	private static boolean isCoherentLoadout(
		Map<EquipmentInventorySlot, GearRecommendation> selected,
		GearStrategy strategy,
		List<GearRequirement> requirements)
	{
		for (GearRequirement requirement : requirements)
		{
			if (!requirement.isSatisfied(selected)) return false;
		}
		GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
		if (weapon == null) return false;
		if (weapon.isTwoHanded() && selected.containsKey(EquipmentInventorySlot.SHIELD)) return false;
		if (strategy.getMinimumEquippedWeightKg() > 0
			&& totalEquippedWeight(selected) + 0.0001 < strategy.getMinimumEquippedWeightKg()) return false;
		if (strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			String weaponName = NameMatcher.normalize(weapon.getItemName());
			if (!usesNoAmmoSlot(weaponName))
			{
				GearRecommendation ammo = selected.get(EquipmentInventorySlot.AMMO);
				if (ammo == null || !isCompatibleAmmo(ammo.getItemName(), weaponName)) return false;
			}
		}
		return true;
	}

	private static boolean isCompatibleAmmo(String ammoName, String normalizedWeaponName)
	{
		return RangedAmmoPolicy.isCompatible(normalizedWeaponName, ammoName);
	}

	private static int candidateIndex(List<BankEquipment> candidates, int canonicalItemId)
	{
		for (int index = 0; index < candidates.size(); index++)
		{
			if (candidates.get(index).canonicalItemId == canonicalItemId) return index;
		}
		return -1;
	}

	private static double loadoutScore(
		Map<EquipmentInventorySlot, GearRecommendation> loadout,
		GearStrategy strategy)
	{
		double total = 0;
		for (GearRecommendation item : loadout.values()) total += item.getScore();
		GearRecommendation weapon = loadout.get(EquipmentInventorySlot.WEAPON);
		if (weapon != null
			&& supportsInquisitorFullSet(strategy, weapon.getItemName())
			&& hasCompleteInquisitorSet(loadout))
		{
			total += inquisitorFullSetBonus();
		}
		return total;
	}

	private static boolean hasCompleteInquisitorSet(
		Map<EquipmentInventorySlot, GearRecommendation> loadout)
	{
		for (EquipmentInventorySlot slot : INQUISITOR_SET_SLOTS)
		{
			GearRecommendation item = loadout.get(slot);
			if (item == null
				|| !isInquisitorArmour(NameMatcher.normalize(item.getItemName()))) return false;
		}
		return true;
	}

	private static String loadoutSignature(
		Map<EquipmentInventorySlot, GearRecommendation> loadout)
	{
		StringBuilder signature = new StringBuilder();
		for (EquipmentInventorySlot slot : LOADOUT_SLOT_ORDER)
		{
			GearRecommendation item = loadout.get(slot);
			if (item != null)
			{
				signature.append(slot.ordinal())
					.append(':')
					.append(item.getCanonicalItemId())
					.append(';');
			}
		}
		return signature.toString();
	}

	private static Map<EquipmentInventorySlot, GearRecommendation> rerankLoadout(
		Map<EquipmentInventorySlot, GearRecommendation> loadout,
		int rank)
	{
		EnumMap<EquipmentInventorySlot, GearRecommendation> reranked =
			new EnumMap<>(EquipmentInventorySlot.class);
		for (Map.Entry<EquipmentInventorySlot, GearRecommendation> entry : loadout.entrySet())
		{
			reranked.put(entry.getKey(), entry.getValue().withRank(rank));
		}
		return reranked;
	}

	static Map<EquipmentInventorySlot, BankEquipment> selectRiskBudgetItems(
		int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		int riskCapGp)
	{
		List<BankEquipment> weaponChoices = rankedChoices(
			candidates.getOrDefault(EquipmentInventorySlot.WEAPON, Collections.emptyList()), rank);
		weaponChoices = preferredPinnedChoices(weaponChoices);
		if (weaponChoices.isEmpty())
		{
			return buildRiskPlan(null, rank, candidates, strategy, riskCapGp).items;
		}

		RiskPlan best = null;
		for (BankEquipment weapon : weaponChoices)
		{
			RiskPlan plan = buildRiskPlan(weapon, rank, candidates, strategy, riskCapGp);
			if (betterRiskPlan(plan, best, riskCapGp))
			{
				best = plan;
			}
		}
		return best == null ? Collections.emptyMap() : best.items;
	}

	private static RiskPlan buildRiskPlan(
		BankEquipment weapon,
		int rank,
		Map<EquipmentInventorySlot, List<BankEquipment>> candidates,
		GearStrategy strategy,
		int riskCapGp)
	{
		EnumMap<EquipmentInventorySlot, List<BankEquipment>> choices =
			new EnumMap<>(EquipmentInventorySlot.class);
		if (weapon != null)
		{
			choices.put(EquipmentInventorySlot.WEAPON, Collections.singletonList(weapon));
		}

		for (EquipmentInventorySlot slot : LOADOUT_SLOT_ORDER)
		{
			if (slot == EquipmentInventorySlot.WEAPON) continue;
			if (slot == EquipmentInventorySlot.SHIELD && weapon != null && weapon.stats.isTwoHanded()) continue;

			List<BankEquipment> slotChoices;
			if (slot == EquipmentInventorySlot.AMMO
				&& weapon != null
				&& strategy.getCombatStyle() == CombatStyle.RANGED)
			{
				String weaponName = NameMatcher.normalize(weapon.name);
				if (usesNoAmmoSlot(weaponName)) continue;
				slotChoices = rankedChoices(
					compatibleAmmo(candidates.getOrDefault(slot, Collections.emptyList()), weaponName),
					rank);
			}
			else
			{
				List<BankEquipment> contextual = contextualCandidates(
					candidates.getOrDefault(slot, Collections.emptyList()),
					strategy,
					weapon == null ? "" : weapon.name);
				slotChoices = rankedChoices(contextual, rank);
			}

			slotChoices = preferredPinnedChoices(slotChoices);
			slotChoices = withContextScores(
				slotChoices, strategy, weapon == null ? "" : weapon.name);
			if (!slotChoices.isEmpty())
			{
				choices.put(slot, slotChoices);
			}
		}
		return chooseWithinRiskBudget(choices, riskCapGp);
	}

	private static List<BankEquipment> withContextScores(
		List<BankEquipment> choices,
		GearStrategy strategy,
		String selectedWeaponName)
	{
		List<BankEquipment> adjusted = new ArrayList<>();
		for (BankEquipment item : choices)
		{
			double contextualScore = contextualScore(
				strategy, selectedWeaponName, item.name, item.slot, item.stats, item.score);
			if (contextualScore == item.score)
			{
				adjusted.add(item);
				continue;
			}
			BankEquipment copy = new BankEquipment(
				item.itemId, item.canonicalItemId, item.name, item.slot,
				item.stats, item.banked, item.packed, item.weightKg);
			copy.score = contextualScore;
			copy.guidePrice = item.guidePrice;
			copy.pinned = item.pinned;
			adjusted.add(copy);
		}
		return adjusted;
	}

	private static RiskPlan chooseWithinRiskBudget(
		Map<EquipmentInventorySlot, List<BankEquipment>> choices,
		int riskCapGp)
	{
		EnumMap<EquipmentInventorySlot, BankEquipment> selected =
			new EnumMap<>(EquipmentInventorySlot.class);
		long totalPrice = 0;
		double totalScore = 0;
		for (EquipmentInventorySlot slot : LOADOUT_SLOT_ORDER)
		{
			List<BankEquipment> slotChoices = choices.getOrDefault(slot, Collections.emptyList());
			if (slotChoices.isEmpty()) continue;
			BankEquipment base = cheapestChoice(slotChoices);
			selected.put(slot, base);
			totalPrice += base.guidePrice;
			totalScore += base.score;
		}

		if (totalPrice <= riskCapGp)
		{
			while (true)
			{
				RiskUpgrade bestUpgrade = null;
				for (Map.Entry<EquipmentInventorySlot, BankEquipment> entry : selected.entrySet())
				{
					List<BankEquipment> slotChoices =
						choices.getOrDefault(entry.getKey(), Collections.emptyList());
					if (slotChoices.size() <= 1 || entry.getValue().pinned) continue;
					for (BankEquipment candidate : slotChoices)
					{
						long addedPrice = (long) candidate.guidePrice - entry.getValue().guidePrice;
						double addedScore = candidate.score - entry.getValue().score;
						if (addedScore <= 0 || totalPrice + addedPrice > riskCapGp) continue;
						RiskUpgrade upgrade = new RiskUpgrade(
							entry.getKey(), candidate, addedPrice, addedScore);
						if (upgrade.isBetterThan(bestUpgrade))
						{
							bestUpgrade = upgrade;
						}
					}
				}
				if (bestUpgrade == null) break;
				BankEquipment previous = selected.put(bestUpgrade.slot, bestUpgrade.candidate);
				totalPrice += (long) bestUpgrade.candidate.guidePrice - previous.guidePrice;
				totalScore += bestUpgrade.candidate.score - previous.score;
			}
		}
		return new RiskPlan(selected, totalPrice, totalScore);
	}

	private static List<BankEquipment> rankedChoices(List<BankEquipment> values, int rank)
	{
		if (values == null || values.isEmpty()) return Collections.emptyList();
		int start = Math.max(0, rank - 1);
		if (start >= values.size()) return Collections.emptyList();
		return new ArrayList<>(values.subList(start, values.size()));
	}

	private static List<BankEquipment> preferredPinnedChoices(List<BankEquipment> values)
	{
		for (BankEquipment value : values)
		{
			if (value.pinned)
			{
				return Collections.singletonList(value);
			}
		}
		return values;
	}

	private static BankEquipment cheapestChoice(List<BankEquipment> values)
	{
		return values.stream()
			.min(Comparator.comparingInt((BankEquipment item) -> item.guidePrice)
				.thenComparing(Comparator.comparingDouble((BankEquipment item) -> item.score).reversed()))
			.orElseThrow();
	}

	private static boolean betterRiskPlan(RiskPlan candidate, RiskPlan current, int riskCapGp)
	{
		if (candidate == null) return false;
		if (current == null) return true;
		boolean candidateWithinCap = candidate.totalPrice <= riskCapGp;
		boolean currentWithinCap = current.totalPrice <= riskCapGp;
		if (candidateWithinCap != currentWithinCap) return candidateWithinCap;
		if (candidateWithinCap)
		{
			return candidate.totalScore > current.totalScore;
		}
		return candidate.totalPrice < current.totalPrice
			|| (candidate.totalPrice == current.totalPrice
				&& candidate.totalScore > current.totalScore);
	}

	private static List<BankEquipment> compatibleAmmo(List<BankEquipment> ammo, String weapon)
	{
		List<BankEquipment> compatible = new ArrayList<>();
		for (BankEquipment candidate : ammo)
		{
			if (RangedAmmoPolicy.isCompatible(weapon, candidate.name)) compatible.add(candidate);
		}
		return compatible;
	}

	static long totalGuidePrice(Map<EquipmentInventorySlot, BankEquipment> selected)
	{
		long total = 0;
		for (BankEquipment item : selected.values()) total += Math.max(0, item.guidePrice);
		return total;
	}

	private int totalRecommendationGuidePrice(
		Map<EquipmentInventorySlot, GearRecommendation> selected)
	{
		long total = 0;
		for (GearRecommendation item : selected.values())
		{
			total += Math.max(0, itemManager.getItemPrice(item.getItemId()));
		}
		return (int) Math.min(Integer.MAX_VALUE, total);
	}

	private GearRecommendation recommendation(BankEquipment i, int rank, GearStrategy strategy)
	{
		return recommendation(i, rank, strategy, i.score, null);
	}

	private GearRecommendation contextualRecommendation(
		BankEquipment item,
		int rank,
		GearStrategy strategy,
		String selectedWeaponName)
	{
		double adjustedScore = contextualScore(
			strategy, selectedWeaponName, item.name, item.slot, item.stats, item.score);
		return recommendation(
			item,
			rank,
			strategy,
			adjustedScore,
			contextualReason(strategy, selectedWeaponName, item.name, item.slot, item.stats));
	}

	private GearRecommendation recommendation(
		BankEquipment item,
		int rank,
		GearStrategy strategy,
		double score,
		String contextualReason)
	{
		String reason = explain(strategy, item.name, item.slot, item.stats);
		if (contextualReason != null && !contextualReason.isEmpty())
		{
			reason = contextualReason + ", " + reason;
		}
		return GearRecommendation.builder().itemId(item.itemId).canonicalItemId(item.canonicalItemId)
			.itemName(item.name).slot(item.slot).score(score).rank(rank).twoHanded(item.stats.isTwoHanded())
			.weightKg(item.weightKg).reason(reason).packed(item.packed).banked(item.banked).build();
	}

	private ReadinessReport readiness(Map<EquipmentInventorySlot, GearRecommendation> selected,
		List<GearRequirement> requirements, List<SupplyRecommendation> supplies,
		GearStrategy strategy, int magicLevel, boolean ancientSpellbookActive,
		boolean arceuusSpellbookActive, boolean loadedDizanasQuiver)
	{
		List<String> missing = new ArrayList<>();
		boolean protection = true;
		for (GearRequirement req : requirements)
		{
			if (!req.isSatisfied(selected)) { protection = false; missing.add(req.getLabel()); }
		}
		double selectedWeight = totalEquippedWeight(selected);
		if (strategy.getMinimumEquippedWeightKg() > 0
			&& selectedWeight + 0.0001 < strategy.getMinimumEquippedWeightKg())
		{
			protection = false;
			missing.add(String.format(Locale.ENGLISH,
				"Equipped weight %.0f kg required (%.1f kg selected)",
				strategy.getMinimumEquippedWeightKg(), selectedWeight));
		}
		GearRecommendation weapon = selected.get(EquipmentInventorySlot.WEAPON);
		boolean ammoReady = weapon != null;
		if (weapon == null)
		{
			missing.add("Compatible " + strategy.getCombatStyle().name().toLowerCase(Locale.ENGLISH) + " weapon");
		}
		if (weapon != null && strategy.getCombatStyle() == CombatStyle.RANGED && !usesNoAmmoSlot(NameMatcher.normalize(weapon.getItemName())))
		{
			ammoReady = loadedDizanasQuiver
				|| selected.containsKey(EquipmentInventorySlot.AMMO);
			if (!ammoReady) missing.add("Compatible ammunition");
		}
		int packedGear = 0;
		for (GearRecommendation r : selected.values()) if (r.isPacked()) packedGear++;
		int gearTotal = selected.size();
		int suppliesPacked = 0, suppliesTotal = 0;
		boolean dragonfireShieldReady = hasDragonfireProtection(selected);
		for (SupplyRecommendation s : supplies)
		{
			if (!s.isEnabledForTrip())
			{
				continue;
			}
			// Cannon components are ground equipment and are rendered inside the Tier 1
			// loadout. Count the four required parts as gear readiness; cannonballs
			// remain a trip supply. This keeps the readiness strip consistent with
			// what the player sees in the loadout section.
			if ("Cannon setup".equals(s.getCategory()))
			{
				gearTotal++;
				if (s.getStatus().isPacked()) packedGear++;
			}
			else
			{
				suppliesTotal++;
				if (s.getStatus().isPacked() && s.hasRecommendedQuantityPacked()) suppliesPacked++;
			}

			boolean requiredHere = s.isRequired()
				&& !("Antifire".equals(s.getCategory()) && dragonfireShieldReady);
			if (requiredHere && (!s.getStatus().isPacked() || !s.hasRecommendedQuantityPacked()))
			{
				missing.add(s.getStatus().isBanked()
					? "Pack " + (s.hasQuantityTarget() ? s.getQuantityStillNeeded() + " " : "")
						+ s.getItemName()
					: s.getCategory() + ": " + s.getItemName());
			}
		}
		String spell = "Not required";
		if (strategy != null && strategy.isAncientAoe())
		{
			String highest = highestAncientAoe(magicLevel);
			if (magicLevel < 62)
			{
				spell = highest;
				missing.add("Ancient AoE spell level (62+ Magic)");
			}
			else if (!ancientSpellbookActive)
			{
				spell = highest + " • Ancient spellbook inactive";
				missing.add("Switch to the Ancient Magicks spellbook");
			}
			else
			{
				spell = highest + " • spellbook ready";
			}
		}
		else if (supplies.stream().anyMatch(s -> "Thrall book".equals(s.getCategory())))
		{
			spell = arceuusSpellbookActive
				? "Thralls • Arceuus spellbook ready"
				: "Thralls • Arceuus spellbook inactive";
			if (!arceuusSpellbookActive)
			{
				missing.add("Switch to the Arceuus spellbook for Thralls");
			}
		}
		return new ReadinessReport(packedGear, gearTotal, protection, ammoReady, spell,
			suppliesPacked, suppliesTotal, missing);
	}

	static List<SupplyRecommendation> withoutCategory(List<SupplyRecommendation> supplies, String category)
	{
		if (supplies == null || supplies.isEmpty()) return Collections.emptyList();
		List<SupplyRecommendation> filtered = new ArrayList<>();
		for (SupplyRecommendation supply : supplies)
		{
			if (!category.equals(supply.getCategory())) filtered.add(supply);
		}
		return filtered;
	}

	private static boolean hasDragonfireProtection(Map<EquipmentInventorySlot, GearRecommendation> selected)
	{
		GearRecommendation shield = selected.get(EquipmentInventorySlot.SHIELD);
		if (shield == null) return false;
		String name = NameMatcher.normalize(shield.getItemName());
		return name.contains("anti dragon shield")
			|| name.contains("dragonfire shield")
			|| name.contains("dragonfire ward");
	}

	static String highestAncientAoe(int level)
	{
		if (level >= 94) return "Ice Barrage";
		if (level >= 92) return "Blood Barrage";
		if (level >= 88) return "Shadow Barrage";
		if (level >= 86) return "Smoke Barrage";
		if (level >= 70) return "Ice Burst";
		if (level >= 68) return "Blood Burst";
		if (level >= 64) return "Shadow Burst";
		if (level >= 62) return "Smoke Burst";
		return "Ancient AoE unavailable";
	}

	static double scoreStats(GearStrategy strategy, String itemName, EquipmentInventorySlot slot, ItemEquipmentStats stats)
	{
		return scoreStats(strategy, itemName, slot, stats, GearPriority.BALANCED);
	}

	static double scoreStats(GearStrategy strategy, String itemName, EquipmentInventorySlot slot,
		ItemEquipmentStats stats, GearPriority gearPriority)
	{
		double damage;
		double accuracy;
		String normalizedItemName = NameMatcher.normalize(itemName);
		boolean prayerFirst = gearPriority == GearPriority.PRAYER_FIRST;
		boolean defenceFirst = gearPriority == GearPriority.DEFENCE_FIRST;

		// Prayer First changes sustain gear, not the combat-optimal weapon.
		double prayerWeight = prayerFirst && slot != EquipmentInventorySlot.WEAPON
			? 200.0
			: strategy.getPrayerWeight();

		double utility = stats.getPrayer() * prayerWeight;
		if (slot != EquipmentInventorySlot.WEAPON)
		{
			double defenceWeight = defenceFirst ? 5.0 : BASE_DEFENCE_WEIGHT;
			utility += (stats.getDstab() + stats.getDslash() + stats.getDcrush()
				+ stats.getDrange() + stats.getDmagic()) * defenceWeight;
			utility += stats.getDmagic() * strategy.getMagicDefenceWeight();
		}

		switch (strategy.getCombatStyle())
		{
			case MAGIC:
				damage = effectiveMagicDamageBonus(strategy, normalizedItemName, stats) * 25.0;
				accuracy = stats.getAmagic() * .28;
				break;
			case RANGED:
				// Eclipse atlatl ranged damage scales from Melee Strength.
				damage = ((slot == EquipmentInventorySlot.WEAPON
					&& normalizedItemName.contains("eclipse atlatl")
					? stats.getStr()
					: stats.getRstr())
					+ (slot == EquipmentInventorySlot.WEAPON
						? WeaponCombatRules.intrinsicRangedStrength(itemName)
						: 0)) * 5.0;
				accuracy = stats.getArange() * .32;
				break;
			default:
				damage = stats.getStr() * 5.0;
				accuracy = attackBonus(strategy.getAttackType(), stats) * .34;
				break;
		}

		if (slot == EquipmentInventorySlot.WEAPON)
		{
			double damageMultiplier = WeaponCombatRules.damageMultiplier(strategy, itemName)
				* WeaponCombatRules.intrinsicDamageMultiplier(strategy, itemName);
			double accuracyMultiplier = WeaponCombatRules.accuracyMultiplier(strategy, itemName);

			/*
			 * Apply monster-specific passives to a proxy for the whole attack.
			 * This fixes cases such as Emberlight vs Abyssal whip on demons:
			 * Emberlight's +70% effect scales the wielder's attack/max hit in game,
			 * not only Emberlight's own small +Strength bonus.
			 */
			damage = (WEAPON_SHARED_DAMAGE_BASE + damage) * damageMultiplier;
			accuracy = (WEAPON_SHARED_ACCURACY_BASE + accuracy) * accuracyMultiplier;

			// Rat-bone weapons add flat max hit instead of a multiplier.
			damage += WeaponCombatRules.flatDamageScore(strategy, itemName);

			// Attack speed scales the whole attack contribution.
			if (stats.getAspeed() > 0)
			{
				double speedScale = 4.0 / stats.getAspeed();
				damage *= speedScale;
				accuracy *= speedScale;
			}
		}
		else if (prayerFirst || defenceFirst)
		{
			// Sustain objectives dominate non-weapon gear; offence remains a tie-breaker.
			damage *= 0.25;
			accuracy *= 0.10;
		}

		/*
		 * Efaritay's aid applies to the player's whole attack roll against
		 * Vampyres: +10% damage and +15% accuracy. Those target-only effects
		 * are not present in RuneLite's visible equipment stats, so score them
		 * from the same shared offensive baselines used for weapon passives.
		 * This deliberately puts the ring ahead of Berserker/Ultor-style flat
		 * Strength rings for a valid Vampyre method, but nowhere else.
		 */
		if (isEfaritaysAidAgainstVampyre(strategy, normalizedItemName, slot))
		{
			damage += WEAPON_SHARED_DAMAGE_BASE * 0.10;
			accuracy += WEAPON_SHARED_ACCURACY_BASE * 0.15;
		}

		double score = damage + accuracy + utility;
		String n = normalizedItemName;

		// Curated boss strategy tables are an explicit method constraint. Unlike
		// ordinary preferred items, their published weapon order must not be
		// reversed by RuneLite's incomplete item-only stat proxy.
		if (slot == EquipmentInventorySlot.WEAPON)
		{
			for (int x = 0; x < strategy.getRankedWeapons().size(); x++)
			{
				if (n.contains(NameMatcher.normalize(strategy.getRankedWeapons().get(x))))
				{
					score += 100_000 - x * 1_000;
					break;
				}
			}
		}

		if (slot == EquipmentInventorySlot.HEAD && (n.contains("slayer helm") || n.startsWith("black mask"))
			&& (strategy.getCombatStyle() == CombatStyle.MELEE || n.contains("(i)") || n.contains("imbued")))
		{
			score += 1200;
		}

		if (slot == EquipmentInventorySlot.AMMO && WeaponCombatRules.isFieryPearlAmmo(strategy, itemName))
		{
			score += 35;
		}

		// Curated names are tie-breakers; required weapons are enforced elsewhere.
		// In particular, a preferred weapon name must not overcome a meaningful
		// attack-speed, accuracy, strength or encounter-passive disadvantage.
		for (int x = 0; x < strategy.getPreferredItems().size(); x++)
		{
			if (n.contains(NameMatcher.normalize(strategy.getPreferredItems().get(x))))
			{
				score += slot == EquipmentInventorySlot.WEAPON
					? Math.max(2, 12 - x * 2)
					: Math.max(10, 40 - x * 5);
				break;
			}
		}

		return score;
	}

	static float effectiveMagicDamageBonus(
		GearStrategy strategy,
		String normalizedItemName,
		ItemEquipmentStats stats)
	{
		float visibleBonus = stats.getMdmg();
		if (strategy != null
			&& strategy.isAncientAoe()
			&& normalizedItemName != null
			&& (normalizedItemName.contains("virtus mask")
				|| normalizedItemName.contains("virtus robe top")
				|| normalizedItemName.contains("virtus robe bottom")))
		{
			// RuneLite exposes Virtus' visible 2% bonus in item stats. Ancient
			// combat spells receive another 3% per piece at cast time.
			return visibleBonus + 3.0f;
		}
		return visibleBonus;
	}

	private List<BankEquipment> collectEquipment(
		Item[] items,
		Set<Integer> bank,
		Set<Integer> packed,
		Map<Integer, Integer> canonicalByItemId)
	{
		Map<Integer, BankEquipment> dedup = new HashMap<>();
		if (items == null) return new ArrayList<>();
		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0) continue;
			int canonical = canonicalId(item.getId(), canonicalByItemId);
			// Packed/banked state is tracked by canonical id, so a later variation
			// cannot improve this entry. Avoid repeating composition/stat lookups for
			// the same item appearing in bank, inventory, and worn snapshots.
			if (dedup.containsKey(canonical)) continue;
			ItemComposition comp = itemManager.getItemComposition(item.getId());
			if (comp == null || comp.getPlaceholderTemplateId() != -1) continue;
			ItemStats stat = itemManager.getItemStats(item.getId());
			if (stat == null) stat = itemManager.getItemStats(canonical);
			if (stat == null || !stat.isEquipable() || stat.getEquipment() == null) continue;
			EquipmentInventorySlot slot = slotFor(stat.getEquipment().getSlot());
			if (slot == null || !SUPPORTED_SLOTS.contains(slot)) continue;
			String name = comp.getName();
			if (name == null || name.trim().isEmpty() || "null".equalsIgnoreCase(name)) continue;
			BankEquipment candidate = new BankEquipment(item.getId(), canonical, name, slot,
				stat.getEquipment(), bank.contains(canonical), packed.contains(canonical), stat.getWeight());
			dedup.put(canonical, candidate);
		}
		return new ArrayList<>(dedup.values());
	}

	private Set<Integer> canonicalIds(
		Item[] items,
		Map<Integer, Integer> canonicalByItemId)
	{
		Set<Integer> result = new HashSet<>(); if (items == null) return result;
		for (Item item : items)
		{
			if (item != null && item.getId() > 0 && item.getQuantity() > 0)
			{
				result.add(canonicalId(item.getId(), canonicalByItemId));
			}
		}
		return result;
	}

	private int canonicalId(int itemId, Map<Integer, Integer> canonicalByItemId)
	{
		Integer cached = canonicalByItemId.get(itemId);
		if (cached != null) return cached;
		int canonical = itemManager.canonicalize(itemId);
		canonicalByItemId.put(itemId, canonical);
		return canonical;
	}

	private static Set<String> collectOwnedNames(List<BankEquipment> items) { Set<String> r=new HashSet<>(); for(BankEquipment i:items) r.add(NameMatcher.normalize(i.name)); return r; }
	private static Set<String> parsePreferenceTokens(String s) { Set<String> r=new HashSet<>(); if(s!=null) for(String t:s.split(",")) if(!t.trim().isEmpty()) r.add(NameMatcher.normalize(t)); return r; }
	private static boolean matchesAnyPreference(String name, Set<String> tokens) { String n=NameMatcher.normalize(name); for(String t:tokens) if(n.contains(t)) return true; return false; }

	private static boolean isEligible(GearStrategy s, Set<String> names, int magic, int ranged)
	{
		if (s.getCombatStyle()==CombatStyle.MAGIC && magic<s.getMinimumMagic()) return false;
		if (s.getCombatStyle()==CombatStyle.RANGED && ranged<s.getMinimumRanged()) return false;
		return s.getRequiredWeapon()==null || names.stream().anyMatch(n -> NameMatcher.matchesAnyToken(n, s.getRequiredWeapon()));
	}

	static boolean allowed(BankEquipment item, GearStrategy strategy)
	{
		String n = NameMatcher.normalize(item.name);
		/*
		 * Void's offensive bonuses only exist with its top, robe, gloves and a
		 * combat helm worn together. Slayer loadouts prioritize the stronger
		 * on-task Slayer helm/black-mask effect, so ranking an isolated Void
		 * piece by defence can produce impossible hybrids (for example Slayer
		 * helm + Bandos chestplate + Elite void robe). Until whole Void sets are
		 * modeled as one package, do not present individual pieces as upgrades.
		 */
		if (isVoidSetPiece(n)) return false;
		if (item.slot != EquipmentInventorySlot.WEAPON) return true;
		if (strategy.getCombatStyle() == CombatStyle.RANGED && !RangedAmmoPolicy.isUsableWeapon(n)) return false;
		if (!WeaponCombatRules.usableOnTarget(strategy, n)) return false;
		if (!matchesCombatStyle(strategy.getCombatStyle(), n, item.stats)) return false;

		// A strategy that names a required weapon (for example Venator bow) is a
		// real loadout constraint, not merely an eligibility check.
		if (strategy.getRequiredWeapon() != null
			&& !NameMatcher.matchesAnyToken(n, strategy.getRequiredWeapon())) return false;

		if (strategy.getCombatStyle() == CombatStyle.MELEE
			&& !WeaponCombatRules.supportsAttackType(n, strategy.getAttackType())
			&& !WeaponCombatRules.hasTargetSpecificEffect(strategy, n)) return false;

		if (strategy.getWeaponRule() == WeaponRule.LEAF_BLADED)
		{
			if (strategy.getCombatStyle() == CombatStyle.MELEE && !n.contains("leaf-bladed")) return false;
			if (strategy.getCombatStyle() == CombatStyle.MAGIC && !n.contains("slayer's staff")) return false;
		}

		if (strategy.getWeaponRule() == WeaponRule.VAMPYRE)
		{
			return n.contains("sunspear") || n.contains("hallowed flail") || n.contains("blisterwood")
				|| n.contains("ivandis") || n.contains("rod of ivandis") || n.contains("silverlight")
				|| n.contains("darklight") || n.contains("arclight") || n.contains("emberlight");
		}
		return true;
	}

	private static boolean isVoidSetPiece(String normalizedName)
	{
		return normalizedName.contains("void knight top")
			|| normalizedName.contains("void knight robe")
			|| normalizedName.contains("void knight gloves")
			|| normalizedName.contains("elite void top")
			|| normalizedName.contains("elite void robe")
			|| normalizedName.contains("void melee helm")
			|| normalizedName.contains("void ranger helm")
			|| normalizedName.contains("void mage helm");
	}

	private static boolean matchesCombatStyle(CombatStyle style,String n,ItemEquipmentStats s)
	{
		switch(style){case MAGIC:return s.getAmagic()>0||s.getMdmg()>0||has(n,"staff","wand","sceptre","trident","tome");case RANGED:return s.getArange()>0||s.getRstr()>0||has(n,"bow","crossbow","blowpipe","atlatl","chinchompa");default:return s.getStr()>0||s.getAstab()>0||s.getAslash()>0||s.getAcrush()>0;}
	}
	static boolean usesNoAmmoSlot(String weapon)
	{
		return !RangedAmmoPolicy.usesAmmoSlot(weapon);
	}

	private static BankEquipment nthCompatibleAmmo(List<BankEquipment> ammo, String weapon, int rank)
	{
		List<BankEquipment> compatible = new ArrayList<>();
		for (BankEquipment candidate : ammo)
		{
			if (RangedAmmoPolicy.isCompatible(weapon, candidate.name)) compatible.add(candidate);
		}
		return rank <= 0 || rank > compatible.size() ? null : compatible.get(rank - 1);
	}
	private static BankEquipment nthPrayerBlessing(List<BankEquipment> ammo, int rank)
	{
		List<BankEquipment> blessings = new ArrayList<>();
		for (BankEquipment candidate : ammo)
		{
			if (NameMatcher.normalize(candidate.name).contains("blessing")) blessings.add(candidate);
		}
		blessings.sort(Comparator
			.comparingInt((BankEquipment item) -> item.stats.getPrayer()).reversed()
			.thenComparing(Comparator.comparingDouble((BankEquipment item) -> item.score).reversed()));
		return rank <= 0 || rank > blessings.size() ? null : blessings.get(rank - 1);
	}
	private static boolean has(String v,String...t){for(String x:t)if(v.contains(x))return true;return false;}
	static int attackBonus(AttackType a,ItemEquipmentStats s){switch(a){case STAB:return s.getAstab();case SLASH:return s.getAslash();case CRUSH:return s.getAcrush();default:return Math.max(s.getAstab(),Math.max(s.getAslash(),s.getAcrush()));}}
	private static EquipmentInventorySlot slotFor(int i){for(EquipmentInventorySlot s:EquipmentInventorySlot.values())if(s.getSlotIdx()==i)return s;return null;}

	private static String explain(GearStrategy strategy,String name,EquipmentInventorySlot slot,ItemEquipmentStats stats)
	{
		List<String> r = new ArrayList<>();
		if (slot == EquipmentInventorySlot.WEAPON)
		{
			String affinity = WeaponCombatRules.affinityReason(strategy, name);
			if (affinity != null) r.add(affinity);
			String intrinsic = WeaponCombatRules.intrinsicReason(strategy, name);
			if (intrinsic != null) r.add(intrinsic);
		}
		else if (slot == EquipmentInventorySlot.AMMO && WeaponCombatRules.isFieryPearlAmmo(strategy, name))
		{
			r.add("Fiery-target Sea Curse bonus");
		}
		else if (isEfaritaysAidAgainstVampyre(strategy, NameMatcher.normalize(name), slot))
		{
			r.add("+10% Vampyre damage, +15% Vampyre accuracy");
		}
		for(String p:strategy.getPreferredItems())if(NameMatcher.normalize(name).contains(NameMatcher.normalize(p))){r.add("task-method priority");break;}
		switch(strategy.getCombatStyle()){case MAGIC:add(r,effectiveMagicDamageBonus(strategy,NameMatcher.normalize(name),stats),"% magic dmg");add(r,stats.getAmagic(),"magic");break;case RANGED:add(r,stats.getRstr(),"ranged Str");add(r,stats.getArange(),"ranged");break;default:add(r,stats.getStr(),"melee Str");add(r,attackBonus(strategy.getAttackType(),stats),strategy.getAttackType().name().toLowerCase(Locale.ENGLISH));}
		if (slot != EquipmentInventorySlot.WEAPON)
		{
			int totalDefence = stats.getDstab() + stats.getDslash() + stats.getDcrush()
				+ stats.getDrange() + stats.getDmagic();
			add(r, totalDefence, "total defence");
			if (strategy.getMagicDefenceWeight() > 0) add(r, stats.getDmagic(), "Magic defence focus");
		}
		add(r,stats.getPrayer(),"prayer"); if(slot==EquipmentInventorySlot.WEAPON&&stats.getAspeed()>0)r.add(stats.getAspeed()+"-tick speed"); if(r.isEmpty())r.add("best weighted stats available"); return String.join(", ",r);
	}

	private static boolean isEfaritaysAidAgainstVampyre(
		GearStrategy strategy,
		String normalizedItemName,
		EquipmentInventorySlot slot)
	{
		return strategy != null
			&& slot == EquipmentInventorySlot.RING
			&& normalizedItemName.contains("efaritay's aid")
			&& (strategy.getWeaponRule() == WeaponRule.VAMPYRE
				|| strategy.getTargetTraits().contains(TargetTrait.VAMPYRE));
	}
	private static void add(List<String> r,float v,String label){if(v!=0)r.add((v>0?"+":"")+(v==Math.rint(v)?Integer.toString((int)v):Float.toString(v))+" "+label);}

	private static final class RiskPlan
	{
		private final Map<EquipmentInventorySlot, BankEquipment> items;
		private final long totalPrice;
		private final double totalScore;

		private RiskPlan(
			Map<EquipmentInventorySlot, BankEquipment> items,
			long totalPrice,
			double totalScore)
		{
			this.items = items;
			this.totalPrice = totalPrice;
			this.totalScore = totalScore;
		}
	}

	private static final class LoadoutCandidate
	{
		private final Map<EquipmentInventorySlot, GearRecommendation> items;
		private final double score;
		private final long guidePrice;

		private LoadoutCandidate(
			Map<EquipmentInventorySlot, GearRecommendation> items,
			double score,
			long guidePrice)
		{
			EnumMap<EquipmentInventorySlot, GearRecommendation> copy =
				new EnumMap<>(EquipmentInventorySlot.class);
			copy.putAll(items);
			this.items = copy;
			this.score = score;
			this.guidePrice = guidePrice;
		}
	}

	private static final class RiskUpgrade
	{
		private final EquipmentInventorySlot slot;
		private final BankEquipment candidate;
		private final long addedPrice;
		private final double addedScore;

		private RiskUpgrade(
			EquipmentInventorySlot slot,
			BankEquipment candidate,
			long addedPrice,
			double addedScore)
		{
			this.slot = slot;
			this.candidate = candidate;
			this.addedPrice = addedPrice;
			this.addedScore = addedScore;
		}

		private boolean isBetterThan(RiskUpgrade other)
		{
			if (other == null) return true;
			double efficiency = addedPrice <= 0
				? Double.POSITIVE_INFINITY
				: addedScore / addedPrice;
			double otherEfficiency = other.addedPrice <= 0
				? Double.POSITIVE_INFINITY
				: other.addedScore / other.addedPrice;
			return efficiency > otherEfficiency
				|| (efficiency == otherEfficiency && addedScore > other.addedScore);
		}
	}

	private static final class WeightUpgrade
	{
		private final EquipmentInventorySlot slot;
		private final BankEquipment candidate;
		private final double weightGain;
		private final double scoreLoss;
		private final boolean reachesMinimum;

		private WeightUpgrade(
			EquipmentInventorySlot slot,
			BankEquipment candidate,
			double weightGain,
			double scoreLoss,
			boolean reachesMinimum)
		{
			this.slot = slot;
			this.candidate = candidate;
			this.weightGain = weightGain;
			this.scoreLoss = scoreLoss;
			this.reachesMinimum = reachesMinimum;
		}

		private boolean isBetterThan(WeightUpgrade other)
		{
			if (other == null) return true;
			if (reachesMinimum != other.reachesMinimum) return reachesMinimum;
			if (reachesMinimum)
			{
				return scoreLoss < other.scoreLoss
					|| (scoreLoss == other.scoreLoss && weightGain < other.weightGain);
			}
			double efficiency = Math.max(0, scoreLoss) / weightGain;
			double otherEfficiency = Math.max(0, other.scoreLoss) / other.weightGain;
			return efficiency < otherEfficiency
				|| (efficiency == otherEfficiency && scoreLoss < other.scoreLoss);
		}
	}

	static final class BankEquipment
	{
		final int itemId,canonicalItemId; final String name; final EquipmentInventorySlot slot; final ItemEquipmentStats stats; final boolean banked,packed; final double weightKg; double score; int guidePrice; boolean pinned;
		BankEquipment(int itemId,int canonical,String name,EquipmentInventorySlot slot,ItemEquipmentStats stats,boolean banked,boolean packed)
		{
			this(itemId, canonical, name, slot, stats, banked, packed, 0);
		}
		BankEquipment(int itemId,int canonical,String name,EquipmentInventorySlot slot,ItemEquipmentStats stats,boolean banked,boolean packed,double weightKg)
		{
			this.itemId=itemId;this.canonicalItemId=canonical;this.name=name;this.slot=slot;this.stats=stats;this.banked=banked;this.packed=packed;this.weightKg=weightKg;
		}
	}
}
