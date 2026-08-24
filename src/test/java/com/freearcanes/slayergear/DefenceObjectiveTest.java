package com.freearcanes.slayergear;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefenceObjectiveTest
{
	@Test
	public void objectiveNamesAreClearWhileStableEnumKeysRemain()
	{
		assertEquals("Max DPS", GearPriority.BALANCED.toString());
		assertEquals("Prayer Sustain", GearPriority.PRAYER_FIRST.toString());
		assertEquals("Defence First", GearPriority.DEFENCE_FIRST.toString());
		assertEquals("Value / Low Cost", GearPriority.VALUE.toString());
	}

	@Test
	public void defenceFirstPrefersTankBodyOverSmallStrengthBonus()
	{
		GearStrategy strategy = GearStrategy.builder().name("Melee")
			.combatStyle(CombatStyle.MELEE).build();
		ItemEquipmentStats tankBody = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.dstab(100).dslash(100).dcrush(100).drange(100).dmagic(20).build();
		ItemEquipmentStats strengthBody = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx()).str(8).build();

		double tank = GearScorer.scoreStats(strategy, "Tank body",
			EquipmentInventorySlot.BODY, tankBody, GearPriority.DEFENCE_FIRST);
		double strength = GearScorer.scoreStats(strategy, "Strength body",
			EquipmentInventorySlot.BODY, strengthBody, GearPriority.DEFENCE_FIRST);
		assertTrue(tank > strength);
	}

	@Test
	public void defenceFirstDoesNotWeakenWeaponSelection()
	{
		GearStrategy strategy = GearStrategy.builder().name("Melee")
			.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH).build();
		ItemEquipmentStats strong = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.aslash(100).str(100).aspeed(4).build();
		ItemEquipmentStats weak = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.aslash(30).str(30).aspeed(4).build();

		assertTrue(GearScorer.scoreStats(strategy, "Strong sword",
			EquipmentInventorySlot.WEAPON, strong, GearPriority.DEFENCE_FIRST)
			> GearScorer.scoreStats(strategy, "Weak sword",
				EquipmentInventorySlot.WEAPON, weak, GearPriority.DEFENCE_FIRST));
	}
}
