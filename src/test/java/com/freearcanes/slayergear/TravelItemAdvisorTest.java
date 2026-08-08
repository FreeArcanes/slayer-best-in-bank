package com.freearcanes.slayergear;

import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TravelItemAdvisorTest
{
	@Test
	public void assignedLocationOverridesCuratedStrategyLocation()
	{
		GearStrategy strategy = GearStrategy.builder()
			.location("Catacombs of Kourend")
			.build();

		List<TravelItemAdvisor.TravelRule> rules = TravelItemAdvisor.recommend(
			"Slayer Tower",
			strategy,
			defaultConfig());

		assertTrue(hasFallback(rules, "Slayer ring"));
		assertTrue(!hasFallback(rules, "Kourend teleport"));
	}

	@Test
	public void strategyLocationIsFallbackWhenAssignmentHasNoArea()
	{
		GearStrategy strategy = GearStrategy.builder()
			.location("Fossil Island Wyvern Cave")
			.build();

		List<TravelItemAdvisor.TravelRule> rules = TravelItemAdvisor.recommend(
			"",
			strategy,
			defaultConfig());

		assertTrue(hasFallback(rules, "Digsite pendant"));
	}

	@Test
	public void homeTeleportPreferenceIsStrict()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig()
		{
			@Override
			public HomeTeleportPreference homeTeleportPreference()
			{
				return HomeTeleportPreference.CONSTRUCTION_CAPE;
			}
		};

		TravelItemAdvisor.TravelRule home = TravelItemAdvisor.recommend(
			"",
			null,
			config).get(0);

		assertEquals("Construction cape", home.getFallback());
		assertArrayEquals(
			new String[]{"construct. cape", "construction cape"},
			home.getPreferredNames());
	}

	@Test
	public void maxCapeIsAnExplicitHomeTeleportPreference()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig()
		{
			@Override
			public HomeTeleportPreference homeTeleportPreference()
			{
				return HomeTeleportPreference.MAX_CAPE;
			}
		};

		TravelItemAdvisor.TravelRule home = TravelItemAdvisor.recommend("", null, config).get(0);

		assertEquals("Max cape", home.getFallback());
		assertArrayEquals(new String[]{"max cape"}, home.getPreferredNames());
		assertTrue(SmartSupplyAdvisor.matchesPreferredSupply("max cape", "max cape"));
		assertFalse(SmartSupplyAdvisor.matchesPreferredSupply("magic cape", "max cape"));
		assertFalse(SmartSupplyAdvisor.matchesPreferredSupply(
			"imbued saradomin max cape", "max cape"));
	}

	@Test
	public void constructionCapeMatchesRuneLiteItemNames()
	{
		assertTrue(SmartSupplyAdvisor.matchesPreferredSupply(
			"construct. cape",
			"construct. cape"));
		assertTrue(SmartSupplyAdvisor.matchesPreferredSupply(
			"construct. cape(t)",
			"construct. cape"));
	}

	@Test
	public void locationFamiliesRespectPreferenceOrdering()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig()
		{
			@Override
			public SlayerRingPreference slayerRingPreference()
			{
				return SlayerRingPreference.CHARGED_FIRST;
			}

			@Override
			public KourendTeleportPreference kourendTeleportPreference()
			{
				return KourendTeleportPreference.RADAS_BLESSING_FIRST;
			}
		};

		TravelItemAdvisor.TravelRule slayerRing = find(
			TravelItemAdvisor.recommend("Slayer Tower", null, config),
			"Slayer ring");
		assertArrayEquals(
			new String[]{"slayer ring (", "eternal slayer ring"},
			slayerRing.getPreferredNames());

		TravelItemAdvisor.TravelRule kourend = find(
			TravelItemAdvisor.recommend("Catacombs of Kourend", null, config),
			"Kourend teleport");
		assertArrayEquals(
			new String[]{"rada's blessing", "xeric's talisman", "kourend castle teleport"},
			kourend.getPreferredNames());
	}

	@Test
	public void spellTeleportPreferenceChangesTrollheimOrdering()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig()
		{
			@Override
			public SpellTeleportPreference spellTeleportPreference()
			{
				return SpellTeleportPreference.RUNES_FIRST;
			}
		};

		TravelItemAdvisor.TravelRule route = find(
			TravelItemAdvisor.recommend("God Wars Dungeon", null, config),
			"Trollheim teleport");

		assertArrayEquals(
			new String[]{"law rune", "trollheim teleport"},
			route.getPreferredNames());
	}

	@Test
	public void suggestionsCanBeDisabled()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig()
		{
			@Override
			public boolean travelSuggestionsEnabled()
			{
				return false;
			}
		};

		assertTrue(TravelItemAdvisor.recommend(
			"Catacombs of Kourend",
			null,
			config).isEmpty());
	}

	@Test
	public void previouslyMissingAssignmentAreasUseCatalogRoutes()
	{
		assertTrue(hasFallback(
			TravelItemAdvisor.recommend("Jormungand's Prison", null, defaultConfig()),
			"Fremennik travel"));
		assertTrue(hasFallback(
			TravelItemAdvisor.recommend("Neypotzli", null, defaultConfig()),
			"Varlamore travel"));
		assertTrue(hasFallback(
			TravelItemAdvisor.recommend("Forthos Dungeon", null, defaultConfig()),
			"Kourend teleport"));
		assertTrue(hasFallback(
			TravelItemAdvisor.recommend("Meiyerditch Laboratories", null, defaultConfig()),
			"Drakan's medallion"));
	}

	private static SlayerGearAdvisorConfig defaultConfig()
	{
		return new SlayerGearAdvisorConfig() { };
	}

	private static boolean hasFallback(
		List<TravelItemAdvisor.TravelRule> rules,
		String fallback)
	{
		return rules.stream()
			.anyMatch(rule -> fallback.equals(rule.getFallback()));
	}

	private static TravelItemAdvisor.TravelRule find(
		List<TravelItemAdvisor.TravelRule> rules,
		String fallback)
	{
		return rules.stream()
			.filter(rule -> fallback.equals(rule.getFallback()))
			.findFirst()
			.orElseThrow();
	}
}
