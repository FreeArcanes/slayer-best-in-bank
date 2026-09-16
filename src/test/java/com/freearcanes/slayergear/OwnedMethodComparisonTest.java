package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OwnedMethodComparisonTest
{
	@Test
	public void normalizesValuesAndCalculatesAverageDps()
	{
		OwnedMethodComparison comparison = new OwnedMethodComparison(
			"Ranged", CombatStyle.RANGED, null,
			LoadoutOffenseEstimate.range(6, 8, 0, "target", "method", 10, 12),
			-10, 250, GearPriority.VALUE, true, false);

		assertEquals(7.0, comparison.averageDps(), 0.001);
		assertEquals(0, comparison.getTripGp());
		assertEquals("", comparison.getLocation());
		assertTrue(comparison.isSelected());
		assertFalse(comparison.isFastest());
		assertTrue(comparison.withFastest(true).isFastest());
	}

	@Test
	public void panelMetricsIncludeDpsKillRateAndCost()
	{
		OwnedMethodComparison comparison = new OwnedMethodComparison(
			"Magic", CombatStyle.MAGIC, "Catacombs",
			LoadoutOffenseEstimate.range(5, 5, 0, "target", "method", 20, 20),
			12_000, 300, GearPriority.BALANCED, false, true);

		String text = SlayerGearPanel.comparisonMetricsText(comparison);
		assertTrue(text.contains("5.00 DPS"));
		assertTrue(text.contains("kills/hr"));
		assertTrue(text.contains("300 gp/kill"));
		assertTrue(text.contains("12.0k trip"));
	}
}
