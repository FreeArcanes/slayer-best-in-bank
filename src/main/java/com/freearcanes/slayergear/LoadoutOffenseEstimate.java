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
	private final String methodName;
	private final double minimumSecondsPerKill;
	private final double maximumSecondsPerKill;

	private LoadoutOffenseEstimate(double damagePerSecond, double relativePercent, boolean available)
	{
		this(damagePerSecond, damagePerSecond, relativePercent, available, "", "", 0, 0);
	}

	private LoadoutOffenseEstimate(double damagePerSecond, double maximumDamagePerSecond, double relativePercent,
		boolean available, String targetName, String methodName,
		double minimumSecondsPerKill, double maximumSecondsPerKill)
	{
		this.damagePerSecond = Math.max(0, damagePerSecond);
		this.maximumDamagePerSecond = Math.max(this.damagePerSecond, maximumDamagePerSecond);
		this.relativePercent = relativePercent;
		this.available = available;
		this.targetName = targetName == null ? "" : targetName;
		this.methodName = methodName == null ? "" : methodName;
		this.minimumSecondsPerKill = Math.max(0, minimumSecondsPerKill);
		this.maximumSecondsPerKill = Math.max(this.minimumSecondsPerKill, maximumSecondsPerKill);
	}

	static LoadoutOffenseEstimate unavailable()
	{
		return UNAVAILABLE;
	}

	static LoadoutOffenseEstimate estimated(
		double damagePerSecond, double relativePercent, String targetName)
	{
		return new LoadoutOffenseEstimate(damagePerSecond, damagePerSecond,
			relativePercent, true, targetName, "", 0, 0);
	}

	static LoadoutOffenseEstimate range(double minimumDamagePerSecond,
		double maximumDamagePerSecond, double relativePercent, String targetName)
	{
		return range(minimumDamagePerSecond, maximumDamagePerSecond,
			relativePercent, targetName, "");
	}

	static LoadoutOffenseEstimate range(double minimumDamagePerSecond,
		double maximumDamagePerSecond, double relativePercent, String targetName,
		String methodName)
	{
		return new LoadoutOffenseEstimate(minimumDamagePerSecond, maximumDamagePerSecond,
			relativePercent, true, targetName, methodName, 0, 0);
	}

	static LoadoutOffenseEstimate range(double minimumDamagePerSecond,
		double maximumDamagePerSecond, double relativePercent, String targetName,
		String methodName, double minimumSecondsPerKill, double maximumSecondsPerKill)
	{
		return new LoadoutOffenseEstimate(minimumDamagePerSecond, maximumDamagePerSecond,
			relativePercent, true, targetName, methodName,
			minimumSecondsPerKill, maximumSecondsPerKill);
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
	String getMethodName() { return methodName; }
	boolean hasKillRate() { return minimumSecondsPerKill > 0; }
	double getMinimumSecondsPerKill() { return minimumSecondsPerKill; }
	double getMaximumSecondsPerKill() { return maximumSecondsPerKill; }
	double getMinimumKillsPerHour() { return maximumSecondsPerKill > 0 ? 3600.0 / maximumSecondsPerKill : 0; }
	double getMaximumKillsPerHour() { return minimumSecondsPerKill > 0 ? 3600.0 / minimumSecondsPerKill : 0; }
}
