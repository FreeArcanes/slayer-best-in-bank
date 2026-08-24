package com.freearcanes.slayergear;

final class ObjectiveLoadoutComparison
{
	private final GearPriority objective;
	private final String gearChanges;

	ObjectiveLoadoutComparison(GearPriority objective, String gearChanges)
	{
		this.objective = objective;
		this.gearChanges = gearChanges == null ? "" : gearChanges;
	}

	GearPriority getObjective() { return objective; }
	String getGearChanges() { return gearChanges; }
}
