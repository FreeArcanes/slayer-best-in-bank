package com.freearcanes.slayergear;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CatalogConsistencyTest
{
	@Test
	public void everyRegisteredAliasResolvesToItsOriginalProfile()
	{
		Map<String, SlayerTaskProfile> catalog = TaskProfiles.catalogSnapshot();
		assertFalse(catalog.isEmpty());
		assertEquals(TaskProfiles.profileCount(),
			catalog.values().stream().distinct().count());

		for (Map.Entry<String, SlayerTaskProfile> entry : catalog.entrySet())
		{
			SlayerTaskProfile resolved = TaskProfiles.find(entry.getKey()).orElseThrow();
			assertEquals("Alias changed profile: " + entry.getKey(),
				entry.getValue().getKey(), resolved.getKey());
		}
	}

	@Test
	public void everyCuratedProfileAndStrategyHasCompleteMetadata()
	{
		Set<SlayerTaskProfile> checked = new HashSet<>();
		for (SlayerTaskProfile profile : TaskProfiles.catalogSnapshot().values())
		{
			if (!checked.add(profile)) continue;
			assertText("profile key", profile.getKey(), profile.getDisplayName());
			assertText("display name", profile.getDisplayName(), profile.getKey());
			assertText("summary", profile.getSummary(), profile.getKey());
			assertText("protection advice", profile.getProtectionAdvice(), profile.getKey());
			assertFalse("No strategies for " + profile.getKey(), profile.getStrategies().isEmpty());

			Set<String> strategyNames = new HashSet<>();
			for (GearStrategy strategy : profile.getStrategies())
			{
				assertText("strategy name", strategy.getName(), profile.getKey());
				assertTrue("Duplicate strategy name in " + profile.getKey(),
					strategyNames.add(NameMatcher.normalize(strategy.getName())));
				assertText("location", strategy.getLocation(), profile.getKey() + "/" + strategy.getName());
				assertText("rationale", strategy.getRationale(), profile.getKey() + "/" + strategy.getName());
				assertNotNull("Missing combat style in " + profile.getKey(), strategy.getCombatStyle());
				assertNotNull("Missing attack type in " + profile.getKey(), strategy.getAttackType());
				assertNotNull("Missing weapon rule in " + profile.getKey(), strategy.getWeaponRule());

				if (strategy.isAncientAoe())
				{
					assertEquals("Ancient AoE must be Magic: " + profile.getKey(),
						CombatStyle.MAGIC, strategy.getCombatStyle());
					assertTrue("Ancient AoE level must be at least 62: " + profile.getKey(),
						strategy.getMinimumMagic() >= 62);
				}
				if (strategy.getElementalWeakness() == ElementalWeakness.NONE)
				{
					assertEquals("NONE weakness cannot have a percentage: " + profile.getKey(),
						0, strategy.getElementalWeaknessPercent());
				}
				else
				{
					assertTrue("Elemental weakness needs a percentage: " + profile.getKey(),
						strategy.getElementalWeaknessPercent() > 0);
				}
				if (strategy.getRequiredOffhand() != null)
				{
					assertFalse("Off-hand strategy cannot require a known two-handed weapon: " + profile.getKey(),
						containsTwoHandedToken(strategy.getRequiredWeapon()));
				}
			}
		}
	}

	@Test
	public void everyCannonAliasHasMetadataAndHonorsNoCannonLocations()
	{
		Map<String, CannonTaskCatalog.CannonRoute> routes = CannonTaskCatalog.routesSnapshot();
		assertFalse(routes.isEmpty());
		assertEquals(CannonTaskCatalog.routeCount(),
			routes.values().stream().distinct().count());

		for (Map.Entry<String, CannonTaskCatalog.CannonRoute> entry : routes.entrySet())
		{
			CannonTaskCatalog.CannonRoute route = entry.getValue();
			assertText("cannon display name", route.getDisplayName(), entry.getKey());
			assertText("cannon location", route.getLocation(), entry.getKey());
			assertText("cannon rationale", route.getRationale(), entry.getKey());
			assertNotNull(route.getCombatStyle());
			assertNotNull(route.getAttackType());
			assertTrue("Alias does not resolve to its route: " + entry.getKey(),
				CannonTaskCatalog.find(entry.getKey()).isPresent());
			assertFalse("No-cannon location leaked route for " + entry.getKey(),
				CannonTaskCatalog.find(entry.getKey(), "Catacombs of Kourend").isPresent());
		}
	}

	@Test
	public void everySlayerMasterAssignmentHasCombatClassification()
	{
		Map<String, WeaponRule> rules = TaskCombatCatalog.weaponRulesSnapshot();
		for (Map.Entry<String, java.util.List<String>> master :
			SlayerMasterCatalog.allAssignments().entrySet())
		{
			for (String task : master.getValue())
			{
				assertTrue(master.getKey() + " task lacks combat classification: " + task,
					rules.containsKey(normalize(task)));
			}
		}
	}

	@Test
	public void specializedWeaponRulesReachResolvedStrategies()
	{
		for (Map.Entry<String, WeaponRule> entry : TaskCombatCatalog.weaponRulesSnapshot().entrySet())
		{
			if (entry.getValue() == WeaponRule.ANY) continue;
			SlayerTaskProfile profile = TaskProfiles.find(entry.getKey()).orElseThrow();
			assertTrue("Weapon rule " + entry.getValue() + " not applied to " + entry.getKey(),
				profile.getStrategies().stream()
					.anyMatch(strategy -> strategy.getWeaponRule() == entry.getValue()));
		}
	}

	@Test
	public void sharedAliasCatalogMakesCollisionPolicyExplicit()
	{
		AliasCatalog<String> catalog = new AliasCatalog<>(
			value -> value.trim().toLowerCase(Locale.ENGLISH));
		catalog.register("specific", AliasCatalog.CollisionPolicy.KEEP_FIRST, "Example");
		catalog.register("broad fallback", AliasCatalog.CollisionPolicy.KEEP_FIRST, "example");
		assertEquals("specific", catalog.get("EXAMPLE"));
		assertTrue(catalog.ignoredCollisionsSnapshot().containsKey("example"));

		catalog.register("refined", AliasCatalog.CollisionPolicy.REPLACE, "example");
		assertEquals("refined", catalog.get("Example"));
	}

	@Test
	public void exactBossProfilesCanRefineBroaderAssignmentAliases()
	{
		assertEquals("hellhounds", TaskProfiles.find("Hellhounds").orElseThrow().getKey());
		assertEquals("cerberus-boss", TaskProfiles.find("Cerberus").orElseThrow().getKey());
		assertEquals("araxytes", TaskProfiles.find("Araxytes").orElseThrow().getKey());
		assertEquals("araxxor-boss", TaskProfiles.find("Araxxor").orElseThrow().getKey());
	}

	private static void assertText(String field, String value, String context)
	{
		assertNotNull("Missing " + field + ": " + context, value);
		assertFalse("Blank " + field + ": " + context, value.trim().isEmpty());
	}

	private static boolean containsTwoHandedToken(String requiredWeapon)
	{
		if (requiredWeapon == null) return false;
		String normalized = NameMatcher.normalize(requiredWeapon);
		return normalized.contains("bow")
			|| normalized.contains("halberd")
			|| normalized.contains("godsword")
			|| normalized.contains("maul")
			|| normalized.contains("bulwark");
	}

	private static String normalize(String value)
	{
		String normalized = value.trim().toLowerCase(Locale.ENGLISH)
			.replaceAll("[^a-z0-9]+", " ").trim();
		return normalized.startsWith("the ") ? normalized.substring(4) : normalized;
	}
}
