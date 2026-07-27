package com.freearcanes.slayergear;

import java.util.Collections;
import java.util.List;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SlayerGearPanelTest
{
	@Test
	public void tierOneChoiceIsPartOfTheActiveLoadout()
	{
		assertTrue(SlayerGearPanel.hasTierOneChoice(
			List.of(recommendation(EquipmentInventorySlot.AMMO, 1))));
	}

	@Test
	public void alternativeOnlySlotIsNotPartOfTheActiveLoadout()
	{
		assertFalse(SlayerGearPanel.hasTierOneChoice(
			List.of(recommendation(EquipmentInventorySlot.AMMO, 2))));
	}

	@Test
	public void backupDefenderIsNotPartOfTwoHandedTierOneLoadout()
	{
		assertFalse(SlayerGearPanel.hasTierOneChoice(
			List.of(recommendation(EquipmentInventorySlot.SHIELD, 2))));
	}

	@Test
	public void emptySlotIsNotPartOfTheActiveLoadout()
	{
		assertFalse(SlayerGearPanel.hasTierOneChoice(Collections.emptyList()));
	}

	private static GearRecommendation recommendation(EquipmentInventorySlot slot, int rank)
	{
		return GearRecommendation.builder()
			.itemId(1)
			.itemName("Test item")
			.slot(slot)
			.rank(rank)
			.build();
	}
}
