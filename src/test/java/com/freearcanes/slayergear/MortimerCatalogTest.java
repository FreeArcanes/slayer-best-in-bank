package com.freearcanes.slayergear;

import java.util.List;
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
	}

	private static SlayerMasterCatalog.MasterAssignment find(
		List<SlayerMasterCatalog.MasterAssignment> assignments, String task)
	{
		return assignments.stream()
			.filter(assignment -> assignment.getTask().equals(task))
			.findFirst().orElseThrow();
	}
}
