package com.freearcanes.slayergear;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.runelite.client.game.ItemManager;

public class BossSlayerFeatureTest
{
	@Test
	public void selectorCatalogResolvesCuratedBossProfiles()
	{
		assertTrue(BossSlayerCatalog.names().contains("Vardorvis"));
		assertEquals("vardorvis-boss", TaskProfiles.find("Vardorvis").orElseThrow().getKey());
		assertEquals("abyssal-sire-boss", TaskProfiles.find("Abyssal Sire").orElseThrow().getKey());
	}

	@Test
	public void standardTasksExposeEveryWikiQualifiedBossAlternative()
	{
		assertTrue(BossSlayerCatalog.forTask("Araxytes").contains("Araxxor"));
		assertTrue(BossSlayerCatalog.forTask("Spiders").contains("Araxxor"));
		assertTrue(BossSlayerCatalog.forTask("Spiders").contains("Sarachnis"));
		assertTrue(BossSlayerCatalog.forTask("Spiders").contains("Venenatis"));
		assertTrue(BossSlayerCatalog.forTask("Hellhounds").contains("Cerberus"));
		assertTrue(BossSlayerCatalog.forTask("Greater demons").contains("Tormented Demons"));
		assertTrue(BossSlayerCatalog.forTask("TzHaar").contains("TzKal-Zuk"));
		assertEquals("hellhounds", TaskProfiles.find("Hellhounds").orElseThrow().getKey());
	}

	@Test
	public void wikiStandardTaskMappingIsComplete()
	{
		Map<String, List<String>> expected = Map.ofEntries(
			Map.entry("Abyssal demons", List.of("Abyssal Sire")),
			Map.entry("Hydras", List.of("Alchemical Hydra")),
			Map.entry("Lesser nagua", List.of("Amoxliatl")),
			Map.entry("Araxytes", List.of("Araxxor")),
			Map.entry("Spiders", List.of("Araxxor", "Sarachnis", "Venenatis")),
			Map.entry("Fire giants", List.of("Branda the Fire Queen")),
			Map.entry("Cows", List.of("Brutus")),
			Map.entry("Moss giants", List.of("Bryophyta")),
			Map.entry("Bears", List.of("Callisto")),
			Map.entry("Hellhounds", List.of("Cerberus")),
			Map.entry("Dagannoth", List.of("Dagannoth Kings")),
			Map.entry("Black demons", List.of("Demonic Gorillas", "Skotizo")),
			Map.entry("Monkeys", List.of("Demonic Gorillas")),
			Map.entry("Ice giants", List.of("Eldric the Ice King")),
			Map.entry("Gargoyles", List.of("Grotesque Guardians")),
			Map.entry("Greater demons", List.of("K'ril Tsutsaroth", "Skotizo", "Tormented Demons")),
			Map.entry("Kalphites", List.of("Kalphite Queen")),
			Map.entry("Black dragons", List.of("King Black Dragon")),
			Map.entry("Cave krakens", List.of("Kraken")),
			Map.entry("Aviansies", List.of("Kree'arra")),
			Map.entry("Hill giants", List.of("Obor")),
			Map.entry("Scorpions", List.of("Scorpia")),
			Map.entry("Rats", List.of("Scurrius")),
			Map.entry("Gryphons", List.of("Shellbane Gryphon")),
			Map.entry("Smoke devils", List.of("Thermonuclear Smoke Devil")),
			Map.entry("TzHaar", List.of("TzTok-Jad", "TzKal-Zuk")),
			Map.entry("Skeletons", List.of("Vet'ion")),
			Map.entry("Blue dragons", List.of("Vorkath")),
			Map.entry("Zombies", List.of("Vorkath")));
		for (Map.Entry<String, List<String>> entry : expected.entrySet())
		{
			assertEquals(entry.getKey(), entry.getValue(), BossSlayerCatalog.forTask(entry.getKey()));
		}
	}

	@Test
	public void araxxorProfileCarriesEncounterSpecificBisContext()
	{
		SlayerTaskProfile araxxor = TaskProfiles.find("Araxxor").orElseThrow();
		GearStrategy primary = araxxor.getStrategies().get(0);
		GearStrategy spawnSwitch = araxxor.getStrategies().get(1);

		assertTrue(primary.getPreferredItems().contains("amulet of rancour"));
		assertTrue(NameMatcher.matchesAnyToken("Noxious halberd", spawnSwitch.getRequiredWeapon()));
	}

	@Test
	public void bossSwitchesParticipateInBankLookup()
	{
		GearRecommendation claws = GearRecommendation.builder()
			.itemId(100).canonicalItemId(100).itemName("Dragon claws")
			.slot(EquipmentInventorySlot.WEAPON).rank(1)
			.reason("Damage special switch").banked(true).build();
		GearRecommendations recommendations = GearRecommendations.ready(
			"Cerberus", 1, null, null, Collections.emptyList(), Collections.emptyMap(),
			Collections.emptyList(), Collections.singletonList(claws), Collections.emptyList(),
			ReadinessReport.empty(), 1);

		assertTrue(recommendations.isBankViewItem(100));
		assertSame(claws, recommendations.find(100));
	}

	@Test
	public void thrallPreparationOnlyAppearsForBossPvmWhenEnabled()
	{
		SlayerGearAdvisorConfig config = mock(SlayerGearAdvisorConfig.class);
		when(config.useBossThralls()).thenReturn(true);
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(mock(ItemManager.class), config);
		SlayerTaskProfile araxxor = TaskProfiles.find("Araxxor").orElseThrow();
		List<SmartSupplyAdvisor.SupplyRule> bossRules = advisor.buildRules(
			araxxor, araxxor.getStrategies().get(0));
		assertTrue(bossRules.stream().anyMatch(rule -> "Thrall book".equals(rule.getCategory())));
		assertTrue(bossRules.stream().anyMatch(rule -> "Thrall runes".equals(rule.getCategory())));

		SlayerTaskProfile ordinary = TaskProfiles.find("Hellhounds").orElseThrow();
		List<SmartSupplyAdvisor.SupplyRule> ordinaryRules = advisor.buildRules(
			ordinary, ordinary.getStrategies().get(0));
		assertFalse(ordinaryRules.stream().anyMatch(rule -> "Thrall book".equals(rule.getCategory())));
	}
}
