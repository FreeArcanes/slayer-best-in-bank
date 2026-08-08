package com.freearcanes.slayergear;

import java.util.List;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MortimerCatalogTest
{
	@Test
	public void mortimerUsesTheCompleteLaunchAssignmentPool()
	{
		List<String> tasks = SlayerMasterCatalog.allAssignments().get("Mortimer");
		assertEquals(29, tasks.size());
		assertEquals("Crawling hands", tasks.get(0));
		assertEquals("Hydras", tasks.get(tasks.size() - 1));
		assertTrue(tasks.contains("Custodian stalkers"));
		assertTrue(tasks.contains("Venators"));
		assertTrue(SlayerMasterCatalog.mastersFor("Venator").contains("Mortimer"));
		assertTrue(SlayerMasterCatalog.mastersFor("Bloodveld").contains("Mortimer"));
	}

	@Test
	public void mortimerAssignmentLevelsWeightsAndQuantitiesMatchLaunchData()
	{
		List<SlayerMasterCatalog.MasterAssignment> assignments =
			SlayerMasterCatalog.detailedAssignmentsFor("Mortimer");

		SlayerMasterCatalog.MasterAssignment venators = find(assignments, "Venators");
		assertEquals(74, venators.getSlayerLevel());
		assertEquals(10, venators.getWeight());
		assertEquals(120, venators.getMinimum());
		assertEquals(180, venators.getMaximum());
		assertTrue(venators.isExtendable());

		SlayerMasterCatalog.MasterAssignment smoke = find(assignments, "Smoke devils");
		assertEquals(93, smoke.getSlayerLevel());
		assertEquals(8, smoke.getWeight());
		assertEquals(80, smoke.getMinimum());
		assertEquals(120, smoke.getMaximum());
		assertFalse(smoke.isExtendable());
	}

	@Test
	public void mortimerSpecialRulesAreRecorded()
	{
		SlayerMasterCatalog.MasterRules rules = SlayerMasterCatalog.rulesFor("Mortimer");
		assertEquals("Wyrmscraig Caverns", rules.getLocation());
		assertTrue(rules.getAccessRequirement().contains("Fallen From Grace"));
		assertEquals(70, rules.getMinimumSlayer());
		assertEquals(100, rules.getMinimumCombat());
		assertTrue(rules.isSlayerCapeBypass());
		assertFalse(rules.isAwardsBasePoints());
		assertEquals(2, rules.getInitialChoices());
		assertEquals(3, rules.getUnlockedChoices());
		assertEquals(50, rules.getChoicesUnlockAt());
		assertEquals(15, rules.getClueModifierUnlockAt());
		assertEquals(25, rules.getSuperiorUniqueModifierUnlockAt());
		assertEquals(40, rules.getXpModifierUnlockAt());
		assertEquals(100, rules.getCancelCost());
		assertEquals(2, rules.getBlockSlots());
		assertEquals(120, rules.getBlockCost());
		assertFalse(rules.isTuraelResetAllowed());
	}

	@Test
	public void venatorsHaveDedicatedVampyreMethodsNotVenatorBowLogic()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Venators").orElseThrow();
		assertEquals("venators", profile.getKey());
		assertEquals(WeaponRule.VAMPYRE, TaskCombatCatalog.ruleFor("Venator"));
		assertTrue(TaskCombatCatalog.traitsFor("Venators").contains(TargetTrait.VAMPYRE));

		GearStrategy ranged = profile.getStrategies().stream()
			.filter(strategy -> strategy.getCombatStyle() == CombatStyle.RANGED)
			.findFirst().orElseThrow();
		assertTrue(NameMatcher.matchesAnyToken("Blisterwood stakes", ranged.getRequiredWeapon()));
		assertFalse(NameMatcher.matchesAnyToken("Venator bow", ranged.getRequiredWeapon()));
		assertTrue(GearScorer.usesNoAmmoSlot("Blisterwood stakes"));

		GearStrategy melee = profile.getStrategies().stream()
			.filter(strategy -> strategy.getCombatStyle() == CombatStyle.MELEE)
			.findFirst().orElseThrow();
		assertEquals(WeaponRule.VAMPYRE, melee.getWeaponRule());
		assertTrue(melee.getPreferredItems().contains("sunspear"));
		assertEquals("venators", TaskProfiles.find("Venator").orElseThrow().getKey());

		ItemEquipmentStats noxiousStats = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.aslash(105)
			.str(86)
			.aspeed(5)
			.build();
		GearScorer.BankEquipment noxiousHalberd = new GearScorer.BankEquipment(
			1, 1, "Noxious halberd", EquipmentInventorySlot.WEAPON,
			noxiousStats, true, false);
		assertFalse("Noxious halberd cannot damage a Venator",
			GearScorer.allowed(noxiousHalberd, melee));
	}

	@Test
	public void efaritaysAidOutranksStrengthRingsForVenatorsOnly()
	{
		GearStrategy venator = TaskProfiles.find("Venator").orElseThrow()
			.getStrategies().stream()
			.filter(strategy -> strategy.getCombatStyle() == CombatStyle.MELEE)
			.findFirst().orElseThrow();
		ItemEquipmentStats aidStats = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.RING.getSlotIdx())
			.build();
		ItemEquipmentStats berserkerStats = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.RING.getSlotIdx())
			.str(8)
			.build();
		ItemEquipmentStats ultorStats = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.RING.getSlotIdx())
			.str(12)
			.build();

		double aid = GearScorer.scoreStats(venator, "Efaritay's aid",
			EquipmentInventorySlot.RING, aidStats);
		double berserker = GearScorer.scoreStats(venator, "Berserker ring (i)",
			EquipmentInventorySlot.RING, berserkerStats);
		double ultor = GearScorer.scoreStats(venator, "Ultor ring",
			EquipmentInventorySlot.RING, ultorStats);
		assertTrue(aid > berserker);
		assertTrue(aid > ultor);

		GearStrategy ordinary = TaskProfiles.find("Bloodveld").orElseThrow()
			.getStrategies().stream()
			.filter(strategy -> strategy.getCombatStyle() == CombatStyle.MELEE)
			.findFirst().orElseThrow();
		double offTargetAid = GearScorer.scoreStats(ordinary, "Efaritay's aid",
			EquipmentInventorySlot.RING, aidStats);
		double offTargetBerserker = GearScorer.scoreStats(ordinary, "Berserker ring (i)",
			EquipmentInventorySlot.RING, berserkerStats);
		assertTrue(offTargetBerserker > offTargetAid);
	}

	private static SlayerMasterCatalog.MasterAssignment find(
		List<SlayerMasterCatalog.MasterAssignment> assignments, String task)
	{
		return assignments.stream()
			.filter(assignment -> assignment.getTask().equals(task))
			.findFirst().orElseThrow();
	}
}
