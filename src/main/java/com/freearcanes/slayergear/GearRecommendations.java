package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;

final class GearRecommendations
{
	enum State
	{
		NO_TASK,
		UNSUPPORTED_TASK,
		OPEN_BANK,
		READY
	}

	private final State state;
	private final String taskName;
	private final int taskAmount;
	private final SlayerTaskProfile profile;
	private final GearStrategy strategy;
	private final List<GearStrategy> alternativeStrategies;
	private final Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot;
	private final List<LoadoutTier> loadoutTiers;
	private final List<GearRecommendation> weaponSwitches;
	private final List<SupplyRecommendation> supplies;
	private final ReadinessReport readiness;
	private final int bankItemsChecked;
	private final List<String> assignableMasters;
	private final GearPriority objective;
	private final List<ObjectiveLoadoutComparison> objectiveComparisons;
	private final InventoryCapacityPlan inventoryPlan;
	private final boolean bankPlanLocked;
	private final boolean bankRefreshPending;

	private GearRecommendations(
		State state,
		String taskName,
		int taskAmount,
		SlayerTaskProfile profile,
		GearStrategy strategy,
		List<GearStrategy> alternativeStrategies,
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot,
		List<LoadoutTier> loadoutTiers,
		List<GearRecommendation> weaponSwitches,
		List<SupplyRecommendation> supplies,
		ReadinessReport readiness,
		int bankItemsChecked,
		List<String> assignableMasters,
		GearPriority objective,
		List<ObjectiveLoadoutComparison> objectiveComparisons,
		InventoryCapacityPlan inventoryPlan,
		boolean bankPlanLocked,
		boolean bankRefreshPending)
	{
		this.state = state;
		this.taskName = taskName;
		this.taskAmount = taskAmount;
		this.profile = profile;
		this.strategy = strategy;
		this.alternativeStrategies = immutable(alternativeStrategies);
		EnumMap<EquipmentInventorySlot, List<GearRecommendation>> copy = new EnumMap<>(EquipmentInventorySlot.class);
		if (bySlot != null)
		{
			for (Map.Entry<EquipmentInventorySlot, List<GearRecommendation>> entry : bySlot.entrySet())
			{
				copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
			}
		}
		this.bySlot = Collections.unmodifiableMap(copy);
		this.loadoutTiers = immutable(loadoutTiers);
		this.weaponSwitches = immutable(weaponSwitches);
		this.supplies = immutable(supplies);
		this.readiness = readiness == null ? ReadinessReport.empty() : readiness;
		this.bankItemsChecked = bankItemsChecked;
		this.assignableMasters = immutable(assignableMasters);
		this.objective = objective == null ? GearPriority.BALANCED : objective;
		this.objectiveComparisons = immutable(objectiveComparisons);
		this.inventoryPlan = inventoryPlan == null
			? InventoryCapacityPlan.unavailable() : inventoryPlan;
		this.bankPlanLocked = bankPlanLocked;
		this.bankRefreshPending = bankRefreshPending;
	}

