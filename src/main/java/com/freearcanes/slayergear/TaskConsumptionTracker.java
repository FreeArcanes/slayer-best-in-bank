package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;

/** Session-only observed consumption tracker; never guesses from bank quantities. */
final class TaskConsumptionTracker
{
	private String taskName = "";
	private int startingAmount;
	private int latestAmount;
	private long startedAtMillis;
	private final Map<String, TrackedSupply> supplies = new LinkedHashMap<>();

	void start(GearRecommendations recommendations, Item[] packedItems,
		ItemManager itemManager, long nowMillis)
	{
		start(recommendations,
			recommendations == null ? "" : recommendations.getTaskName(),
			recommendations == null ? 0 : recommendations.getTaskAmount(),
			packedItems, itemManager, nowMillis);
	}

	void start(GearRecommendations recommendations, String assignmentName,
		int assignmentAmount, Item[] packedItems, ItemManager itemManager, long nowMillis)
	{
		reset();
		begin(recommendations, assignmentName, assignmentAmount,
			packedItems, itemManager, nowMillis);
	}

	void resume(GearRecommendations recommendations, String assignmentName,
		int assignmentAmount, Item[] packedItems, ItemManager itemManager, long nowMillis)
	{
		String nextTask = assignmentName == null ? "" : assignmentName.trim();
		if (!isActive() || !NameMatcher.normalize(taskName).equals(
			NameMatcher.normalize(nextTask)) || assignmentAmount > latestAmount)
		{
			reset();
			begin(recommendations, nextTask, assignmentAmount,
				packedItems, itemManager, nowMillis);
			return;
		}
		if (recommendations == null
			|| recommendations.getState() != GearRecommendations.State.READY) return;
		latestAmount = Math.max(0, assignmentAmount);
		// Banking starts a fresh inventory checkpoint for every previously tracked
		// category, including one omitted from the newly selected trip plan.
		for (TrackedSupply tracked : supplies.values())
		{
			int units = unitsFor(tracked.canonicalItemId, tracked.unit,
				packedItems, itemManager);
			tracked.resume(tracked.canonicalItemId, tracked.unit, units);
		}
		for (SupplyRecommendation supply : recommendations.getSupplies())
		{
			if (!trackable(supply)) continue;
			int units = unitsFor(supply.getCanonicalItemId(), supply.getQuantityUnit(),
				packedItems, itemManager);
			TrackedSupply tracked = supplies.get(supply.getCategory());
			if (tracked == null)
			{
				supplies.put(supply.getCategory(), new TrackedSupply(
					supply.getCategory(), supply.getCanonicalItemId(),
					supply.getQuantityUnit(), units));
			}
			else
			{
				tracked.resume(supply.getCanonicalItemId(), supply.getQuantityUnit(), units);
			}
		}
	}

	private void begin(GearRecommendations recommendations, String assignmentName,
		int assignmentAmount, Item[] packedItems, ItemManager itemManager, long nowMillis)
	{
		if (recommendations == null
			|| recommendations.getState() != GearRecommendations.State.READY) return;
		taskName = assignmentName == null ? "" : assignmentName.trim();
		if (taskName.isEmpty()) return;
		startingAmount = Math.max(0, assignmentAmount);
		latestAmount = startingAmount;
		startedAtMillis = Math.max(0, nowMillis);
		for (SupplyRecommendation supply : recommendations.getSupplies())
		{
			if (!trackable(supply)) continue;
			int units = unitsFor(supply.getCanonicalItemId(), supply.getQuantityUnit(),
				packedItems, itemManager);
			supplies.put(supply.getCategory(), new TrackedSupply(
				supply.getCategory(), supply.getCanonicalItemId(), supply.getQuantityUnit(), units));
		}
	}

	void observe(Item[] packedItems, ItemManager itemManager, int remainingAmount)
	{
		if (!isActive()) return;
		int nextAmount = Math.max(0, remainingAmount);
		boolean confirmedTaskProgress = nextAmount < latestAmount;
		for (TrackedSupply supply : supplies.values())
		{
			supply.observedUnits = unitsFor(supply.canonicalItemId, supply.unit,
				packedItems, itemManager);
			if (confirmedTaskProgress) supply.commitObservedUse();
		}
		latestAmount = nextAmount;
	}

	TaskCompletionSummary finish(int finalAmount, long nowMillis)
	{
		if (!isActive()) return null;
		int nextAmount = Math.max(0, finalAmount);
		if (nextAmount < latestAmount)
		{
			for (TrackedSupply supply : supplies.values()) supply.commitObservedUse();
		}
		latestAmount = nextAmount;
		List<String> consumed = new ArrayList<>();
		for (TrackedSupply supply : supplies.values())
		{
			int used = supply.consumedUnits;
			if (used > 0) consumed.add(supply.category + " " + used + " " + supply.unit);
		}
		TaskCompletionSummary summary = new TaskCompletionSummary(taskName,
			Math.max(0, startingAmount - latestAmount),
			Math.max(0, nowMillis - startedAtMillis) / 1000L, consumed);
		reset();
		return summary;
	}

	void reset()
	{
		taskName = "";
		startingAmount = 0;
		latestAmount = 0;
		startedAtMillis = 0;
		supplies.clear();
	}

	boolean isActive() { return !taskName.isEmpty(); }

	private static boolean trackable(SupplyRecommendation supply)
	{
		return supply != null && supply.isEnabledForTrip()
			&& supply.hasQuantityTarget() && supply.getCanonicalItemId() > 0;
	}

	static int unitsFor(int canonicalItemId, String unit, Item[] items,
		ItemManager itemManager)
	{
		if (items == null || itemManager == null) return 0;
		int total = 0;
		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0
				|| itemManager.canonicalize(item.getId()) != canonicalItemId) continue;
			if ("doses".equals(unit))
			{
				ItemComposition composition = itemManager.getItemComposition(item.getId());
				int doses = composition == null ? 0
					: SmartSupplyAdvisor.doseScore(composition.getName());
				total += item.getQuantity() * Math.max(1, doses);
			}
			else total += item.getQuantity();
		}
		return total;
	}

	private static final class TrackedSupply
	{
		private final String category;
		private int canonicalItemId;
		private String unit;
		private int checkpointUnits;
		private int observedUnits;
		private int consumedUnits;
		private TrackedSupply(String category, int canonicalItemId, String unit, int units)
		{
			this.category = category;
			this.canonicalItemId = canonicalItemId;
			this.unit = unit;
			this.checkpointUnits = units;
			this.observedUnits = units;
		}

		private void resume(int itemId, String quantityUnit, int units)
		{
			canonicalItemId = itemId;
			unit = quantityUnit;
			checkpointUnits = units;
			observedUnits = units;
		}

		private void commitObservedUse()
		{
			consumedUnits += Math.max(0, checkpointUnits - observedUnits);
			checkpointUnits = observedUnits;
		}
	}
}
