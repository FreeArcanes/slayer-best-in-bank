package com.freearcanes.slayergear;

public enum PrayerRemainsPreference
{
	OFF("Off"),
	AUTOMATIC("Automatic by task"),
	BOTH("Both tools"),
	BONECRUSHER("Bonecrusher"),
	ASH_SANCTIFIER("Ash sanctifier");

	private final String label;

	PrayerRemainsPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
