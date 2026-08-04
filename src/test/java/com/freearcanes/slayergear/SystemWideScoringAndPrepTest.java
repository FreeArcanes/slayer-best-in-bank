package com.freearcanes.slayergear;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemWideScoringAndPrepTest
{
	private final SmartSupplyAdvisor advisor =
		new SmartSupplyAdvisor(null, new SlayerGearAdvisorConfig() {});

	@Test
	public void baselineDefenceIsACompleteButSmallTieBreaker()
	{
		GearStrategy ordinary = GearStrategy.builder()
			.name("Ordinary melee")
			.combatStyle(CombatStyle.MELEE)
			.build();
		ItemEquipmentStats offensive = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.str(4)
			.build();
		ItemEquipmentStats defensive = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.dstab(70)
			.dslash(70)
			.dcrush(70)
			.drange(70)
			.dmagic(70)
			.build();
		ItemEquipmentStats rangedDefence = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.drange(40)
			.build();

		assertTrue(GearScorer.scoreStats(ordinary, "Strength body",
			EquipmentInventorySlot.BODY, offensive)
			> GearScorer.scoreStats(ordinary, "Tank body",
				EquipmentInventorySlot.BODY, defensive));
		assertTrue(GearScorer.scoreStats(ordinary, "Ranged defence body",
			EquipmentInventorySlot.BODY, rangedDefence)
			> GearScorer.scoreStats(ordinary, "No-stat body",
				EquipmentInventorySlot.BODY, ItemEquipmentStats.builder()
					.slot(EquipmentInventorySlot.BODY.getSlotIdx()).build()));
	}

	@Test
	public void explicitIncomingMagicContextChangesOnlyThatMethodsTieBreaker()
	{
		GearStrategy ordinary = GearStrategy.builder()
			.name("Ordinary melee")
			.combatStyle(CombatStyle.MELEE)
			.build();
		GearStrategy magicHeavy = GearStrategy.builder()
			.name("Magic-heavy melee")
			.combatStyle(CombatStyle.MELEE)
			.magicDefenceWeight(0.28)
			.build();
		ItemEquipmentStats physicalTank = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.dstab(100)
			.dslash(100)
			.dcrush(100)
			.build();
		ItemEquipmentStats magicTank = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.dmagic(80)
			.build();

		assertTrue(GearScorer.scoreStats(ordinary, "Physical tank",
			EquipmentInventorySlot.BODY, physicalTank)
			> GearScorer.scoreStats(ordinary, "Magic tank",
				EquipmentInventorySlot.BODY, magicTank));
		assertTrue(GearScorer.scoreStats(magicHeavy, "Magic tank",
			EquipmentInventorySlot.BODY, magicTank)
			> GearScorer.scoreStats(magicHeavy, "Physical tank",
				EquipmentInventorySlot.BODY, physicalTank));
	}

	@Test
	public void knownMagicAttackingMeleeMethodsOptIntoExtraMagicDefence()
	{
		for (String task : new String[]{
			"Aberrant spectres", "Aquanites", "Bloodveld", "Jellies", "Suqahs", "Waterfiends"})
		{
			SlayerTaskProfile profile = TaskProfiles.find(task).orElseThrow();
			assertTrue(task + " lacks a context-aware Magic-defence melee method",
				profile.getStrategies().stream().anyMatch(strategy ->
					strategy.getCombatStyle() == CombatStyle.MELEE
						&& strategy.getMagicDefenceWeight() > 0));
		}

		SlayerTaskProfile kalphites = TaskProfiles.find("Kalphites").orElseThrow();
		assertFalse(kalphites.getStrategies().stream().anyMatch(strategy ->
			strategy.getMagicDefenceWeight() > 0));
	}

	@Test
	public void everyCuratedTaskStrategyHasVisibleCorePreparation()
	{
		Set<SlayerTaskProfile> profiles = new HashSet<>(TaskProfiles.catalogSnapshot().values());
		for (SlayerTaskProfile profile : profiles)
		{
			for (GearStrategy strategy : profile.getStrategies())
			{
				Set<String> categories = categories(profile, strategy, null);
				assertTrue(profile.getKey() + "/" + strategy.getName() + " lacks Prayer prep",
					categories.contains("Prayer"));
				assertTrue(profile.getKey() + "/" + strategy.getName() + " lacks Food prep",
					categories.contains("Food"));
				assertTrue(profile.getKey() + "/" + strategy.getName() + " lacks Run energy prep",
					categories.contains("Run energy"));
				assertTrue(profile.getKey() + "/" + strategy.getName() + " lacks Travel prep",
					categories.contains("Travel"));
				assertTrue(profile.getKey() + "/" + strategy.getName() + " lacks style boost",
					categories.contains(boostCategory(strategy)));
			}
		}
	}

	@Test
	public void genericSlayerMasterTasksAlsoReceiveCorePreparation()
	{
		for (List<String> assignments : SlayerMasterCatalog.allAssignments().values())
		{
			for (String task : assignments)
			{
				SlayerTaskProfile profile = TaskProfiles.find(task).orElseThrow();
				GearStrategy strategy = profile.getStrategies().get(0);
				Set<String> categories = categories(profile, strategy, null);
				assertTrue(task + " lacks Food prep", categories.contains("Food"));
				assertTrue(task + " lacks Prayer prep", categories.contains("Prayer"));
				assertTrue(task + " lacks Travel prep", categories.contains("Travel"));
			}
		}
	}

	@Test
	public void locationAndMonsterHazardsLayerOntoCorePreparation()
	{
		SlayerTaskProfile crawler = TaskProfiles.find(
			"Cave crawlers", "Lumbridge Swamp Caves").orElseThrow();
		Set<String> cave = categories(crawler, crawler.getStrategies().get(0),
			"Lumbridge Swamp Caves");
		assertTrue(cave.contains("Poison protection"));
		assertTrue(cave.contains("Light source"));
		assertTrue(cave.contains("Light backup"));
		assertTrue(cave.contains("Cave access"));

		SlayerTaskProfile lizardmen = TaskProfiles.find("Lizardmen").orElseThrow();
		assertTrue(categories(lizardmen, lizardmen.getStrategies().get(0), null)
			.contains("Poison protection"));

		SlayerTaskProfile greenDragons = TaskProfiles.find("Green dragons").orElseThrow();
		assertTrue(categories(greenDragons, greenDragons.getStrategies().get(0), null)
			.contains("Antifire"));

		SlayerTaskProfile lizards = TaskProfiles.find("Lizards").orElseThrow();
		assertTrue(categories(lizards, lizards.getStrategies().get(0), null)
			.contains("Desert hydration"));

		SlayerTaskProfile brineRats = TaskProfiles.find("Brine rats").orElseThrow();
		assertTrue(categories(brineRats, brineRats.getStrategies().get(0), null)
			.contains("Cave access"));

		SlayerTaskProfile kalphites = TaskProfiles.find("Kalphites").orElseThrow();
		assertFalse(categories(kalphites, kalphites.getStrategies().get(0), null)
			.contains("Cave access"));
		assertTrue(categories(kalphites, kalphites.getStrategies().get(0), "Kalphite Lair")
			.contains("Cave access"));
	}

	@Test
	public void corePreparationRemainsNonBlockingWhenItemsAreUnowned()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Greater demons").orElseThrow();
		GearStrategy strategy = profile.getStrategies().get(0);
		List<SupplyRecommendation> recommendations = advisor.recommend(
			profile, strategy, 80, null, null);
		for (String category : new String[]{"Combat boost", "Prayer", "Food", "Run energy", "Travel"})
		{
			SupplyRecommendation supply = recommendations.stream()
				.filter(item -> category.equals(item.getCategory()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Missing visible prep category: " + category));
			assertFalse(category + " must remain optional", supply.isRequired());
		}
	}

	private Set<String> categories(
		SlayerTaskProfile profile,
		GearStrategy strategy,
		String assignedLocation)
	{
		return advisor.buildRules(profile, strategy, assignedLocation).stream()
			.map(SmartSupplyAdvisor.SupplyRule::getCategory)
			.collect(Collectors.toSet());
	}

	private static String boostCategory(GearStrategy strategy)
	{
		switch (strategy.getCombatStyle())
		{
			case MAGIC:
				return "Magic boost";
			case RANGED:
				return "Ranged boost";
			default:
				return "Combat boost";
		}
	}
}
