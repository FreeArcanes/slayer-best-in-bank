package com.freearcanes.slayergear;

/** Live player/loadout inputs used by the potion dose model. */
final class PotionEstimationContext
{
	private final String taskName;
	private final int prayerLevel;
	private final int prayerBonus;
	private final double secondsPerKill;

	PotionEstimationContext(String taskName, int prayerLevel, int prayerBonus,
		double secondsPerKill)
	{
		this.taskName = taskName == null ? "" : taskName;
		this.prayerLevel = Math.max(1, prayerLevel);
		this.prayerBonus = Math.max(0, prayerBonus);
		this.secondsPerKill = Math.max(0, secondsPerKill);
	}

	String getTaskName() { return taskName; }
	int getPrayerLevel() { return prayerLevel; }
	int getPrayerBonus() { return prayerBonus; }
	double getSecondsPerKill() { return secondsPerKill; }
}
