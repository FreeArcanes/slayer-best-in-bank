package com.freearcanes.slayergear;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TargetDefenceCatalogTest
{
	@Test
	public void bundledDatasetHasAValidHeaderAndEveryRowHasSixteenColumns() throws Exception
	{
		String resource = "/com/freearcanes/slayergear/slayer-targets.tsv";
		try (InputStream stream = TargetDefenceCatalogTest.class.getResourceAsStream(resource))
		{
			assertTrue(stream != null);
			try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream, StandardCharsets.UTF_8)))
			{
				String line;
				boolean foundHeader = false;
				while ((line = reader.readLine()) != null)
				{
					if (!line.isEmpty() && line.charAt(0) == '\uFEFF') line = line.substring(1);
					if (line.isEmpty() || line.startsWith("#")) continue;
					String[] fields = line.split("\t", -1);
					assertEquals("Malformed dataset row: " + line, 16, fields.length);
					if (!foundHeader)
					{
						assertEquals("name", fields[0]);
						assertEquals("version", fields[1]);
						foundHeader = true;
					}
				}
				assertTrue("Dataset header missing", foundHeader);
			}
		}
	}

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
	public void targetDatasetProvidesCombatIntelligenceFields()
	{
		TargetDefence abyssal = TargetDefenceCatalog.find(
			"Abyssal demons", "abyssal-demons").get(0);
		assertEquals(150, abyssal.getHitpoints());
		assertTrue(abyssal.hasAttribute("demon"));
		assertTrue(abyssal.getSize() >= 1);
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
