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
	private final int automaticQuantity;
	private final int requestedQuantity;
	private final int recommendedQuantity;
	private final int packedQuantity;
	private final int bankQuantity;
	private final String quantityUnit;

	SupplyRecommendation(
		int itemId,
		int canonicalItemId,
		String itemName,
		String category,
		String reason,
		SupplyStatus status,
		boolean required)
	{
		this(itemId, canonicalItemId, itemName, category, reason, status, required, 0, 0, 0, 0, "");
	}

	SupplyRecommendation(
		int itemId,
		int canonicalItemId,
		String itemName,
		String category,
		String reason,
		SupplyStatus status,
		boolean required,
		int recommendedQuantity,
		int packedQuantity,
		int bankQuantity,
		String quantityUnit)
	{
		this(itemId, canonicalItemId, itemName, category, reason, status, required,
			recommendedQuantity, recommendedQuantity, packedQuantity, bankQuantity, quantityUnit);
	}

	SupplyRecommendation(
		int itemId,
		int canonicalItemId,
		String itemName,
		String category,
		String reason,
		SupplyStatus status,
		boolean required,
		int automaticQuantity,
		int recommendedQuantity,
		int packedQuantity,
		int bankQuantity,
		String quantityUnit)
	{
		this(
			itemId,
			canonicalItemId,
			itemName,
			category,
			reason,
			status,
			required,
			automaticQuantity,
			recommendedQuantity,
			recommendedQuantity,
			packedQuantity,
			bankQuantity,
			quantityUnit);
	}

	private SupplyRecommendation(
		int itemId,
		int canonicalItemId,
		String itemName,
		String category,
		String reason,
		SupplyStatus status,
		boolean required,
		int automaticQuantity,
		int requestedQuantity,
		int recommendedQuantity,
		int packedQuantity,
		int bankQuantity,
		String quantityUnit)
	{
		this.itemId = itemId;
		this.canonicalItemId = canonicalItemId == 0 ? itemId : canonicalItemId;
		this.itemName = itemName;
		this.category = category;
		this.reason = reason;
		this.status = status;
		this.required = required;
		this.automaticQuantity = Math.max(0, automaticQuantity);
		this.requestedQuantity = Math.max(0, requestedQuantity);
		this.recommendedQuantity = Math.max(0, recommendedQuantity);
		this.packedQuantity = Math.max(0, packedQuantity);
		this.bankQuantity = Math.max(0, bankQuantity);
		this.quantityUnit = quantityUnit == null ? "" : quantityUnit;
	}

	int getItemId() { return itemId; }
	int getCanonicalItemId() { return canonicalItemId; }
	String getItemName() { return itemName; }
	String getCategory() { return category; }
	String getReason() { return reason; }
	SupplyStatus getStatus() { return status; }
	boolean isRequired() { return required; }
	int getAutomaticQuantity() { return automaticQuantity; }
	int getRequestedQuantity() { return requestedQuantity; }
	int getRecommendedQuantity() { return recommendedQuantity; }
	int getPackedQuantity() { return packedQuantity; }
	int getBankQuantity() { return bankQuantity; }
	String getQuantityUnit() { return quantityUnit; }
	boolean isQuantityAdjustable() { return automaticQuantity > 0; }
	boolean isCapacityAdjusted() { return recommendedQuantity < requestedQuantity; }
	boolean isEnabledForTrip() { return !isQuantityAdjustable() || recommendedQuantity > 0; }
	boolean hasQuantityTarget() { return recommendedQuantity > 0; }
	boolean hasRecommendedQuantityPacked() { return !hasQuantityTarget() || packedQuantity >= recommendedQuantity; }
	int getQuantityStillNeeded() { return Math.max(0, recommendedQuantity - packedQuantity); }
	int getUnitsPerWithdrawal()
	{
		if ("doses".equals(quantityUnit))
		{
			return Math.max(1, SmartSupplyAdvisor.doseScore(itemName));
		}
		return "shots".equals(quantityUnit) ? Math.max(1, getQuantityStillNeeded()) : 1;
	}
	int getWithdrawalsStillNeeded()
	{
		int remaining = getQuantityStillNeeded();
		int perWithdrawal = getUnitsPerWithdrawal();
		return remaining == 0 ? 0 : (remaining + perWithdrawal - 1) / perWithdrawal;
	}
	int getAdjustmentUnit()
	{
		if ("doses".equals(quantityUnit)) return Math.max(1, SmartSupplyAdvisor.doseScore(itemName));
		if ("shots".equals(quantityUnit)) return 100;
		return 1;
	}

	SupplyRecommendation withCapacityQuantity(int quantity)
	{
		return new SupplyRecommendation(
			itemId,
			canonicalItemId,
			itemName,
			category,
			reason,
			status,
			required,
			automaticQuantity,
			requestedQuantity,
			Math.max(0, quantity),
			packedQuantity,
			bankQuantity,
			quantityUnit);
	}
}
