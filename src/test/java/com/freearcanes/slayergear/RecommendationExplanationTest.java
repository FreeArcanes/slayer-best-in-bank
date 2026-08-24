package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecommendationExplanationTest
{
	@Test
	public void usesSolverReasonWhenAvailable()
	{
		GearRecommendation recommendation = GearRecommendation.builder()
			.itemName("Test item").reason("Target weakness, +12 ranged").build();
		assertEquals("Target weakness, +12 ranged",
			SlayerGearPanel.recommendationExplanation(recommendation));
	}

	@Test
	public void safelyExplainsMissingReason()
	{
		GearRecommendation recommendation = GearRecommendation.builder()
			.itemName("Test item").build();
		assertEquals("Selected as the strongest valid owned option for this objective.",
			SlayerGearPanel.recommendationExplanation(recommendation));
	}
}
