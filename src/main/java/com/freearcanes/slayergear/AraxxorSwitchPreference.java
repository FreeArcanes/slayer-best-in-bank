package com.freearcanes.slayergear;

public enum AraxxorSwitchPreference
{
	AUTOMATIC("Best owned"),
	NOXIOUS_HALBERD("Noxious halberd"),
	HEAVY_BALLISTA("Heavy ballista + dragon javelins");

	private final String label;

	AraxxorSwitchPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
