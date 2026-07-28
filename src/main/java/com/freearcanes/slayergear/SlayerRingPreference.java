package com.freearcanes.slayergear;

public enum SlayerRingPreference
{
	ETERNAL_FIRST("Eternal first"),
	CHARGED_FIRST("Charged first");

	private final String label;

	SlayerRingPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
