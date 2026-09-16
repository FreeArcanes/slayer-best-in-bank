package com.freearcanes.slayergear;

import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MethodComparisonAdvisorTest
{
	@Test
	public void maxDpsSelectsFastestMeasuredMethod()
	{
		List<OwnedMethodComparison> ranked = MethodComparisonAdvisor.apply(List.of(
			method("Cheap", 8, 100, GearPriority.VALUE),
			method("Fast", 10, 500, GearPriority.BALANCED)), GearPriority.BALANCED);

		assertEquals("Fast", winner(ranked).getStrategyName());
		assertTrue(winner(ranked).getObjectiveReason().contains("Highest measured DPS"));
	}

	@Test
	public void valueChoosesCheapestMethodWithinDpsFloor()
	{
		List<OwnedMethodComparison> ranked = MethodComparisonAdvisor.apply(List.of(
			method("Too slow", 8.4, 20, GearPriority.VALUE),
			method("Efficient", 9, 100, GearPriority.VALUE),
			method("Fast", 10, 500, GearPriority.BALANCED)), GearPriority.VALUE);

		assertEquals("Efficient", winner(ranked).getStrategyName());
		assertTrue(winner(ranked).getObjectiveReason().contains("85%"));
	}

	@Test
	public void sustainObjectiveUsesPolicyMatchBeforeDps()
	{
		List<OwnedMethodComparison> ranked = MethodComparisonAdvisor.apply(List.of(
			method("Fast", 10, 500, GearPriority.BALANCED),
			method("Prayer", 7, 400, GearPriority.PRAYER_FIRST)),
			GearPriority.PRAYER_FIRST);

		assertEquals("Prayer", winner(ranked).getStrategyName());
	}

	private static OwnedMethodComparison method(
		String name, double dps, long gpPerKill, GearPriority suggested)
	{
		return new OwnedMethodComparison(name, CombatStyle.MELEE, "",
			LoadoutOffenseEstimate.range(dps, dps, 0, "target", "method", 10, 10),
			gpPerKill * 10, gpPerKill, suggested, false, false);
	}

	private static OwnedMethodComparison winner(List<OwnedMethodComparison> comparisons)
	{
		return comparisons.stream().filter(OwnedMethodComparison::isObjectiveBest)
			.findFirst().orElseThrow(AssertionError::new);
	}
}