	private static <T> List<T> immutable(List<T> values)
	{
		return values == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(values));
	}

	static GearRecommendations noTask()
	{
		return new GearRecommendations(State.NO_TASK, "", 0, null, null,
			Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(),
			Collections.emptyList(), Collections.emptyList(), ReadinessReport.empty(), 0, Collections.emptyList(),
			GearPriority.BALANCED, Collections.emptyList(), InventoryCapacityPlan.unavailable(), false, false);
	}

	static GearRecommendations unsupported(String taskName, int taskAmount)
	{
		return new GearRecommendations(State.UNSUPPORTED_TASK, taskName, taskAmount, null, null,
			Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(),
			Collections.emptyList(), Collections.emptyList(), ReadinessReport.empty(), 0, SlayerMasterCatalog.mastersFor(taskName),
			GearPriority.BALANCED, Collections.emptyList(), InventoryCapacityPlan.unavailable(), false, false);
	}

	static GearRecommendations openBank(String taskName, int taskAmount, SlayerTaskProfile profile)
	{
		return new GearRecommendations(State.OPEN_BANK, taskName, taskAmount, profile, null,
			Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(),
			Collections.emptyList(), Collections.emptyList(), ReadinessReport.empty(), 0, SlayerMasterCatalog.mastersFor(taskName),
			GearPriority.BALANCED, Collections.emptyList(), InventoryCapacityPlan.unavailable(), false, false);
	}

	static GearRecommendations ready(
		String taskName,
		int taskAmount,
		SlayerTaskProfile profile,
		GearStrategy strategy,
		List<GearStrategy> alternativeStrategies,
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot,
		List<LoadoutTier> loadoutTiers,
		List<GearRecommendation> weaponSwitches,
		List<SupplyRecommendation> supplies,
		ReadinessReport readiness,
		int bankItemsChecked)
	{
		return ready(taskName, taskAmount, profile, strategy, alternativeStrategies,
			bySlot, loadoutTiers, weaponSwitches, supplies, readiness,
			bankItemsChecked, GearPriority.BALANCED);
	}

	static GearRecommendations ready(
		String taskName,
		int taskAmount,
		SlayerTaskProfile profile,
		GearStrategy strategy,
		List<GearStrategy> alternativeStrategies,
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot,
		List<LoadoutTier> loadoutTiers,
		List<GearRecommendation> weaponSwitches,
		List<SupplyRecommendation> supplies,
		ReadinessReport readiness,
		int bankItemsChecked,
		GearPriority objective)
	{
		return ready(taskName, taskAmount, profile, strategy, alternativeStrategies,
			bySlot, loadoutTiers, weaponSwitches, supplies, readiness, bankItemsChecked,
			objective, Collections.emptyList());
	}

	static GearRecommendations ready(
		String taskName, int taskAmount, SlayerTaskProfile profile, GearStrategy strategy,
		List<GearStrategy> alternativeStrategies,
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot,
		List<LoadoutTier> loadoutTiers, List<GearRecommendation> weaponSwitches,
		List<SupplyRecommendation> supplies, ReadinessReport readiness,
		int bankItemsChecked, GearPriority objective,
		List<ObjectiveLoadoutComparison> objectiveComparisons)
	{
		return new GearRecommendations(State.READY, taskName, taskAmount, profile, strategy,
			alternativeStrategies, bySlot, loadoutTiers, weaponSwitches, supplies, readiness, bankItemsChecked,
			SlayerMasterCatalog.mastersFor(taskName), objective, objectiveComparisons,
			InventoryCapacityPlan.unavailable(), false, false);
	}

	static GearRecommendations ready(
		String taskName,
		int taskAmount,
		SlayerTaskProfile profile,
		GearStrategy strategy,
		List<GearStrategy> alternativeStrategies,
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot,
		List<LoadoutTier> loadoutTiers,
		List<SupplyRecommendation> supplies,
		ReadinessReport readiness,
		int bankItemsChecked)
	{
		return ready(taskName, taskAmount, profile, strategy, alternativeStrategies,
			bySlot, loadoutTiers, Collections.emptyList(), supplies, readiness, bankItemsChecked);
	}

	State getState() { return state; }
	String getTaskName() { return taskName; }
	int getTaskAmount() { return taskAmount; }
	SlayerTaskProfile getProfile() { return profile; }
	GearStrategy getStrategy() { return strategy; }
	List<GearStrategy> getAlternativeStrategies() { return alternativeStrategies; }
	Map<EquipmentInventorySlot, List<GearRecommendation>> getBySlot() { return bySlot; }
	List<LoadoutTier> getLoadoutTiers() { return loadoutTiers; }
	List<GearRecommendation> getWeaponSwitches() { return weaponSwitches; }
	List<SupplyRecommendation> getSupplies() { return supplies; }
	ReadinessReport getReadiness() { return readiness; }
	int getBankItemsChecked() { return bankItemsChecked; }
	List<String> getAssignableMasters() { return assignableMasters; }
	GearPriority getObjective() { return objective; }
	List<ObjectiveLoadoutComparison> getObjectiveComparisons() { return objectiveComparisons; }
	InventoryCapacityPlan getInventoryPlan() { return inventoryPlan; }
	boolean isBankPlanLocked() { return bankPlanLocked; }
	boolean isBankRefreshPending() { return bankRefreshPending; }

	GearRecommendations withPreparationState(
		List<SupplyRecommendation> plannedSupplies,
		InventoryCapacityPlan plan,
		boolean locked,
		boolean refreshPending)
	{
		List<SupplyRecommendation> effectiveSupplies =
			plannedSupplies == null ? supplies : plannedSupplies;
		return new GearRecommendations(
			state,
			taskName,
			taskAmount,
			profile,
			strategy,
			alternativeStrategies,
			bySlot,
			loadoutTiers,
			weaponSwitches,
			effectiveSupplies,
			readiness.withSupplyProgress(effectiveSupplies),
			bankItemsChecked,
			assignableMasters,
			objective,
			objectiveComparisons,
			plan,
			locked,
			refreshPending);
	}

	GearRecommendations withBankSessionState(boolean locked, boolean refreshPending)
	{
		return new GearRecommendations(
			state,
			taskName,
			taskAmount,
			profile,
			strategy,
			alternativeStrategies,
			bySlot,
			loadoutTiers,
			weaponSwitches,
			supplies,
			readiness,
			bankItemsChecked,
			assignableMasters,
			objective,
			objectiveComparisons,
			inventoryPlan,
			locked,
			refreshPending);
	}

	GearRecommendations withoutWeaponSwitches()
	{
		return new GearRecommendations(
			state, taskName, taskAmount, profile, strategy, alternativeStrategies,
			bySlot, loadoutTiers, Collections.emptyList(), supplies, readiness,
			bankItemsChecked, assignableMasters, objective, objectiveComparisons, inventoryPlan,
			bankPlanLocked, bankRefreshPending);
	}

	List<GearRecommendation> get(EquipmentInventorySlot slot)
	{
		return bySlot.getOrDefault(slot, Collections.emptyList());
	}

	boolean isRecommended(int canonicalItemId)
	{
		return bySlot.values().stream().flatMap(List::stream)
			.anyMatch(recommendation -> recommendation.getCanonicalItemId() == canonicalItemId)
			|| weaponSwitches.stream().anyMatch(item -> item.getCanonicalItemId() == canonicalItemId);
	}

	GearRecommendation find(int canonicalItemId)
	{
		GearRecommendation equipped = bySlot.values().stream().flatMap(List::stream)
			.filter(recommendation -> recommendation.getCanonicalItemId() == canonicalItemId)
			.findFirst().orElse(null);
		if (equipped != null) return equipped;
		return weaponSwitches.stream()
			.filter(item -> item.getCanonicalItemId() == canonicalItemId)
			.findFirst().orElse(null);
	}

	SupplyRecommendation findSupply(int canonicalItemId)
	{
		return supplies.stream()
			.filter(supply -> supply.getStatus().isBanked())
			.filter(SupplyRecommendation::isEnabledForTrip)
			.filter(supply -> supply.getCanonicalItemId() == canonicalItemId)
			.findFirst().orElse(null);
	}

	boolean isBankViewItem(int canonicalItemId)
	{
		return isRecommended(canonicalItemId) || findSupply(canonicalItemId) != null;
	}

	int getRecommendationCount()
	{
		return bySlot.values().stream().mapToInt(List::size).sum();
	}
}
