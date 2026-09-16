package com.freearcanes.slayergear;

import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncounterItemRulesTest
{
	private final SmartSupplyAdvisor advisor =
		new SmartSupplyAdvisor(null, new SlayerGearAdvisorConfig() {});

	@Test
	public void krakenBossRequiresOneFishingExplosivePerPlannedKill()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Cave krakens").orElseThrow();
		GearStrategy boss = profile.getStrategies().stream()
			.filter(strategy -> strategy.getName().contains("Kraken boss"))
			.findFirst().orElseThrow();
		SmartSupplyAdvisor.SupplyRule rule = rule(
			advisor.buildRules(profile, boss, null, "Cave krakens"),
			"Fishing explosives");
		assertTrue(rule.isRequired());
		assertTrue(rule.getPreferredNames().contains("fishing explosive"));

		SupplyRecommendation missing = advisor.recommend(
			profile, boss, null, 87, null, null, false,
			GearPriority.BALANCED,
			new PotionEstimationContext("Cave krakens", 99, 10, 30)).stream()
			.filter(item -> "Fishing explosives".equals(item.getCategory()))
			.findFirst().orElseThrow();
		assertEquals(87, missing.getRecommendedQuantity());
		assertEquals("uses", missing.getQuantityUnit());
		assertTrue(missing.isStackQuantity());
		assertEquals(1, InventoryCapacityPlanner.additionalSlots(missing));
	}

	@Test
	public void ordinaryCaveKrakensDoNotDemandBossExplosives()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Cave krakens").orElseThrow();
		GearStrategy ordinary = profile.getStrategies().stream()
			.filter(strategy -> "Cave krakens".equals(strategy.getName()))
			.findFirst().orElseThrow();
		assertFalse(advisor.buildRules(profile, ordinary, null, "Cave krakens").stream()
			.anyMatch(rule -> "Fishing explosives".equals(rule.getCategory())));
	}

	@Test
	public void kreeRequiresGrappleAndCrossbowWhileOrdinaryAviansiesOnlySuggestThem()
	{
		SlayerTaskProfile kree = TaskProfiles.find("Kree'arra").orElseThrow();
		List<SmartSupplyAdvisor.SupplyRule> bossRules = advisor.buildRules(
			kree, kree.getStrategies().get(0), "Boss lair", "Kree'arra");
		assertTrue(ruleWithItem(bossRules, "mith grapple").isRequired());
		assertTrue(ruleWithItem(bossRules, "crossbow").isRequired());

		SlayerTaskProfile aviansies = TaskProfiles.find("Aviansies").orElseThrow();
		List<SmartSupplyAdvisor.SupplyRule> taskRules = advisor.buildRules(
			aviansies, aviansies.getStrategies().get(0), "God Wars Dungeon", "Aviansies");
		assertFalse(ruleWithItem(taskRules, "mith grapple").isRequired());
		assertTrue(ruleWithItem(taskRules, "mith grapple").isShownWhenMissing());
	}

	@Test
	public void otherVerifiedEncounterToolsAreCovered()
	{
		assertRequired("General Graardor", "=hammer");
		assertRequired("Skotizo", "dark totem");
		assertRequired("Bryophyta", "magic secateurs");

		SlayerTaskProfile brine = TaskProfiles.find("Brine rats").orElseThrow();
		assertTrue(ruleWithItem(advisor.buildRules(brine, brine.getStrategies().get(0)),
			"spade").isRequired());

		SlayerTaskProfile kings = TaskProfiles.find("Dagannoth Kings").orElseThrow();
		List<SmartSupplyAdvisor.SupplyRule> kingRules = advisor.buildRules(
			kings, kings.getStrategies().get(0), "Boss lair", "Dagannoth Kings");
		assertFalse(ruleWithItem(kingRules, "pet rock").isRequired());
		assertFalse(ruleWithItem(kingRules, "rune thrownaxe").isRequired());
	}

	@Test
	public void everyStandardSlayerProtectionItemHasASafetyRule()
	{
		Map<String, String> protectionByTask = Map.ofEntries(
			Map.entry("Aberrant spectres", "nose peg"),
			Map.entry("Banshees", "earmuffs"),
			Map.entry("Sourhogs", "reinforced goggles"),
			Map.entry("Dust devils", "facemask"),
			Map.entry("Smoke devils", "facemask"),
			Map.entry("Wall beasts", "spiny helmet"),
			Map.entry("Fever spiders", "slayer gloves"),
			Map.entry("Basilisks", "mirror shield"),
			Map.entry("Cockatrice", "mirror shield"),
			Map.entry("Harpie bug swarms", "lit bug lantern"),
			Map.entry("Killerwatts", "insulated boots"),
			Map.entry("Drakes", "boots of stone"),
			Map.entry("Wyrms", "boots of stone"),
			Map.entry("Hydras", "boots of stone"),
			Map.entry("Skeletal wyverns", "elemental shield"),
			Map.entry("Fossil Island wyverns", "elemental shield"),
			Map.entry("Cave horrors", "witchwood icon"));

		for (Map.Entry<String, String> expected : protectionByTask.entrySet())
		{
			SlayerTaskProfile profile = TaskProfiles.find(expected.getKey()).orElseThrow();
			GearStrategy strategy = profile.getStrategies().get(0);
			String safetyKey = profile.getKey() + " " + NameMatcher.normalize(expected.getKey());
			assertTrue(expected.getKey() + " should cover " + expected.getValue(),
				TaskSafetyRules.gearRequirements(safetyKey, strategy, false).stream()
					.flatMap(requirement -> requirement.getOptions().stream())
					.anyMatch(option -> option.getTokenExpression().contains(expected.getValue())));
		}
	}

	@Test
	public void everyStandardSlayerToolAndFinisherHasASupplyRule()
	{
		Map<String, String> toolByTask = Map.ofEntries(
			Map.entry("Gargoyles", "rock hammer"),
			Map.entry("Mutated zygomites", "fungicide spray"),
			Map.entry("Lizards", "ice cooler"),
			Map.entry("Rockslugs", "bag of salt"),
			Map.entry("Harpie bug swarms", "lit bug lantern"),
			Map.entry("Mogres", "fishing explosive"),
			Map.entry("Molanisks", "slayer bell"),
			Map.entry("Warped creatures", "crystal chime"),
			Map.entry("Brine rats", "spade"));

		for (Map.Entry<String, String> expected : toolByTask.entrySet())
		{
			SlayerTaskProfile profile = TaskProfiles.find(expected.getKey()).orElseThrow();
			assertTrue(expected.getKey() + " should require " + expected.getValue(),
				ruleWithItem(advisor.buildRules(profile, profile.getStrategies().get(0)),
					expected.getValue()).isRequired());
		}
	}

	@Test
	public void bossAccessStatusAndUtilityItemsAreCovered()
	{
		assertRequired("Obor", "giant key");
		assertRequired("Bryophyta", "mossy key");
		assertRequired("Barrows Brothers", "spade");
		assertSuggested("Barrows Brothers", "strange old lockpick");
		assertSuggested("Duke Sucellus", "dragon pickaxe");
		assertSuggested("Brutus", "cowbell amulet");
		assertSuggested("Branda the Fire Queen", "giantsoul amulet");
		assertSuggested("Sarachnis", "wilderness sword");

		assertRequiredCategory("Vorkath", "Venom protection");
		assertRequiredCategory("Zulrah", "Venom protection");
		assertSuggested("Kalphite Queen", "antidote++");
		assertSuggested("King Black Dragon", "antidote++");
	}

	@Test
	public void genericBossIdentityStillAppliesTaskSafetyRules()
	{
		GearStrategy strategy = TaskProfiles.find("Alchemical Hydra").orElseThrow()
			.getStrategies().get(0);
		assertTrue(TaskSafetyRules.gearRequirements(
			"ranged-boss alchemical hydra", strategy, false).stream()
			.anyMatch(requirement -> requirement.getLabel().contains("Karuulm")));

		SlayerTaskProfile vorkath = TaskProfiles.find("Vorkath").orElseThrow();
		assertTrue(advisor.buildRules(vorkath, vorkath.getStrategies().get(0),
			"Boss lair", "Vorkath").stream()
			.anyMatch(rule -> "Antifire".equals(rule.getCategory()) && rule.isRequired()));
	}

	private void assertRequired(String encounter, String item)
	{
		SlayerTaskProfile profile = TaskProfiles.find(encounter).orElseThrow();
		SmartSupplyAdvisor.SupplyRule rule = ruleWithItem(advisor.buildRules(
			profile, profile.getStrategies().get(0), "Boss lair", encounter), item);
		assertTrue(encounter + " should require " + item, rule.isRequired());
	}

	private void assertSuggested(String encounter, String item)
	{
		SlayerTaskProfile profile = TaskProfiles.find(encounter).orElseThrow();
		SmartSupplyAdvisor.SupplyRule rule = ruleWithItem(advisor.buildRules(
			profile, profile.getStrategies().get(0), "Boss lair", encounter), item);
		assertFalse(encounter + " should only suggest " + item, rule.isRequired());
		assertTrue(encounter + " should show missing " + item, rule.isShownWhenMissing());
	}

	private void assertRequiredCategory(String encounter, String category)
	{
		SlayerTaskProfile profile = TaskProfiles.find(encounter).orElseThrow();
		assertTrue(encounter + " should require " + category,
			rule(advisor.buildRules(profile, profile.getStrategies().get(0),
				"Boss lair", encounter), category).isRequired());
	}

	private static SmartSupplyAdvisor.SupplyRule rule(
		List<SmartSupplyAdvisor.SupplyRule> rules, String category)
	{
		return rules.stream().filter(rule -> category.equals(rule.getCategory()))
			.findFirst().orElseThrow();
	}

	private static SmartSupplyAdvisor.SupplyRule ruleWithItem(
		List<SmartSupplyAdvisor.SupplyRule> rules, String item)
	{
		return rules.stream().filter(rule -> rule.getPreferredNames().contains(item))
			.findFirst().orElseThrow();
	}
}
