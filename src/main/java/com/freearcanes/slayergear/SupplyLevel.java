package com.freearcanes.slayergear;

public enum SupplyLevel
{
	LIGHT("Light", 0.65),
	NORMAL("Normal", 1.0),
	EXTRA("Extra", 1.4);

	private final String label;
	private final double multiplier;

	SupplyLevel(String label, double multiplier)
	{
		this.label = label;
		this.multiplier = multiplier;
	}

	double getMultiplier() { return multiplier; }

	@Override
	public String toString() { return label; }
}
