package com.freearcanes.slayergear;

public enum GearPriority
{
	BALANCED("Balanced"),
	PRAYER_FIRST("Prayer First");

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