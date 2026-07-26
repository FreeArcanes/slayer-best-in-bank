package com.freearcanes.slayergear;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;

final class LoadoutTier
{
	private final int rank;
	private final Map<EquipmentInventorySlot, GearRecommendation> items;
	private final int guidePrice;
	private final int riskCapGp;

	LoadoutTier(int rank, Map<EquipmentInventorySlot, GearRecommendation> items)
	{
		this(rank, items, 0, 0);
	}

	LoadoutTier(
		int rank,
		Map<EquipmentInventorySlot, GearRecommendation> items,
		int guidePrice,
		int riskCapGp)
	{
		this.rank = rank;
		EnumMap<EquipmentInventorySlot, GearRecommendation> copy = new EnumMap<>(EquipmentInventorySlot.class);
		copy.putAll(items);
		this.items = Collections.unmodifiableMap(copy);
		this.guidePrice = Math.max(0, guidePrice);
		this.riskCapGp = Math.max(0, riskCapGp);
	}

	int getRank() { return rank; }
	Map<EquipmentInventorySlot, GearRecommendation> getItems() { return items; }
	GearRecommendation get(EquipmentInventorySlot slot) { return items.get(slot); }
	int getGuidePrice() { return guidePrice; }
	int getRiskCapGp() { return riskCapGp; }
}
