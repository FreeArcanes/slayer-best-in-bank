package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Item;

final class InventoryCapacityPlanner
{
	@Inject
	InventoryCapacityPlanner()
	{
	}

	GearRecommendations apply(
		GearRecommendations recommendations,
		Item[] inventoryItems,
		boolean bankPlanLocked,
		boolean bankRefreshPending)
	{
		if (recommendations == null
			|| recommendations.getState() != GearRecommendations.State.READY)
		{
			return recommendations;
		}

		List<SupplyRecommendation> supplies =
			new ArrayList<>(recommendations.getSupplies());
		int occupiedSlots = occupiedSlots(inventoryItems);
		int pendingGearSlots = pendingGearSlots(recommendations);
		int requestedSlots = occupiedSlots + pendingGearSlots + supplySlots(supplies);
		int excess = Math.max(0, requestedSlots - InventoryCapacityPlan.INVENTORY_CAPACITY);
		List<String> reductions = new ArrayList<>();

		List<Integer> trimOrder = new ArrayList<>();
		for (int index = 0; index < supplies.size(); index++)
		{
			SupplyRecommendation supply = supplies.get(index);
			if (!supply.isRequired()
				&& supply.isEnabledForTrip()
				&& supply.isQuantityAdjustable()
				&& additionalSlots(supply) > 0)
			{
				trimOrder.add(index);
			}
		}
		trimOrder.sort(Comparator
			.comparingInt((Integer index) -> trimPriority(supplies.get(index)))
			.thenComparingInt(Integer::intValue));

		for (int index : trimOrder)
		{
			if (excess <= 0)
			{
				break;
			}

			SupplyRecommendation supply = supplies.get(index);
			int currentSlots = additionalSlots(supply);
			int slotsToRemove = Math.min(excess, currentSlots);
			int reducedQuantity = quantityForAdditionalSlots(
				supply, currentSlots - slotsToRemove);
			if (reducedQuantity >= supply.getRecommendedQuantity())
			{
				continue;
			}

			int reduction = supply.getRecommendedQuantity() - reducedQuantity;
			SupplyRecommendation adjusted = supply.withCapacityQuantity(reducedQuantity);
			supplies.set(index, adjusted);
			int freed = currentSlots - additionalSlots(adjusted);
			excess -= freed;
			if (freed > 0)
			{
				reductions.add(supply.getCategory() + " -" + reduction
					+ (supply.getQuantityUnit().isEmpty()
						? "" : " " + supply.getQuantityUnit()));
			}
		}

		int plannedSlots = occupiedSlots + pendingGearSlots + supplySlots(supplies);
		InventoryCapacityPlan plan = InventoryCapacityPlan.available(
			occupiedSlots,
			pendingGearSlots,
			requestedSlots,
			plannedSlots,
			reductions);
		return recommendations.withPreparationState(
			supplies, plan, bankPlanLocked, bankRefreshPending);
	}

	static int occupiedSlots(Item[] items)
	{
		if (items == null)
		{
			return 0;
		}

		int occupied = 0;
		for (Item item : items)
		{
			if (item != null && item.getId() > 0 && item.getQuantity() > 0)
			{
				occupied++;
			}
		}
		return occupied;
	}

	static int additionalSlots(SupplyRecommendation supply)
	{
		if (supply == null || !supply.isEnabledForTrip())
		{
			return 0;
		}
		if (!supply.hasQuantityTarget())
		{
			return supply.getStatus().isPacked() ? 0 : 1;
		}

		int remaining = supply.getQuantityStillNeeded();
		if (remaining <= 0)
		{
			return 0;
		}
		if ("shots".equals(supply.getQuantityUnit()))
		{
			return supply.getPackedQuantity() > 0 ? 0 : 1;
		}
		if ("doses".equals(supply.getQuantityUnit()))
		{
			int doseSize = Math.max(1, supply.getUnitsPerWithdrawal());
			return (remaining + doseSize - 1) / doseSize;
		}
		return remaining;
	}

	private static int pendingGearSlots(GearRecommendations recommendations)
	{
		if (recommendations.getLoadoutTiers().isEmpty())
		{
			return 0;
		}

		int slots = 0;
		for (GearRecommendation recommendation
			: recommendations.getLoadoutTiers().get(0).getItems().values())
		{
			if (recommendation.isBanked() && !recommendation.isPacked())
			{
				slots++;
			}
		}
		return slots;
	}

	private static int supplySlots(List<SupplyRecommendation> supplies)
	{
		int slots = 0;
		for (SupplyRecommendation supply : supplies)
		{
			slots += additionalSlots(supply);
		}
		return slots;
	}

	private static int quantityForAdditionalSlots(
		SupplyRecommendation supply,
		int additionalSlots)
	{
		int safeSlots = Math.max(0, additionalSlots);
		int packed = supply.getPackedQuantity();
		if ("shots".equals(supply.getQuantityUnit()))
		{
			return safeSlots == 0 ? packed : supply.getRecommendedQuantity();
		}
		if ("doses".equals(supply.getQuantityUnit()))
		{
			return Math.min(
				supply.getRecommendedQuantity(),
				packed + safeSlots * Math.max(1, supply.getUnitsPerWithdrawal()));
		}
		return Math.min(supply.getRecommendedQuantity(), packed + safeSlots);
	}

	private static int trimPriority(SupplyRecommendation supply)
	{
		switch (supply.getCategory())
		{
			case "Food":
				return 0;
			case "Run energy":
				return 1;
			case "Goading":
				return 2;
			case "Prayer regen":
				return 3;
			case "Combat boost":
			case "Ranged boost":
			case "Magic boost":
				return 4;
			case "Prayer":
				return 5;
			case "Cannon ammo":
				return 6;
			default:
				return 7;
		}
	}
}
