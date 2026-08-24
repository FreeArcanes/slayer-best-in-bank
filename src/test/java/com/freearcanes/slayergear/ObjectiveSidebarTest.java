package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObjectiveSidebarTest
{
	@Test
	public void summariesDescribeBothGearAndSupplyEffect()
	{
		assertTrue(SlayerGearPanel.objectiveSummary(GearPriority.BALANCED)
			.contains("DPS"));
		assertTrue(SlayerGearPanel.objectiveSummary(GearPriority.BALANCED)
			.contains("boosts"));
		assertTrue(SlayerGearPanel.objectiveSummary(GearPriority.PRAYER_FIRST)
			.contains("Prayer-bonus gear"));
		assertTrue(SlayerGearPanel.objectiveSummary(GearPriority.PRAYER_FIRST)
			.contains("restoration"));
		assertTrue(SlayerGearPanel.objectiveSummary(GearPriority.DEFENCE_FIRST)
			.contains("defensive armour"));
		assertTrue(SlayerGearPanel.objectiveSummary(GearPriority.DEFENCE_FIRST)
			.contains("food"));
		assertTrue(SlayerGearPanel.objectiveSummary(GearPriority.VALUE)
			.contains("reducing optional"));
	}

	@Test
	public void recommendationCarriesSelectedObjectiveThroughPanelState()
	{
		GearRecommendations recommendations = GearRecommendations.ready(
			"Test", 10, null,
			GearStrategy.builder().name("Test").combatStyle(CombatStyle.MELEE).build(),
			null, null, null, null, null, ReadinessReport.empty(), 0,
			GearPriority.DEFENCE_FIRST);
		assertEquals(GearPriority.DEFENCE_FIRST, recommendations.getObjective());
		assertEquals(GearPriority.DEFENCE_FIRST,
			recommendations.withBankSessionState(true, false).getObjective());
	}
}
