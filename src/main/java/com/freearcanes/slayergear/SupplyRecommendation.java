package com.freearcanes.slayergear;

final class SupplyRecommendation
{
	private final int itemId;
	private final int canonicalItemId;
	private final String itemName;
	private final String category;
	private final String reason;
	private final SupplyStatus status;
	private final boolean required;

	SupplyRecommendation(
		int itemId,
		int canonicalItemId,
		String itemName,
		String category,
		String reason,
		SupplyStatus status,
		boolean required)
	{
		this.itemId = itemId;
		this.canonicalItemId = canonicalItemId == 0 ? itemId : canonicalItemId;
		this.itemName = itemName;
		this.category = category;
		this.reason = reason;
		this.status = status;
		this.required = required;
	}

	int getItemId() { return itemId; }
	int getCanonicalItemId() { return canonicalItemId; }
	String getItemName() { return itemName; }
	String getCategory() { return category; }
	String getReason() { return reason; }
	SupplyStatus getStatus() { return status; }
	boolean isRequired() { return required; }
}
