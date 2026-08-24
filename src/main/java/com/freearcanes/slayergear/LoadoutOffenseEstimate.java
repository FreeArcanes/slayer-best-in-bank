package com.freearcanes.slayergear;

/** A comparable, style-aware offensive estimate for one coherent loadout. */
final class LoadoutOffenseEstimate
{
	private static final LoadoutOffenseEstimate UNAVAILABLE =
		new LoadoutOffenseEstimate(0, 0, false);

	private final double damagePerSecond;
	private final double maximumDamagePerSecond;
	private final double relativePercent;
	private final boolean available;
	private final String targetName;

	private LoadoutOffenseEstimate(double damagePerSecond, double relativePercent, boolean available)
	{
		this(damagePerSecond, damagePerSecond, relativePercent, available, "");
	}

	private LoadoutOffenseEstimate(double damagePerSecond, double maximumDamagePerSecond, double relativePercent,
		boolean available, String targetName)
	{
		this.damagePerSecond = Math.max(0, damagePerSecond);
		this.maximumDamagePerSecond = Math.max(this.damagePerSecond, maximumDamagePerSecond);
		this.relativePercent = relativePercent;
		this.available = available;
		this.targetName = targetName == null ? "" : targetName;
	}

	static LoadoutOffenseEstimate unavailable()
	{
		return UNAVAILABLE;
	}

	static LoadoutOffenseEstimate estimated(
		double damagePerSecond, double relativePercent, String targetName)
	{
		return new LoadoutOffenseEstimate(damagePerSecond, damagePerSecond, relativePercent, true, targetName);
	}

	static LoadoutOffenseEstimate range(double minimumDamagePerSecond,
		double maximumDamagePerSecond, double relativePercent, String targetName)
	{
		return new LoadoutOffenseEstimate(minimumDamagePerSecond, maximumDamagePerSecond,
			relativePercent, true, targetName);
	}

	static LoadoutOffenseEstimate estimated(double damagePerSecond, double relativePercent)
	{
		return estimated(damagePerSecond, relativePercent, "");
	}

	double getDamagePerSecond() { return damagePerSecond; }
	double getMaximumDamagePerSecond() { return maximumDamagePerSecond; }
	boolean isRange() { return maximumDamagePerSecond > damagePerSecond + 0.0005; }
	double getRelativePercent() { return relativePercent; }
	boolean isAvailable() { return available; }
	String getTargetName() { return targetName; }
}
