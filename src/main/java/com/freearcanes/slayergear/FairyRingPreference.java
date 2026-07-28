package com.freearcanes.slayergear;

public enum FairyRingPreference
{
	QUEST_CAPE_FIRST("Quest cape first"),
	LUNAR_STAFF_FIRST("Lunar staff first"),
	DRAMEN_STAFF_FIRST("Dramen staff first");

	private final String label;

	FairyRingPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
