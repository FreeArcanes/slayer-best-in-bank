package com.freearcanes.slayergear;

import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DpsEstimateOverlayTest
{
	@Test
	public void compactDisplayKeepsOnlyMethodAndDps()
	{
		LoadoutOffenseEstimate estimate = LoadoutOffenseEstimate.range(
			6.4, 6.9, 0, "Araxxor", "Soulreaper axe", 30, 34);
		List<String> lines = DpsEstimateOverlay.displayLines(
			"Soulreaper axe", estimate, false);

		assertEquals(3, lines.size());
		assertEquals("SBIB DPS ESTIMATE", lines.get(0));
		assertEquals("Soulreaper axe", lines.get(1));
		assertEquals("6.40–6.90 DPS", lines.get(2));
	}

	@Test
	public void detailedDisplayIncludesKillRateAndTarget()
	{
		LoadoutOffenseEstimate estimate = LoadoutOffenseEstimate.range(
			6.4, 6.9, 0, "Araxxor", "Soulreaper axe", 30, 34);
		List<String> lines = DpsEstimateOverlay.displayLines(
			"Soulreaper axe", estimate, true);

		assertEquals(5, lines.size());
		assertTrue(lines.get(3).contains("kills/hr"));
		assertEquals("Target: Araxxor", lines.get(4));
	}
}
