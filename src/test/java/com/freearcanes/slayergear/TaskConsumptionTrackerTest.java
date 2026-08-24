package com.freearcanes.slayergear;

import java.util.Collections;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TaskConsumptionTrackerTest
{
	@Test
	public void reportsObservedDoseUseKillsAndElapsedTime()
	{
		ItemManager itemManager = mock(ItemManager.class);
		ItemComposition fourDose = mock(ItemComposition.class);
		ItemComposition twoDose = mock(ItemComposition.class);
		when(fourDose.getName()).thenReturn("Prayer potion(4)");
		when(twoDose.getName()).thenReturn("Prayer potion(2)");
		when(itemManager.canonicalize(100)).thenReturn(100);
		when(itemManager.canonicalize(101)).thenReturn(100);
		when(itemManager.getItemComposition(100)).thenReturn(fourDose);
		when(itemManager.getItemComposition(101)).thenReturn(twoDose);
		SupplyRecommendation prayer = new SupplyRecommendation(100, 100,
			"Prayer potion(4)", "Prayer restore", "test", SupplyStatus.PACKED,
			true, 8, 8, 0, "doses");
		GearRecommendations recommendations = ready(prayer);
		TaskConsumptionTracker tracker = new TaskConsumptionTracker();

		tracker.start(recommendations, new Item[]{new Item(100, 2)}, itemManager, 1_000);
		tracker.observe(new Item[]{new Item(101, 2)}, itemManager, 7);
		TaskCompletionSummary summary = tracker.finish(0, 62_000);

		assertNotNull(summary);
		assertEquals(10, summary.getKills());
		assertEquals(61, summary.getElapsedSeconds());
		assertEquals(Collections.singletonList("Prayer restore 4 doses"), summary.getConsumed());
		assertFalse(tracker.isActive());
	}

	@Test
	public void selectedBossPlanStillTracksUnderlyingAssignmentIdentityAndAmount()
	{
		TaskConsumptionTracker tracker = new TaskConsumptionTracker();
		GearRecommendations bossPlan = GearRecommendations.ready("Vorkath", 1, null,
			GearStrategy.builder().name("Boss").build(), Collections.emptyList(),
			Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(),
			Collections.emptyList(), ReadinessReport.empty(), 1);

		tracker.start(bossPlan, "Blue dragons", 42, new Item[0],
			mock(ItemManager.class), 0);
		TaskCompletionSummary summary = tracker.finish(40, 10_000);

		assertNotNull(summary);
		assertEquals("Blue dragons", summary.getTaskName());
		assertEquals(2, summary.getKills());
	}

	private static GearRecommendations ready(SupplyRecommendation supply)
	{
		return GearRecommendations.ready("Test task", 10, null,
			GearStrategy.builder().name("Test").build(), Collections.emptyList(),
			Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(),
			Collections.singletonList(supply), ReadinessReport.empty(), 1);
	}
}
