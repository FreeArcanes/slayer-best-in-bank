package com.freearcanes.slayergear;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

	@Test
	public void checklistIncludesOnlyUnpackedTierOneGearAndOutstandingSupplies()
	{
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot =
			new EnumMap<>(EquipmentInventorySlot.class);
		bySlot.put(EquipmentInventorySlot.WEAPON, Collections.singletonList(
			GearRecommendation.builder().itemId(1).itemName("Abyssal whip")
				.slot(EquipmentInventorySlot.WEAPON).rank(1).banked(true).build()));
		SupplyRecommendation prayer = new SupplyRecommendation(2, 2,
			"Prayer potion(4)", "Prayer restore", "test", SupplyStatus.BANKED,
			true, 8, 0, 8, "doses");
		GearRecommendations recommendations = GearRecommendations.ready(
			"Test", 10, null, GearStrategy.builder().name("Melee").build(),
			Collections.emptyList(), bySlot, Collections.emptyList(),
			Collections.emptyList(), Collections.singletonList(prayer),
			ReadinessReport.empty(), 2);

		List<String> lines = SlayerGearPanel.withdrawalChecklistLines(recommendations);

		assertTrue(lines.contains("Withdraw Abyssal whip"));
		assertTrue(lines.contains("Withdraw 2 Prayer potion(4)"));
	}

	@Test
	public void checklistSeparatesAvailableAndMissingSupplyQuantities()
	{
		SupplyRecommendation prayer = new SupplyRecommendation(2, 2,
			"Prayer potion(4)", "Prayer restore", "test", SupplyStatus.PACKED_BANKED,
			true, 12, 4, 4, "doses");
		GearRecommendations recommendations = GearRecommendations.ready(
			"Test", 10, null, GearStrategy.builder().name("Melee").build(),
			Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(),
			Collections.emptyList(), Collections.singletonList(prayer),
			ReadinessReport.empty(), 1);

		List<String> lines = SlayerGearPanel.withdrawalChecklistLines(recommendations);

		assertTrue(lines.contains("Withdraw 1 Prayer potion(4)"));
		assertTrue(lines.contains("Missing 4 doses Prayer potion(4)"));
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
