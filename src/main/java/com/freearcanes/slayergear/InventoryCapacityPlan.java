package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class InventoryCapacityPlan
{
	static final int INVENTORY_CAPACITY = 28;

	private final boolean available;
	private final int occupiedSlots;
	private final int pendingGearSlots;
	private final int requestedSlots;
	private final int plannedSlots;
	private final List<String> reductions;

	private InventoryCapacityPlan(
		boolean available,
		int occupiedSlots,
		int pendingGearSlots,
		int requestedSlots,
		int plannedSlots,
		List<String> reductions)
	{
		this.available = available;
		this.occupiedSlots = Math.max(0, occupiedSlots);
		this.pendingGearSlots = Math.max(0, pendingGearSlots);
		this.requestedSlots = Math.max(0, requestedSlots);
		this.plannedSlots = Math.max(0, plannedSlots);
		this.reductions = Collections.unmodifiableList(new ArrayList<>(reductions));
	}

	static InventoryCapacityPlan unavailable()
	{
		return new InventoryCapacityPlan(false, 0, 0, 0, 0, Collections.emptyList());
	}

	static InventoryCapacityPlan available(
		int occupiedSlots,
		int pendingGearSlots,
		int requestedSlots,
		int plannedSlots,
		List<String> reductions)
	{
		return new InventoryCapacityPlan(
			true, occupiedSlots, pendingGearSlots, requestedSlots, plannedSlots, reductions);
	}

	boolean isAvailable() { return available; }
	int getOccupiedSlots() { return occupiedSlots; }
	int getPendingGearSlots() { return pendingGearSlots; }
	int getRequestedSlots() { return requestedSlots; }
	int getPlannedSlots() { return plannedSlots; }
	int getCapacity() { return INVENTORY_CAPACITY; }
	List<String> getReductions() { return reductions; }
	boolean wasTrimmed() { return requestedSlots > plannedSlots; }
	boolean fits() { return plannedSlots <= INVENTORY_CAPACITY; }
	int getOverBy() { return Math.max(0, plannedSlots - INVENTORY_CAPACITY); }
}
