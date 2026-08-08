package com.freearcanes.slayergear;

public enum SlayerBraceletPreference
{
	EXPEDITIOUS("Expeditious bracelet"),
	SLAUGHTER("Bracelet of slaughter"),
	BOTH("Both bracelets");

	private final String label;

	SlayerBraceletPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
