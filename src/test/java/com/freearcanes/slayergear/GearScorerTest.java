package com.freearcanes.slayergear;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GearScorerTest
{
	@Test
	public void slayerHelmetGetsOnTaskMeleePriority()
	{
		GearStrategy strategy = TaskProfiles.find("Bloodveld")
			.orElseThrow()
			.getStrategies()
			.get(1);
		ItemEquipmentStats stats = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.HEAD.getSlotIdx())
			.str(3)
			.dmagic(10)
			.build();

		double slayerHelmet = GearScorer.scoreStats(
			strategy, "Slayer helmet", EquipmentInventorySlot.HEAD, stats);
		double ordinaryHelmet = GearScorer.scoreStats(
			strategy, "Ordinary helmet", EquipmentInventorySlot.HEAD, stats);

		assertTrue(slayerHelmet > ordinaryHelmet + 1_000);
	}

	@Test
	public void demonbaneWeaponsReceiveCuratedPriority()
	{
		GearStrategy strategy = TaskProfiles.find("Abyssal demons")
			.orElseThrow()
			.getStrategies()
			.get(2);
		ItemEquipmentStats stats = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.aslash(38)
			.str(35)
			.aspeed(4)
			.build();

		double emberlight = GearScorer.scoreStats(
			strategy, "Emberlight", EquipmentInventorySlot.WEAPON, stats);
		double arclight = GearScorer.scoreStats(
			strategy, "Arclight", EquipmentInventorySlot.WEAPON, stats);
		double ordinarySword = GearScorer.scoreStats(
			strategy, "Ordinary sword", EquipmentInventorySlot.WEAPON, stats);

		// Emberlight and Arclight share the same target multiplier; their real
		// base stats decide between them. With identical synthetic stats both
		// should receive the same demonbane uplift over an ordinary sword.
		assertEquals(emberlight, arclight, 0.0001);
		assertTrue(arclight > ordinarySword);
	}

	@Test
	public void bloodveldFallbackValuesMagicDefence()
	{
		GearStrategy strategy = TaskProfiles.find("Bloodveld")
			.orElseThrow()
			.getStrategies()
			.get(1);
		ItemEquipmentStats lowMagicDefence = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.dmagic(0)
			.build();
		ItemEquipmentStats highMagicDefence = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.dmagic(40)
			.build();

		double low = GearScorer.scoreStats(
			strategy, "Low defence body", EquipmentInventorySlot.BODY, lowMagicDefence);
		double high = GearScorer.scoreStats(
			strategy, "Dragonhide body", EquipmentInventorySlot.BODY, highMagicDefence);

		assertTrue(high > low);
	}

	@Test
	public void everyDuradelAssignmentCategoryHasAProfile()
	{
		List<String> tasks = Arrays.asList(
			"Aberrant spectres", "Abyssal demons", "Ankou", "Aquanites", "Araxytes",
			"Aviansies", "Basilisks", "Black demons", "Black dragons", "Bloodveld",
			"Blue dragons", "Boss", "Cave horrors", "Cave kraken", "Dagannoth",
			"Dark beasts", "Drakes", "Dust devils", "Elves", "Fire giants",
			"Fossil island wyverns", "Frost dragons", "Gargoyles", "Greater demons",
			"Gryphons", "Hellhounds", "Kalphites", "Kurask", "Lizardmen",
			"Metal dragons", "Mutated zygomites", "Nechryael", "Red dragons",
			"Skeletal wyverns", "Smoke devils", "Spiritual creatures", "Suqahs",
			"Trolls", "TzHaar", "Vampyres", "Warped creatures", "Waterfiends", "Wyrms"
		);

		for (String task : tasks)
		{
			assertTrue("Missing profile for " + task, TaskProfiles.find(task).isPresent());
		}
	}

	@Test
	public void barrageInventoryOnlyAppliesToBarrageMethods()
	{
		GearStrategy barrage = GearStrategy.builder()
			.name("Blood barrage")
			.combatStyle(CombatStyle.MAGIC)
			.ancientAoe(true)
			.build();
		GearStrategy ordinaryMagic = GearStrategy.builder()
			.name("Water Magic")
			.combatStyle(CombatStyle.MAGIC)
			.build();

		assertTrue(barrage.isAncientAoe());
		assertFalse(ordinaryMagic.isAncientAoe());
	}

	@Test
	public void fullestPotionDoseHasHighestSupplyPriority()
	{
		assertTrue(
			SmartSupplyAdvisor.doseScore("Prayer potion(4)")
				> SmartSupplyAdvisor.doseScore("Prayer potion(1)"));
	}

	@Test
	public void venatorStrategiesPreferGoadingAndPrayerSustain()
	{
		GearStrategy venator = GearStrategy.builder()
			.name("Venator bow")
			.combatStyle(CombatStyle.RANGED)
			.build();

		assertTrue(SmartSupplyAdvisor.isVenator(venator));
	}
	@Test
	public void withdrawnTierOneGearStaysInActiveLoadoutPool()
	{
		Item tierTwoStillBanked = new Item(2002, 1);
		Item tierOneWithdrawn = new Item(1001, 1);
		Item equippedItem = new Item(3003, 1);

		Item[] combined = SlayerGearAdvisorPlugin.combineGearPool(
			new Item[] {tierTwoStillBanked},
			new Item[] {tierOneWithdrawn},
			new Item[] {equippedItem});

		assertEquals(3, combined.length);
		assertEquals(2002, combined[0].getId());
		assertEquals(1001, combined[1].getId());
		assertEquals(3003, combined[2].getId());
	}

	@Test
	public void lowRiskModeCapsTheCombinedLoadoutInsteadOfEachItem()
	{
		Map<EquipmentInventorySlot, List<GearScorer.BankEquipment>> candidates =
			new EnumMap<>(EquipmentInventorySlot.class);
		candidates.put(EquipmentInventorySlot.HEAD, Arrays.asList(
			riskItem(1, "Strong helmet", EquipmentInventorySlot.HEAD, 400, 100, false),
			riskItem(2, "Budget helmet", EquipmentInventorySlot.HEAD, 100, 60, false)));
		candidates.put(EquipmentInventorySlot.BODY, Arrays.asList(
			riskItem(3, "Strong body", EquipmentInventorySlot.BODY, 400, 100, false),
			riskItem(4, "Budget body", EquipmentInventorySlot.BODY, 100, 60, false)));

		Map<EquipmentInventorySlot, GearScorer.BankEquipment> selected =
			GearScorer.selectRiskBudgetItems(
				1,
				candidates,
				GearStrategy.builder().combatStyle(CombatStyle.MELEE).build(),
				500);

		assertEquals(2, selected.size());
		assertEquals(500, GearScorer.totalGuidePrice(selected));
	}

	@Test
	public void pinnedGearRemainsAHardOverrideWhenItExceedsTheLoadoutCap()
	{
		GearScorer.BankEquipment pinned = riskItem(
			10, "Pinned helmet", EquipmentInventorySlot.HEAD, 700, 100, true);
		Map<EquipmentInventorySlot, List<GearScorer.BankEquipment>> candidates =
			new EnumMap<>(EquipmentInventorySlot.class);
		candidates.put(EquipmentInventorySlot.HEAD, Arrays.asList(
			pinned,
			riskItem(11, "Cheap helmet", EquipmentInventorySlot.HEAD, 100, 60, false)));
		candidates.put(EquipmentInventorySlot.BODY, Arrays.asList(
			riskItem(12, "Cheap body", EquipmentInventorySlot.BODY, 100, 50, false)));

		Map<EquipmentInventorySlot, GearScorer.BankEquipment> selected =
			GearScorer.selectRiskBudgetItems(
				1,
				candidates,
				GearStrategy.builder().combatStyle(CombatStyle.MELEE).build(),
				500);

		assertEquals(pinned, selected.get(EquipmentInventorySlot.HEAD));
		assertEquals(800, GearScorer.totalGuidePrice(selected));
	}

	@Test
	public void minimumSafeSlotIsKeptWhenNoLoadoutCanFitTheCap()
	{
		GearScorer.BankEquipment requiredShield = riskItem(
			20, "Required shield", EquipmentInventorySlot.SHIELD, 600, 50, false);
		Map<EquipmentInventorySlot, List<GearScorer.BankEquipment>> candidates =
			new EnumMap<>(EquipmentInventorySlot.class);
		candidates.put(EquipmentInventorySlot.SHIELD, Arrays.asList(requiredShield));

		Map<EquipmentInventorySlot, GearScorer.BankEquipment> selected =
			GearScorer.selectRiskBudgetItems(
				1,
				candidates,
				GearStrategy.builder().combatStyle(CombatStyle.MELEE).build(),
				500);

		assertEquals(requiredShield, selected.get(EquipmentInventorySlot.SHIELD));
		assertEquals(600, GearScorer.totalGuidePrice(selected));
	}

	@Test
	public void alternativeTiersAreCompleteLoadoutsWithSmallCoherentChanges()
	{
		Map<EquipmentInventorySlot, List<GearScorer.BankEquipment>> candidates =
			new EnumMap<>(EquipmentInventorySlot.class);
		candidates.put(EquipmentInventorySlot.WEAPON, Arrays.asList(
			riskItem(30, "Main weapon", EquipmentInventorySlot.WEAPON, 0, 200, false)));
		candidates.put(EquipmentInventorySlot.HEAD, Arrays.asList(
			riskItem(31, "Best helmet", EquipmentInventorySlot.HEAD, 0, 100, false),
			riskItem(32, "Next helmet", EquipmentInventorySlot.HEAD, 0, 90, false)));
		candidates.put(EquipmentInventorySlot.BODY, Arrays.asList(
			riskItem(33, "Best body", EquipmentInventorySlot.BODY, 0, 100, false),
			riskItem(34, "Next body", EquipmentInventorySlot.BODY, 0, 1, false)));

		List<Map<EquipmentInventorySlot, GearRecommendation>> tiers =
			new GearScorer(null, null).buildCoherentLoadouts(
				3,
				candidates,
				GearStrategy.builder().combatStyle(CombatStyle.MELEE).build(),
				Collections.emptyList(),
				Collections.emptySet(),
				false,
				0);

		assertEquals(3, tiers.size());
		assertEquals("Best helmet", tiers.get(0).get(EquipmentInventorySlot.HEAD).getItemName());
		assertEquals("Best body", tiers.get(0).get(EquipmentInventorySlot.BODY).getItemName());
		assertEquals("Next helmet", tiers.get(1).get(EquipmentInventorySlot.HEAD).getItemName());
		assertEquals("Best body", tiers.get(1).get(EquipmentInventorySlot.BODY).getItemName());
		assertEquals("Best helmet", tiers.get(2).get(EquipmentInventorySlot.HEAD).getItemName());
		assertEquals("Next body", tiers.get(2).get(EquipmentInventorySlot.BODY).getItemName());
	}

	@Test
	public void weaponFallbackRebuildsAmmoAndOffhandAsOneCoherentSwap()
	{
		Map<EquipmentInventorySlot, List<GearScorer.BankEquipment>> candidates =
			new EnumMap<>(EquipmentInventorySlot.class);
		candidates.put(EquipmentInventorySlot.WEAPON, Arrays.asList(
			riskItem(40, "Magic shortbow", EquipmentInventorySlot.WEAPON, 0, 100, false, true),
			riskItem(41, "Rune crossbow", EquipmentInventorySlot.WEAPON, 0, 90, false, false)));
		candidates.put(EquipmentInventorySlot.AMMO, Arrays.asList(
			riskItem(42, "Amethyst arrow", EquipmentInventorySlot.AMMO, 0, 20, false),
			riskItem(43, "Dragon bolts", EquipmentInventorySlot.AMMO, 0, 19, false)));
		candidates.put(EquipmentInventorySlot.SHIELD, Arrays.asList(
			riskItem(44, "Odium ward", EquipmentInventorySlot.SHIELD, 0, 15, false)));

		List<Map<EquipmentInventorySlot, GearRecommendation>> tiers =
			new GearScorer(null, null).buildCoherentLoadouts(
				2,
				candidates,
				GearStrategy.builder().combatStyle(CombatStyle.RANGED).build(),
				Collections.emptyList(),
				Collections.emptySet(),
				false,
				0);

		assertEquals("Magic shortbow",
			tiers.get(0).get(EquipmentInventorySlot.WEAPON).getItemName());
		assertEquals("Amethyst arrow",
			tiers.get(0).get(EquipmentInventorySlot.AMMO).getItemName());
		assertFalse(tiers.get(0).containsKey(EquipmentInventorySlot.SHIELD));

		assertEquals("Rune crossbow",
			tiers.get(1).get(EquipmentInventorySlot.WEAPON).getItemName());
		assertEquals("Dragon bolts",
			tiers.get(1).get(EquipmentInventorySlot.AMMO).getItemName());
		assertEquals("Odium ward",
			tiers.get(1).get(EquipmentInventorySlot.SHIELD).getItemName());
	}

	@Test
	public void everyRuneLiteRecognizedSlayerTaskHasSafeProfile()
	{
		List<String> tasks = Arrays.asList(
			"Aberrant spectres",
			"Abyssal demons",
			"The Abyssal Sire",
			"The Alchemical Hydra",
			"Ankou",
			"Aquanites",
			"Araxxor",
			"Araxytes",
			"Aviansies",
			"Bandits",
			"Banshees",
			"Barrows Brothers",
			"Basilisks",
			"Bats",
			"Bears",
			"Birds",
			"Black demons",
			"Black dragons",
			"Black Knights",
			"Bloodveld",
			"Blue dragons",
			"Brine rats",
			"Callisto",
			"Catablepon",
			"Cave bugs",
			"Cave crawlers",
			"Cave horrors",
			"Cave kraken",
			"Cave slimes",
			"Cerberus",
			"Chaos druids",
			"The Chaos Elemental",
			"The Chaos Fanatic",
			"Cockatrice",
			"Cows",
			"Crabs",
			"Crawling hands",
			"Crazy Archaeologists",
			"Crocodiles",
			"Custodian Stalkers",
			"Dagannoth",
			"Dagannoth Kings",
			"Dark beasts",
			"Dark warriors",
			"Deranged Archaeologist",
			"Dogs",
			"Drakes",
			"Duke Sucellus",
			"Dust devils",
			"Dwarves",
			"Earth warriors",
			"Elves",
			"Ents",
			"Fever spiders",
			"Fire giants",
			"Fleshcrawlers",
			"Fossil island wyverns",
			"Frost dragons",
			"Gargoyles",
			"General Graardor",
			"Ghosts",
			"Ghouls",
			"The Giant Mole",
			"Goblins",
			"Greater demons",
			"Green dragons",
			"The Grotesque Guardians",
			"Gryphons",
			"Harpie bug swarms",
			"Hellhounds",
			"Hill giants",
			"Hobgoblins",
			"Hydras",
			"Icefiends",
			"Ice giants",
			"Ice warriors",
			"Infernal mages",
			"TzTok-Jad",
			"Jellies",
			"Jungle horrors",
			"Kalphites",
			"The Kalphite Queen",
			"Killerwatts",
			"The King Black Dragon",
			"The Cave Kraken Boss",
			"Kree'arra",
			"K'ril Tsutsaroth",
			"Kurask",
			"Lava Dragons",
			"Lesser demons",
			"Lesser Nagua",
			"Lizardmen",
			"Lizards",
			"The Maggot King",
			"Magic axes",
			"Mammoths",
			"Metal dragons",
			"Minotaurs",
			"Mogres",
			"Molanisks",
			"Monkeys",
			"Moss giants",
			"Mutated zygomites",
			"Nechryael",
			"Ogres",
			"Otherworldly beings",
			"The Phantom Muspah",
			"Pirates",
			"Pyrefiends",
			"Rats",
			"Red dragons",
			"Revenants",
			"Rockslugs",
			"Rogues",
			"Sarachnis",
			"Scabarites",
			"Scorpia",
			"Scorpions",
			"Sea snakes",
			"Shades",
			"Shadow warriors",
			"The Shellbane Gryphon",
			"Skeletal wyverns",
			"Skeletons",
			"Smoke devils",
			"Sourhogs",
			"Spiders",
			"Spiritual creatures",
			"Suqahs",
			"Terror dogs",
			"The Leviathan",
			"The Whisperer",
			"The Thermonuclear Smoke Devil",
			"Trolls",
			"Turoth",
			"Tzhaar",
			"Vampyres",
			"Vardorvis",
			"Venenatis",
			"Vet'ion",
			"Vorkath",
			"Wall beasts",
			"Warped Creatures",
			"Waterfiends",
			"Werewolves",
			"Wolves",
			"Wyrms",
			"Commander Zilyana",
			"Zombies",
			"TzKal-Zuk",
			"Zulrah"
		);
		for (String task : tasks) assertTrue("Missing safe profile for " + task, TaskProfiles.find(task).isPresent());
	}

	@Test
	public void activeSlayerMasterCatalogIsPopulated()
	{
		assertTrue(SlayerMasterCatalog.allAssignments().containsKey("Turael / Aya"));
		assertTrue(SlayerMasterCatalog.allAssignments().containsKey("Mazchna / Achtryn"));
		assertTrue(SlayerMasterCatalog.allAssignments().containsKey("Vannaka"));
		assertTrue(SlayerMasterCatalog.allAssignments().containsKey("Chaeldar"));
		assertTrue(SlayerMasterCatalog.allAssignments().containsKey("Konar quo Maten"));
		assertTrue(SlayerMasterCatalog.allAssignments().containsKey("Nieve / Steve"));
		assertTrue(SlayerMasterCatalog.allAssignments().containsKey("Duradel / Kuradal"));
		assertTrue(SlayerMasterCatalog.allAssignments().containsKey("Krystilia"));
	}

	@Test
	public void ancientAoeNamesTrackRealMagicThresholds()
	{
		assertTrue(GearScorer.highestAncientAoe(94).equals("Ice Barrage"));
		assertTrue(GearScorer.highestAncientAoe(86).equals("Smoke Barrage"));
		assertTrue(GearScorer.highestAncientAoe(70).equals("Ice Burst"));
		assertTrue(GearScorer.highestAncientAoe(61).contains("unavailable"));
	}

	@Test
	public void currentMasterCatalogTracksLiveAssignments()
	{
		List<String> vannaka = SlayerMasterCatalog.allAssignments().get("Vannaka");
		assertTrue(vannaka.contains("Crabs"));
		assertTrue(vannaka.contains("Gryphons"));
		assertFalse(vannaka.contains("Green dragons"));
		assertFalse(vannaka.contains("Earth warriors"));

		List<String> chaeldar = SlayerMasterCatalog.allAssignments().get("Chaeldar");
		assertTrue(chaeldar.contains("Crabs"));
		assertTrue(chaeldar.contains("Gryphons"));
		assertTrue(chaeldar.contains("Custodian stalkers"));
		assertTrue(chaeldar.contains("Lesser Nagua"));
		assertFalse(chaeldar.contains("Harpie bug swarms"));
		assertFalse(chaeldar.contains("Infernal mages"));
		assertFalse(chaeldar.contains("Aquanites"));

		List<String> nieve = SlayerMasterCatalog.allAssignments().get("Nieve / Steve");
		assertTrue(nieve.contains("Aquanites"));
		assertTrue(nieve.contains("Frost dragons"));
		assertTrue(nieve.contains("Gryphons"));
		assertTrue(nieve.contains("Custodian stalkers"));
		assertTrue(nieve.contains("Minions of Scabaras"));

		List<String> konar = SlayerMasterCatalog.allAssignments().get("Konar quo Maten");
		assertTrue(konar.contains("Jellies"));
		assertTrue(konar.contains("Lesser Nagua"));
		assertTrue(konar.contains("Metal dragons"));

		List<String> krystilia = SlayerMasterCatalog.allAssignments().get("Krystilia");
		assertTrue(krystilia.contains("Black knights"));
		assertTrue(krystilia.contains("Zombies"));
	}

	@Test
	public void everyMasterAssignmentHasASafeProfile()
	{
		SlayerMasterCatalog.allAssignments().forEach((master, tasks) ->
		{
			for (String task : tasks)
			{
				assertTrue(master + " missing safe profile for " + task, TaskProfiles.find(task).isPresent());
			}
		});
	}

	@Test
	public void masterAliasesMapToRuneLiteTaskNames()
	{
		assertTrue(SlayerMasterCatalog.mastersFor("Scabarites").contains("Nieve / Steve"));
		assertTrue(SlayerMasterCatalog.mastersFor("Waterfiends").contains("Konar quo Maten"));
		assertTrue(SlayerMasterCatalog.mastersFor("Aviansies").contains("Chaeldar"));
	}

	@Test
	public void mandatoryShieldRequirementsAreRestrictive()
	{
		GearStrategy strategy = TaskProfiles.find("Basilisks").orElseThrow().getStrategies().get(0);
		List<GearRequirement> requirements = TaskSafetyRules.gearRequirements("basilisks", strategy, true);
		assertTrue(requirements.stream().anyMatch(requirement -> requirement.restricts(EquipmentInventorySlot.SHIELD)));
	}

	@Test
	public void masterCatalogMatchesCurrentLivePoolSizes()
	{
		assertEquals(24, SlayerMasterCatalog.allAssignments().get("Turael / Aya").size());
		assertEquals(25, SlayerMasterCatalog.allAssignments().get("Spria").size());
		assertEquals(30, SlayerMasterCatalog.allAssignments().get("Mazchna / Achtryn").size());
		assertEquals(46, SlayerMasterCatalog.allAssignments().get("Vannaka").size());
		assertEquals(40, SlayerMasterCatalog.allAssignments().get("Chaeldar").size());
		assertEquals(39, SlayerMasterCatalog.allAssignments().get("Konar quo Maten").size());
		assertEquals(46, SlayerMasterCatalog.allAssignments().get("Nieve / Steve").size());
		assertEquals(43, SlayerMasterCatalog.allAssignments().get("Duradel / Kuradal").size());
		assertEquals(37, SlayerMasterCatalog.allAssignments().get("Krystilia").size());
	}

	@Test
	public void masterCatalogResolvesTrackerAliases()
	{
		assertTrue(SlayerMasterCatalog.mastersFor("Boss").contains("Duradel / Kuradal"));
		assertTrue(SlayerMasterCatalog.mastersFor("Steel dragons").contains("Konar quo Maten"));
		assertTrue(SlayerMasterCatalog.mastersFor("Rune dragons").contains("Nieve / Steve"));
		assertTrue(SlayerMasterCatalog.mastersFor("Scabarites").contains("Nieve / Steve"));
	}

	@Test
	public void satisfiedDragonfireProtectionSuppressesAntifireNag()
	{
		List<SupplyRecommendation> supplies = Arrays.asList(
			new SupplyRecommendation(1, 1, "Antifire potion(4)", "Antifire", "Protection", SupplyStatus.BANKED, true),
			new SupplyRecommendation(2, 2, "Prayer potion(4)", "Prayer", "Sustain", SupplyStatus.BANKED, false));
		List<SupplyRecommendation> filtered = GearScorer.withoutCategory(supplies, "Antifire");
		assertEquals(1, filtered.size());
		assertTrue(filtered.get(0).getCategory().equals("Prayer"));
	}

	@Test
	public void requiredShieldMarksTwoHandedWeaponsUnsafe()
	{
		GearStrategy strategy = GearStrategy.builder().name("Protected ranged")
			.combatStyle(CombatStyle.RANGED).requiredOffhand("mirror shield").build();
		List<GearRequirement> requirements = TaskSafetyRules.gearRequirements("basilisks", strategy, false);
		assertTrue(GearScorer.requiresMandatoryOffhand(requirements));
	}

	@Test
	public void kourendEliteRemovesKaruulmBootRequirement()
	{
		GearStrategy strategy = GearStrategy.builder().name("Melee").combatStyle(CombatStyle.MELEE).build();
		assertTrue(TaskSafetyRules.gearRequirements("drakes", strategy, false).size() > 0);
		assertEquals(0, TaskSafetyRules.gearRequirements("drakes", strategy, true).size());
	}

	@Test
	public void specializedTaskProfilesWinOverBroadBossAliases()
	{
		assertTrue(TaskProfiles.find("Cerberus").orElseThrow().getKey().equals("hellhounds"));
		assertTrue(TaskProfiles.find("The Thermonuclear Smoke Devil").orElseThrow().getKey().equals("melee-boss"));
	}

	@Test
	public void rangedSelfAmmoWeaponsDoNotDemandArrowSlot()
	{
		assertTrue(GearScorer.usesNoAmmoSlot("Toxic blowpipe"));
		assertTrue(GearScorer.usesNoAmmoSlot("Black chinchompa"));
		assertTrue(GearScorer.usesNoAmmoSlot("Rune knife"));
		assertFalse(GearScorer.usesNoAmmoSlot("Rune crossbow"));
	}

	@Test
	public void packedAndBankedConsumablesRemainWithdrawable()
	{
		SupplyStatus status = SmartSupplyAdvisor.resolveStatus(true, true);
		assertTrue(status.isPacked());
		assertTrue(status.isBanked());
		assertEquals(SupplyStatus.PACKED_BANKED, status);
	}

	@Test
	public void rawAndBurntFoodAreNeverSuggestedAsTripFood()
	{
		assertTrue(SmartSupplyAdvisor.isUnsafeFoodName("raw anglerfish"));
		assertTrue(SmartSupplyAdvisor.isUnsafeFoodName("burnt shark"));
		assertFalse(SmartSupplyAdvisor.isUnsafeFoodName("anglerfish"));
		assertFalse(SmartSupplyAdvisor.isUnsafeFoodName("cooked karambwan"));
	}

	private static GearScorer.BankEquipment riskItem(
		int itemId,
		String name,
		EquipmentInventorySlot slot,
		int guidePrice,
		double score,
		boolean pinned)
	{
		return riskItem(itemId, name, slot, guidePrice, score, pinned, false);
	}

	private static GearScorer.BankEquipment riskItem(
		int itemId,
		String name,
		EquipmentInventorySlot slot,
		int guidePrice,
		double score,
		boolean pinned,
		boolean twoHanded)
	{
		ItemEquipmentStats stats = ItemEquipmentStats.builder()
			.slot(slot.getSlotIdx())
			.isTwoHanded(twoHanded)
			.build();
		GearScorer.BankEquipment item = new GearScorer.BankEquipment(
			itemId, itemId, name, slot, stats, true, false);
		item.guidePrice = guidePrice;
		item.score = score;
		item.pinned = pinned;
		return item;
	}

}
