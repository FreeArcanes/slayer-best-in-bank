package com.freearcanes.slayergear;

public enum FairyRingPreference
{
	MAX_CAPE_FIRST("Max cape first"),
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
