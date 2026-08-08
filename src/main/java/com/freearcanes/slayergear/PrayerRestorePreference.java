package com.freearcanes.slayergear;

public enum PrayerRestorePreference
{
	PRAYER_POTION("Prayer potion"),
	SUPER_RESTORE("Super restore");

	private final String label;

	PrayerRestorePreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
