package com.freearcanes.slayergear;

public enum SpellTeleportPreference
{
	TABLETS_FIRST("Tablets first"),
	RUNES_FIRST("Runes first");

	private final String label;

	SpellTeleportPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
