package com.freearcanes.slayergear;

/** Complete owned-loadout result for one eligible combat method. */
final class OwnedMethodComparison
{
	private final String strategyName;
	private final CombatStyle combatStyle;
	private final String location;
	private final LoadoutOffenseEstimate offenseEstimate;
	private final long tripGp;
	private final long gpPerKill;
	private final GearPriority suggestedObjective;
	private final boolean selected;
	private final boolean fastest;
	private final boolean objectiveBest;
	private final String objectiveReason;

	OwnedMethodComparison(String strategyName, CombatStyle combatStyle,
		String location, LoadoutOffenseEstimate offenseEstimate,
		long tripGp, long gpPerKill, GearPriority suggestedObjective,
		boolean selected, boolean fastest)
	{
		this(strategyName, combatStyle, location, offenseEstimate, tripGp,
			gpPerKill, suggestedObjective, selected, fastest, false, "");
	}

	OwnedMethodComparison(String strategyName, CombatStyle combatStyle,
		String location, LoadoutOffenseEstimate offenseEstimate,
		long tripGp, long gpPerKill, GearPriority suggestedObjective,
		boolean selected, boolean fastest, boolean objectiveBest,
		String objectiveReason)
	{
		this.strategyName = strategyName == null ? "" : strategyName;
		this.combatStyle = combatStyle == null ? CombatStyle.MELEE : combatStyle;
		this.location = location == null ? "" : location;
		this.offenseEstimate = offenseEstimate == null
			? LoadoutOffenseEstimate.unavailable() : offenseEstimate;
		this.tripGp = Math.max(0, tripGp);
		this.gpPerKill = Math.max(0, gpPerKill);
		this.suggestedObjective = suggestedObjective == null
			? GearPriority.BALANCED : suggestedObjective;
		this.selected = selected;
		this.fastest = fastest;
		this.objectiveBest = objectiveBest;
		this.objectiveReason = objectiveReason == null ? "" : objectiveReason;
	}

	OwnedMethodComparison withFastest(boolean value)
	{
		return new OwnedMethodComparison(strategyName, combatStyle, location,
			offenseEstimate, tripGp, gpPerKill, suggestedObjective, selected, value,
			objectiveBest, objectiveReason);
	}

	OwnedMethodComparison withObjectiveVerdict(boolean best, String reason)
	{
		return new OwnedMethodComparison(strategyName, combatStyle, location,
			offenseEstimate, tripGp, gpPerKill, suggestedObjective, selected, fastest,
			best, reason);
	}

	double averageDps()
	{
		return offenseEstimate.isAvailable()
			? (offenseEstimate.getDamagePerSecond()
				+ offenseEstimate.getMaximumDamagePerSecond()) / 2.0 : 0;
	}

	String getStrategyName() { return strategyName; }
	CombatStyle getCombatStyle() { return combatStyle; }
	String getLocation() { return location; }
	LoadoutOffenseEstimate getOffenseEstimate() { return offenseEstimate; }
	long getTripGp() { return tripGp; }
	long getGpPerKill() { return gpPerKill; }
	GearPriority getSuggestedObjective() { return suggestedObjective; }
	boolean isSelected() { return selected; }
	boolean isFastest() { return fastest; }
	boolean isObjectiveBest() { return objectiveBest; }
	String getObjectiveReason() { return objectiveReason; }
}
