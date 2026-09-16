package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Selects one transparent Objective-aware verdict from already-scored methods. */
final class MethodComparisonAdvisor
{
	private static final double VALUE_DPS_FLOOR = 0.85;

	private MethodComparisonAdvisor() {}

	static List<OwnedMethodComparison> apply(
		List<OwnedMethodComparison> comparisons, GearPriority objective)
	{
		if (comparisons == null || comparisons.isEmpty()) return Collections.emptyList();
		GearPriority selectedObjective = objective == null ? GearPriority.BALANCED : objective;
		double fastestDps = comparisons.stream()
			.mapToDouble(OwnedMethodComparison::averageDps).max().orElse(0);
		int winner = selectedObjective == GearPriority.VALUE
			? valueWinner(comparisons, fastestDps)
			: sustainWinner(comparisons, selectedObjective);
		if (selectedObjective == GearPriority.BALANCED || winner < 0)
		{
			winner = fastestWinner(comparisons);
		}
		String reason = reason(selectedObjective, winner >= 0 ? comparisons.get(winner) : null,
			fastestDps);
		List<OwnedMethodComparison> result = new ArrayList<>(comparisons.size());
		for (int index = 0; index < comparisons.size(); index++)
		{
			result.add(comparisons.get(index).withObjectiveVerdict(index == winner,
				index == winner ? reason : ""));
		}
		return Collections.unmodifiableList(result);
	}

	private static int fastestWinner(List<OwnedMethodComparison> comparisons)
	{
		int winner = -1;
		double best = 0;
		for (int index = 0; index < comparisons.size(); index++)
		{
			double dps = comparisons.get(index).averageDps();
			if (dps > best) { best = dps; winner = index; }
		}
		return winner;
	}

	private static int valueWinner(
		List<OwnedMethodComparison> comparisons, double fastestDps)
	{
		int winner = -1;
		long lowestCost = Long.MAX_VALUE;
		for (int index = 0; index < comparisons.size(); index++)
		{
			OwnedMethodComparison candidate = comparisons.get(index);
			if (candidate.getGpPerKill() <= 0 || candidate.averageDps() <= 0
				|| candidate.averageDps() < fastestDps * VALUE_DPS_FLOOR) continue;
			if (candidate.getGpPerKill() < lowestCost)
			{
				lowestCost = candidate.getGpPerKill();
				winner = index;
			}
		}
		return winner;
	}

	private static int sustainWinner(
		List<OwnedMethodComparison> comparisons, GearPriority objective)
	{
		if (objective == GearPriority.BALANCED || objective == GearPriority.VALUE) return -1;
		int winner = -1;
		double bestDps = 0;
		for (int index = 0; index < comparisons.size(); index++)
		{
			OwnedMethodComparison candidate = comparisons.get(index);
			if (candidate.getSuggestedObjective() != objective) continue;
			if (candidate.averageDps() > bestDps)
			{
				bestDps = candidate.averageDps();
				winner = index;
			}
		}
		return winner;
	}

	private static String reason(
		GearPriority objective, OwnedMethodComparison winner, double fastestDps)
	{
		if (winner == null) return "No measured method verdict is available.";
		if (objective == GearPriority.VALUE && winner.getGpPerKill() > 0)
		{
			return "Lowest modeled GP/kill among methods retaining at least 85% of the fastest DPS.";
		}
		if (objective == GearPriority.PRAYER_FIRST
			|| objective == GearPriority.DEFENCE_FIRST)
		{
			if (winner.getSuggestedObjective() == objective)
			{
				return "Task policy matches this Objective; DPS breaks ties between matching methods.";
			}
			return "No method has a specific policy match, so measured DPS is the fallback.";
		}
		return fastestDps > 0
			? "Highest measured DPS from your eligible owned loadouts."
			: "No measured DPS is available for another method.";
	}
}
