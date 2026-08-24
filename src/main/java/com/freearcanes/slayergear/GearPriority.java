package com.freearcanes.slayergear;

public enum GearPriority
{
	BALANCED("Max DPS"),
	PRAYER_FIRST("Prayer Sustain"),
	DEFENCE_FIRST("Defence First"),
	VALUE("Value / Low Cost");

	private final String displayName;

	GearPriority(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
