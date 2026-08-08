package com.freearcanes.slayergear;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TuraelSpeedProfilesTest
{
	@Test
	public void currentTuraelAyaTaskListHasFastRoutes()
	{
		List<String> tasks = Arrays.asList(
			"Banshees", "Bats", "Bears", "Birds", "Cave bugs",
			"Cave crawlers", "Cave slime", "Cows", "Crawling Hands",
			"Dogs", "Dwarves", "Ghosts", "Goblins", "Icefiends",
			"Kalphites", "Lizards", "Minotaurs", "Monkeys", "Rats",
			"Scorpions", "Skeletons", "Spiders", "Wolves", "Zombies");

		assertEquals(24, TuraelSpeedProfiles.supportedTaskCount());
		for (String task : tasks)
		{
			assertTrue(task, TuraelSpeedProfiles.supports(task));
			SlayerTaskProfile profile = TaskProfiles.find(task, null, true).orElseThrow();
			assertTrue(task, TuraelSpeedProfiles.isSpeedStrategy(profile.getStrategies().get(0)));
		}
	}

	@Test
	public void speedModeTargetsLowLevelSpiderRouteInsteadOfBossVariant()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Spiders", null, true).orElseThrow();
		GearStrategy blowpipe = profile.getStrategies().get(0);

		assertEquals("Outside the H.A.M. Hideout", blowpipe.getLocation());
		assertEquals("toxic blowpipe", blowpipe.getRequiredWeapon());
		assertTrue(NameMatcher.normalize(profile.getSummary()).contains("do not substitute a boss"));
		assertFalse(NameMatcher.normalize(blowpipe.getLocation()).contains("araxxor"));
	}

	@Test
	public void cannonIsOnlyAttachedToSupportedFastRoutes()
	{
		GearStrategy bats = TaskProfiles.find("Bats", null, true).orElseThrow()
			.getStrategies().get(0);
		GearStrategy banshees = TaskProfiles.find("Banshees", null, true).orElseThrow()
			.getStrategies().get(0);

		assertTrue(SmartSupplyAdvisor.isCannon(bats));
		assertFalse(SmartSupplyAdvisor.isCannon(banshees));
	}

	@Test
	public void unsupportedMilestoneTaskKeepsItsOrdinaryProfile()
	{
		SlayerTaskProfile ordinary = TaskProfiles.find("Bloodveld").orElseThrow();
		SlayerTaskProfile selected = TuraelSpeedProfiles.apply(ordinary, "Bloodveld");

		assertSame(ordinary, selected);
	}

	@Test
	public void speedModeAlwaysPacksExpeditiousInsteadOfSlaughter()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig()
		{
			@Override
			public boolean useSlayerBracelet()
			{
				return false;
			}
		};
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, config);
		SlayerTaskProfile profile = TaskProfiles.find("Banshees", null, true).orElseThrow();
		List<SmartSupplyAdvisor.SupplyRule> braceletRules = advisor.buildRules(
			profile,
			profile.getStrategies().get(0)).stream()
			.filter(rule -> "Slayer bracelet".equals(rule.getCategory()))
			.collect(Collectors.toList());

		assertEquals(1, braceletRules.size());
		assertEquals(List.of("expeditious bracelet"),
			braceletRules.get(0).getPreferredNames());
	}
}
