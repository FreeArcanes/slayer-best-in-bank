package com.freearcanes.slayergear;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;

final class LoadoutTier
{
	private final int rank;
	private final Map<EquipmentInventorySlot, GearRecommendation> items;

	LoadoutTier(int rank, Map<EquipmentInventorySlot, GearRecommendation> items)
	{
		this.rank = rank;
		EnumMap<EquipmentInventorySlot, GearRecommendation> copy = new EnumMap<>(EquipmentInventorySlot.class);
		copy.putAll(items);
		this.items = Collections.unmodifiableMap(copy);
	}

	int getRank() { return rank; }
	Map<EquipmentInventorySlot, GearRecommendation> getItems() { return items; }
	GearRecommendation get(EquipmentInventorySlot slot) { return items.get(slot); }
}