package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TripCostEstimate
{
	private final long totalGp;
	private final int plannedKills;
	private final List<String> breakdown;

	TripCostEstimate(long totalGp, int plannedKills, List<String> breakdown)
	{
		this.totalGp = Math.max(0, totalGp);
		this.plannedKills = Math.max(0, plannedKills);
		this.breakdown = breakdown == null ? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(breakdown));
	}

	long getTotalGp() { return totalGp; }
	int getPlannedKills() { return plannedKills; }
	long getGpPerKill() { return plannedKills <= 0 ? 0 : (totalGp + plannedKills - 1) / plannedKills; }
	List<String> getBreakdown() { return breakdown; }
	boolean isAvailable() { return totalGp > 0 && plannedKills > 0; }
}
