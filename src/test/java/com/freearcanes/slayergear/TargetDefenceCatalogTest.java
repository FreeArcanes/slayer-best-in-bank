package com.freearcanes.slayergear;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TargetDefenceCatalogTest
{
	@Test
	public void resolvesPluralTaskAndDeduplicatesIdenticalLocationVariants()
	{
		List<TargetDefence> targets = TargetDefenceCatalog.find("Abyssal demons", "abyssal-demons");
		assertEquals(1, targets.size());
		assertEquals("Abyssal demon", targets.get(0).getName());
	}

	@Test
	public void fossilIslandWyvernsPreserveDifferentDefenceVariants()
	{
		List<TargetDefence> targets = TargetDefenceCatalog.find(
			"Fossil Island wyverns", "fossil-island-wyverns");
		assertTrue(targets.size() >= 3);
	}

	@Test
	public void unknownAssignmentDoesNotInventTargetStats()
	{
		assertTrue(TargetDefenceCatalog.find("Unknown thing", "unknown-thing").isEmpty());
	}

	@Test
	public void resolvesDifferentPluralForms()
	{
		assertTrue(!TargetDefenceCatalog.find("Aviansies", "aviansies").isEmpty());
		assertTrue(!TargetDefenceCatalog.find("Zombies", "zombies").isEmpty());
		assertTrue(!TargetDefenceCatalog.find("Elves", "elves").isEmpty());
	}

	@Test
	public void resolvesMostDistinctCuratedTaskProfiles()
	{
		Set<String> checked = new HashSet<>();
		int resolved = 0;
		for (SlayerTaskProfile profile : TaskProfiles.catalogSnapshot().values())
		{
			if (checked.add(profile.getKey()) && !TargetDefenceCatalog.find(
				profile.getDisplayName(), profile.getKey()).isEmpty())
			{
				resolved++;
			}
		}
		assertTrue("Only resolved " + resolved + " curated target families", resolved >= 45);
	}

	@Test
	public void explicitlyNamedBossStrategyOverridesBroadAssignmentTarget()
	{
		GearStrategy araxxor = GearStrategy.builder().name("Araxxor - Crush melee").build();
		List<TargetDefence> targets = TargetDefenceCatalog.find(
			"Araxytes", "araxytes", araxxor);
		assertTrue(!targets.isEmpty());
		assertEquals("Araxxor", targets.get(0).getName());
	}
}
