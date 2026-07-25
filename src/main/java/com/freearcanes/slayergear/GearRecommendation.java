package com.freearcanes.slayergear;

import net.runelite.api.EquipmentInventorySlot;

final class GearRecommendation
{
	private final int itemId;
	private final int canonicalItemId;
	private final String itemName;
	private final EquipmentInventorySlot slot;
	private final double score;
	private final int rank;
	private final boolean twoHanded;
	private final String reason;
	private final boolean packed;
	private final boolean banked;

	private GearRecommendation(Builder builder)
	{
		itemId = builder.itemId;
		canonicalItemId = builder.canonicalItemId == 0 ? builder.itemId : builder.canonicalItemId;
		itemName = builder.itemName;
		slot = builder.slot;
		score = builder.score;
		rank = builder.rank;
		twoHanded = builder.twoHanded;
		reason = builder.reason;
		packed = builder.packed;
		banked = builder.banked;
	}

	static Builder builder() { return new Builder(); }
	int getItemId() { return itemId; }
	int getCanonicalItemId() { return canonicalItemId; }
	String getItemName() { return itemName; }
	EquipmentInventorySlot getSlot() { return slot; }
	double getScore() { return score; }
	int getRank() { return rank; }
	boolean isTwoHanded() { return twoHanded; }
	String getReason() { return reason; }
	boolean isPacked() { return packed; }
	boolean isBanked() { return banked; }

	static final class Builder
	{
		private int itemId;
		private int canonicalItemId;
		private String itemName;
		private EquipmentInventorySlot slot;
		private double score;
		private int rank;
		private boolean twoHanded;
		private String reason;
		private boolean packed;
		private boolean banked;
		Builder itemId(int value) { itemId = value; return this; }
		Builder canonicalItemId(int value) { canonicalItemId = value; return this; }
		Builder itemName(String value) { itemName = value; return this; }
		Builder slot(EquipmentInventorySlot value) { slot = value; return this; }
		Builder score(double value) { score = value; return this; }
		Builder rank(int value) { rank = value; return this; }
		Builder twoHanded(boolean value) { twoHanded = value; return this; }
		Builder reason(String value) { reason = value; return this; }
		Builder packed(boolean value) { packed = value; return this; }
		Builder banked(boolean value) { banked = value; return this; }
		GearRecommendation build() { return new GearRecommendation(this); }
	}
}
