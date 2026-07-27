package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import net.runelite.api.Item;

/**
 * Preserves the supply portion of a completed bank loadout while the player is
 * using that trip. The state is intentionally UI-free: opening a bank resets it.
 */
final class TripPreparationState
{
	private boolean active;
	private final Set<Integer> preparedSupplyIds = new HashSet<>();
	private Item[] preparedSupplyItems = new Item[0];

	void reset()
	{
		active = false;
		preparedSupplyIds.clear();
		preparedSupplyItems = new Item[0];
	}

	void arm(
		GearRecommendations recommendations,
		Item[] packedItems,
		IntUnaryOperator canonicalize)
	{
		reset();
		if (recommendations == null
			|| recommendations.getState() != GearRecommendations.State.READY
			|| !recommendations.getReadiness().isReadyToLeave())
		{
			return;
		}

		for (SupplyRecommendation supply : recommendations.getSupplies())
		{
			if (supply.isEnabledForTrip()
				&& supply.getCanonicalItemId() > 0
				&& supply.getStatus().isPacked()
				&& supply.hasRecommendedQuantityPacked())
			{
				preparedSupplyIds.add(supply.getCanonicalItemId());
			}
		}

		List<Item> snapshot = new ArrayList<>();
		if (packedItems != null)
		{
			for (Item item : packedItems)
			{
				if (isUsable(item)
					&& preparedSupplyIds.contains(canonicalize.applyAsInt(item.getId())))
				{
					snapshot.add(new Item(item.getId(), item.getQuantity()));
				}
			}
		}
		preparedSupplyItems = snapshot.toArray(new Item[0]);
		active = true;
	}

	Item[] suppliesForScoring(Item[] livePackedItems, IntUnaryOperator canonicalize)
	{
		if (!active || preparedSupplyIds.isEmpty())
		{
			return livePackedItems == null ? new Item[0] : livePackedItems;
		}

		List<Item> effective = new ArrayList<>();
		for (Item item : preparedSupplyItems)
		{
			effective.add(new Item(item.getId(), item.getQuantity()));
		}
		if (livePackedItems != null)
		{
			for (Item item : livePackedItems)
			{
				if (isUsable(item)
					&& !preparedSupplyIds.contains(canonicalize.applyAsInt(item.getId())))
				{
					effective.add(item);
				}
			}
		}
		return effective.toArray(new Item[0]);
	}

	boolean isActive()
	{
		return active;
	}

	static boolean assignmentChanged(
		String previousTask,
		String previousLocation,
		int previousAmount,
		String currentTask,
		String currentLocation,
		int currentAmount)
	{
		if (previousTask == null)
		{
			return false;
		}
		return !safe(previousTask).equals(safe(currentTask))
			|| !safe(previousLocation).equals(safe(currentLocation))
			|| currentAmount > previousAmount;
	}

	private static boolean isUsable(Item item)
	{
		return item != null && item.getId() > 0 && item.getQuantity() > 0;
	}

	private static String safe(String value)
	{
		return value == null ? "" : value.trim();
	}
}
