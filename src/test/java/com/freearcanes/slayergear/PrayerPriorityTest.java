package com.freearcanes.slayergear;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PrayerPriorityTest
{
	@Test
	public void prayerFirstPrefersPrayerArmorOverSmallStrengthBonus()
	{
		GearStrategy strategy = TaskProfiles.find("Bloodveld")
			.orElseThrow()
			.getStrategies()
			.stream()
			.filter(s -> s.getCombatStyle() == CombatStyle.MELEE)
			.findFirst()
			.orElseThrow();

		ItemEquipmentStats proselyte = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.prayer(8)
			.build();

		ItemEquipmentStats fighterTorso = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.str(4)
			.build();

		double prayerProselyte = GearScorer.scoreStats(
			strategy, "Proselyte hauberk", EquipmentInventorySlot.BODY,
			proselyte, GearPriority.PRAYER_FIRST);
		double prayerTorso = GearScorer.scoreStats(
			strategy, "Fighter torso", EquipmentInventorySlot.BODY,
			fighterTorso, GearPriority.PRAYER_FIRST);

		assertTrue(prayerProselyte > prayerTorso);
	}

	@Test
	public void balancedModeKeepsCurrentOffensivePreference()
	{
		GearStrategy strategy = TaskProfiles.find("Bloodveld")
			.orElseThrow()
			.getStrategies()
			.stream()
			.filter(s -> s.getCombatStyle() == CombatStyle.MELEE)
			.findFirst()
			.orElseThrow();

		ItemEquipmentStats prayerBody = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.prayer(8)
			.build();

		ItemEquipmentStats strengthBody = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.str(4)
			.build();

		double prayerScore = GearScorer.scoreStats(
			strategy, "Prayer body", EquipmentInventorySlot.BODY,
			prayerBody, GearPriority.BALANCED);
		double strengthScore = GearScorer.scoreStats(
			strategy, "Strength body", EquipmentInventorySlot.BODY,
			strengthBody, GearPriority.BALANCED);

		assertTrue(strengthScore > prayerScore);
	}

	@Test
	public void prayerFirstStillProtectsGargoyleTargetWeaponLogic()
	{
		GearStrategy gargoyle = TaskProfiles.find("Gargoyles")
			.orElseThrow()
			.getStrategies()
			.get(0);

		ItemEquipmentStats granite = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.acrush(57)
			.str(56)
			.aspeed(4)
			.build();

		ItemEquipmentStats halberd = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.astab(85)
			.aslash(92)
			.str(118)
			.aspeed(7)
			.build();

		double graniteScore = GearScorer.scoreStats(
			gargoyle, "Granite hammer", EquipmentInventorySlot.WEAPON,
			granite, GearPriority.PRAYER_FIRST);
		double halberdScore = GearScorer.scoreStats(
			gargoyle, "Crystal halberd", EquipmentInventorySlot.WEAPON,
			halberd, GearPriority.PRAYER_FIRST);

		assertTrue(graniteScore > halberdScore);
	}

	@Test
	public void prayerFirstPrefersHigherPrayerAccessory()
	{
		GearStrategy strategy = TaskProfiles.find("Vampyres")
			.orElseThrow()
			.getStrategies()
			.get(0);

		ItemEquipmentStats highPrayer = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.RING.getSlotIdx())
			.prayer(8)
			.build();

		ItemEquipmentStats lowerPrayer = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.RING.getSlotIdx())
			.prayer(4)
			.dmagic(20)
			.build();

		double high = GearScorer.scoreStats(
			strategy, "Ring of the gods (i)", EquipmentInventorySlot.RING,
			highPrayer, GearPriority.PRAYER_FIRST);
		double low = GearScorer.scoreStats(
			strategy, "Ring of suffering (ri)", EquipmentInventorySlot.RING,
			lowerPrayer, GearPriority.PRAYER_FIRST);

		assertTrue(high > low);
	}
}