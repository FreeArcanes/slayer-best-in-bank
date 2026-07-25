package com.freearcanes.slayergear;

enum SupplyStatus
{
	PACKED,
	BANKED,
	PACKED_BANKED,
	MISSING;

	boolean isPacked()
	{
		return this == PACKED || this == PACKED_BANKED;
	}

	boolean isBanked()
	{
		return this == BANKED || this == PACKED_BANKED;
	}
}
