package com.freearcanes.slayergear;

import java.util.HashSet;
import java.util.Set;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CombatIntelligenceTest
{
	@Test
	public void crystalHalberdCannotBeScoredAsCrushWeapon()
	{
		assertFalse(WeaponCombatRules.supportsAttackType("Crystal halberd", AttackType.CRUSH));
		assertTrue(WeaponCombatRules.supportsAttackType("Crystal halberd", AttackType.STAB));
		assertTrue(WeaponCombatRules.supportsAttackType("Crystal halberd", AttackType.SLASH));
		assertTrue(WeaponCombatRules.supportsAttackType("Granite hammer", AttackType.CRUSH));
	}

	@Test
	public void auditedWeaponFamiliesCannotMasqueradeAsUnsupportedStyles()
	{
		assertTrue(WeaponCombatRules.supportsAttackType("Arkan blade", AttackType.STAB));
		assertTrue(WeaponCombatRules.supportsAttackType("Arkan blade", AttackType.SLASH));
		assertFalse(WeaponCombatRules.supportsAttackType("Arkan blade", AttackType.CRUSH));
		assertFalse(WeaponCombatRules.supportsAttackType("Noxious halberd", AttackType.CRUSH));
		assertFalse(WeaponCombatRules.supportsAttackType("Ghrazi rapier", AttackType.SLASH));
		assertTrue(WeaponCombatRules.supportsAttackType("Ghrazi rapier", AttackType.STAB));
		assertFalse(WeaponCombatRules.supportsAttackType("Abyssal whip", AttackType.STAB));
		assertFalse(WeaponCombatRules.supportsAttackType("Trident of the swamp", AttackType.SLASH));
		assertTrue(WeaponCombatRules.supportsAttackType("Trident of the swamp", AttackType.CRUSH));
	}

	@Test
	public void araxxorSeparatesMainCrushWeaponFromNoxiousSwitch()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Araxxor").orElseThrow();
		GearStrategy main = profile.getStrategies().get(0);
		GearStrategy switchMethod = profile.getStrategies().get(1);

		assertEquals(AttackType.CRUSH, main.getAttackType());
		assertTrue(main.getTargetTraits().contains(TargetTrait.ARAXXOR));
		assertTrue(main.getTargetTraits().contains(TargetTrait.SCYTHE_THREE_HIT));
		assertEquals(AttackType.SLASH, switchMethod.getAttackType());
		assertEquals("noxious halberd", switchMethod.getRequiredWeapon());
		assertFalse(WeaponCombatRules.supportsAttackType("Noxious halberd", main.getAttackType()));
	}

	@Test
	public void preferredWeaponNameOnlyBreaksCloseTies()
	{
		GearStrategy strategy = GearStrategy.builder()
			.name("Slash")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.SLASH)
			.preferredItem("Suggested sword")
			.build();
		ItemEquipmentStats suggested = weapon(0, 80, 0, 80, 4);
		ItemEquipmentStats stronger = weapon(0, 80, 0, 84, 4);

		assertTrue(GearScorer.scoreStats(
			strategy, "Actually stronger sword", EquipmentInventorySlot.WEAPON, stronger)
			> GearScorer.scoreStats(
				strategy, "Suggested sword", EquipmentInventorySlot.WEAPON, suggested));
	}

	@Test
	public void araxyteAssignmentExposesSeparateNormalAndBossMethods()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Araxytes").orElseThrow();

		assertTrue(profile.getStrategies().stream().anyMatch(strategy ->
			NameMatcher.normalize(strategy.getName()).contains("araxxor")
				&& strategy.getAttackType() == AttackType.CRUSH
				&& strategy.getTargetTraits().contains(TargetTrait.ARAXXOR)));
		assertTrue(profile.getStrategies().stream().anyMatch(strategy ->
			"noxious halberd".equals(strategy.getRequiredWeapon())
				&& strategy.getAttackType() == AttackType.SLASH));
		assertTrue(profile.getStrategies().stream().anyMatch(strategy ->
			NameMatcher.normalize(strategy.getName()).contains("melee fallback")
				&& strategy.getAttackType() == AttackType.CRUSH));
	}

	@Test
	public void auditedSlayerBossesNoLongerUseTheGenericMeleeBucket()
	{
		assertEquals(AttackType.CRUSH, TaskProfiles.find("Cerberus").orElseThrow().getStrategies().get(0).getAttackType());
		assertTrue(TaskProfiles.find("Cerberus").orElseThrow().getStrategies().get(0)
			.getTargetTraits().contains(TargetTrait.DEMON));
		assertEquals(AttackType.SLASH, TaskProfiles.find("Duke Sucellus").orElseThrow().getStrategies().get(0).getAttackType());
		assertEquals(AttackType.CRUSH, TaskProfiles.find("Sarachnis").orElseThrow().getStrategies().get(0).getAttackType());
		assertEquals(AttackType.SLASH, TaskProfiles.find("Vardorvis").orElseThrow().getStrategies().get(0).getAttackType());
		assertEquals(WeaponRule.DEMONBANE, TaskProfiles.find("Abyssal Sire").orElseThrow()
			.getStrategies().get(0).getWeaponRule());
		assertTrue(TaskProfiles.find("Kalphite Queen").orElseThrow().getStrategies().get(0)
			.getTargetTraits().contains(TargetTrait.KALPHITE));
		assertTrue(TaskProfiles.find("Vet'ion").orElseThrow().getStrategies().get(0)
			.getTargetTraits().contains(TargetTrait.WILDERNESS));
	}

	@Test
	public void dukeAppliesItsReducedDemonbaneMultiplier()
	{
		GearStrategy duke = TaskProfiles.find("Duke Sucellus").orElseThrow().getStrategies().get(0);
		GearStrategy ordinaryDemon = TaskProfiles.find("Black demons").orElseThrow().getStrategies().stream()
			.filter(strategy -> strategy.getCombatStyle() == CombatStyle.MELEE)
			.findFirst().orElseThrow();

		assertEquals(1.49, WeaponCombatRules.damageMultiplier(duke, "Emberlight"), 0.0001);
		assertEquals(1.49, WeaponCombatRules.accuracyMultiplier(duke, "Emberlight"), 0.0001);
		assertEquals(1.70, WeaponCombatRules.damageMultiplier(ordinaryDemon, "Emberlight"), 0.0001);
	}

	@Test
	public void gargoylesCarryCrushGolembaneAndEarthWeakness()
	{
		GearStrategy gargoyle = TaskProfiles.find("Gargoyles").orElseThrow().getStrategies().get(0);
		assertEquals(WeaponRule.GOLEMBANE, gargoyle.getWeaponRule());
		assertEquals(AttackType.CRUSH, gargoyle.getAttackType());
		assertTrue(gargoyle.getTargetTraits().contains(TargetTrait.GOLEM));
		assertEquals(ElementalWeakness.EARTH, gargoyle.getElementalWeakness());
		assertEquals(40, gargoyle.getElementalWeaknessPercent());
	}

	@Test
	public void graniteHammerTargetEffectBeatsRawStatHalberdProxyOnGargoyles()
	{
		GearStrategy gargoyle = TaskProfiles.find("Gargoyles").orElseThrow().getStrategies().get(0);
		ItemEquipmentStats granite = weapon(0, 0, 57, 56, 4);
		ItemEquipmentStats halberd = weapon(85, 92, 0, 118, 7);

		double graniteScore = GearScorer.scoreStats(gargoyle, "Granite hammer", EquipmentInventorySlot.WEAPON, granite);
		double halberdScore = GearScorer.scoreStats(gargoyle, "Crystal halberd", EquipmentInventorySlot.WEAPON, halberd);
		assertTrue(graniteScore > halberdScore);
	}

	@Test
	public void golembaneIsDynamicNotAHardForce()
	{
		GearStrategy gargoyle = TaskProfiles.find("Gargoyles").orElseThrow().getStrategies().get(0);
		double granite = GearScorer.scoreStats(gargoyle, "Granite hammer", EquipmentInventorySlot.WEAPON,
			weapon(0, 0, 57, 56, 4));
		double strongerValidCrush = GearScorer.scoreStats(gargoyle, "Stronger valid crush weapon", EquipmentInventorySlot.WEAPON,
			weapon(0, 0, 160, 160, 4));
		assertTrue(strongerValidCrush > granite);
	}

	@Test
	public void currentTargetMultipliersAreApplied()
	{
		GearStrategy gargoyle = TaskProfiles.find("Gargoyles").orElseThrow().getStrategies().get(0);
		assertEquals(1.30, WeaponCombatRules.accuracyMultiplier(gargoyle, "Granite hammer"), 0.0001);
		assertEquals(1.30, WeaponCombatRules.damageMultiplier(gargoyle, "Granite hammer"), 0.0001);

		GearStrategy wyrmMagic = TaskProfiles.find("Wyrms").orElseThrow().getStrategies().stream()
			.filter(s -> s.getCombatStyle() == CombatStyle.MAGIC).findFirst().orElseThrow();
		assertEquals(1.75, WeaponCombatRules.accuracyMultiplier(wyrmMagic, "Dragon hunter wand"), 0.0001);
		assertEquals(1.40, WeaponCombatRules.damageMultiplier(wyrmMagic, "Dragon hunter wand"), 0.0001);

		GearStrategy vampyre = TaskProfiles.find("Vampyres").orElseThrow().getStrategies().get(0);
		assertEquals(1.25, WeaponCombatRules.accuracyMultiplier(vampyre, "Sunspear"), 0.0001);
		assertEquals(1.50, WeaponCombatRules.damageMultiplier(vampyre, "Sunspear"), 0.0001);
	}


	@Test
	public void wikiWeaponPassivesUseCurrentTargetSpecificValues()
	{
		GearStrategy demonMelee = TaskProfiles.find("Black demons").orElseThrow().getStrategies().stream()
			.filter(s -> s.getCombatStyle() == CombatStyle.MELEE).findFirst().orElseThrow();
		assertEquals(1.70, WeaponCombatRules.accuracyMultiplier(demonMelee, "Emberlight"), 0.0001);
		assertEquals(1.70, WeaponCombatRules.damageMultiplier(demonMelee, "Emberlight"), 0.0001);
		assertEquals(1.00, WeaponCombatRules.accuracyMultiplier(demonMelee, "Darklight"), 0.0001);
		assertEquals(1.60, WeaponCombatRules.damageMultiplier(demonMelee, "Darklight"), 0.0001);
		assertEquals(1.05, WeaponCombatRules.accuracyMultiplier(demonMelee, "Burning claws"), 0.0001);
		assertEquals(1.05, WeaponCombatRules.damageMultiplier(demonMelee, "Burning claws"), 0.0001);

		GearStrategy demonRanged = GearStrategy.builder()
			.name("Demonbane Ranged")
			.combatStyle(CombatStyle.RANGED)
			.weaponRule(WeaponRule.DEMONBANE)
			.targetTrait(TargetTrait.DEMON)
			.build();
		assertEquals(1.30, WeaponCombatRules.accuracyMultiplier(demonRanged, "Scorching bow"), 0.0001);
		assertEquals(1.30, WeaponCombatRules.damageMultiplier(demonRanged, "Scorching bow"), 0.0001);
		assertEquals(1.00, WeaponCombatRules.accuracyMultiplier(demonRanged, "Holy water"), 0.0001);
		assertEquals(1.60, WeaponCombatRules.damageMultiplier(demonRanged, "Holy water"), 0.0001);

		GearStrategy golem = TaskProfiles.find("Gargoyles").orElseThrow().getStrategies().get(0);
		assertEquals(1.00, WeaponCombatRules.accuracyMultiplier(golem, "Barronite mace"), 0.0001);
		assertEquals(1.15, WeaponCombatRules.damageMultiplier(golem, "Barronite mace"), 0.0001);

		GearStrategy kurask = TaskProfiles.find("Kurask").orElseThrow().getStrategies().get(0);
		assertEquals(1.175, WeaponCombatRules.damageMultiplier(kurask, "Leaf-bladed battleaxe"), 0.0001);

		GearStrategy vampyre = TaskProfiles.find("Vampyres").orElseThrow().getStrategies().get(0);
		assertEquals(1.25, WeaponCombatRules.accuracyMultiplier(vampyre, "Hallowed flail"), 0.0001);
		assertEquals(1.25, WeaponCombatRules.damageMultiplier(vampyre, "Hallowed flail"), 0.0001);
		assertEquals(1.05, WeaponCombatRules.accuracyMultiplier(vampyre, "Blisterwood flail"), 0.0001);
		assertEquals(1.25, WeaponCombatRules.damageMultiplier(vampyre, "Blisterwood flail"), 0.0001);
		assertEquals(1.20, WeaponCombatRules.damageMultiplier(vampyre, "Ivandis flail"), 0.0001);
	}

	@Test
	public void targetLockedWeaponsAreRejectedOutsideTheirTargetFamily()
	{
		GearStrategy ordinary = GearStrategy.builder().name("Ordinary").combatStyle(CombatStyle.RANGED).build();
		GearStrategy demon = GearStrategy.builder().name("Demon").combatStyle(CombatStyle.RANGED)
			.targetTrait(TargetTrait.DEMON).build();
		GearStrategy rat = GearStrategy.builder().name("Rat").combatStyle(CombatStyle.MELEE)
			.targetTrait(TargetTrait.RAT).build();

		assertFalse(WeaponCombatRules.usableOnTarget(ordinary, "Holy water"));
		assertTrue(WeaponCombatRules.usableOnTarget(demon, "Holy water"));
		assertFalse(WeaponCombatRules.usableOnTarget(ordinary, "Bone mace"));
		assertTrue(WeaponCombatRules.usableOnTarget(rat, "Bone mace"));
	}

	@Test
	public void werewolfAuditExplicitlyAvoidsWolfbaneTrap()
	{
		assertTrue(TaskCombatCatalog.isAudited("Werewolves"));
		String note = TaskCombatCatalog.noteFor("Werewolves").toLowerCase(java.util.Locale.ENGLISH);
		assertTrue(note.contains("wolfbane"));
		assertTrue(note.contains("do not"));
		assertTrue(note.contains("do not count"));
	}

	@Test
	public void everyMasterAssignmentHasExplicitCombatAuditEntry()
	{
		Set<String> seen = new HashSet<>();
		for (java.util.List<String> assignments : SlayerMasterCatalog.allAssignments().values())
		{
			for (String task : assignments)
			{
				if (seen.add(task)) assertTrue("Unaudited task: " + task, TaskCombatCatalog.isAudited(task));
			}
		}
	}

	@Test
	public void targetAffinityFamiliesAreAppliedAcrossGenericTasks()
	{
		assertEquals(WeaponRule.DEMONBANE, TaskCombatCatalog.ruleFor("Lesser demons"));
		assertEquals(WeaponRule.DRAGONBANE, TaskCombatCatalog.ruleFor("Green dragons"));
		assertEquals(WeaponRule.RATBANE, TaskCombatCatalog.ruleFor("Brine rats"));
		assertEquals(WeaponRule.KALPHITE, TaskCombatCatalog.ruleFor("Scabarites"));
		assertEquals(WeaponRule.SHADE, TaskCombatCatalog.ruleFor("Shades"));
		assertEquals(WeaponRule.LEAF_BLADED, TaskCombatCatalog.ruleFor("Kurask"));
		assertEquals(WeaponRule.VAMPYRE, TaskCombatCatalog.ruleFor("Vampyres"));
	}


	@Test
	public void revenantsIncludeWildernessWeaponPassive()
	{
		GearStrategy revenant = TaskProfiles.find("Revenants").orElseThrow().getStrategies().stream()
			.filter(s -> s.getCombatStyle() == CombatStyle.RANGED).findFirst()
			.orElse(TaskProfiles.find("Revenants").orElseThrow().getStrategies().get(0));
		GearStrategy ranged = GearStrategy.builder()
			.name("Revenant ranged")
			.combatStyle(CombatStyle.RANGED)
			.targetTraits(TaskCombatCatalog.traitsFor("Revenants"))
			.build();
		assertTrue(TaskCombatCatalog.traitsFor("Revenants").contains(TargetTrait.WILDERNESS));
		assertEquals(1.50, WeaponCombatRules.accuracyMultiplier(ranged, "Webweaver bow"), 0.0001);
		assertEquals(1.50, WeaponCombatRules.damageMultiplier(ranged, "Webweaver bow"), 0.0001);
		assertEquals(1.00, WeaponCombatRules.damageMultiplier(ranged, "Webweaver bow (u)"), 0.0001);
	}

	@Test
	public void waterfiendsUseCrushEarthAndCurrentDemonAttribute()
	{
		assertEquals(WeaponRule.DEMONBANE, TaskCombatCatalog.ruleFor("Waterfiends"));
		assertEquals(AttackType.CRUSH, TaskCombatCatalog.meleeAttackTypeFor("Waterfiends"));
		assertTrue(TaskCombatCatalog.traitsFor("Waterfiends").contains(TargetTrait.DEMON));
		assertEquals(ElementalWeakness.EARTH, TaskCombatCatalog.elementalWeaknessFor("Waterfiends"));
		assertEquals(100, TaskCombatCatalog.elementalWeaknessPercentFor("Waterfiends"));
	}


	@Test
	public void targetSpecificPassiveCanOverrideGenericMeleeAffinity()
	{
		GearStrategy waterfiend = TaskProfiles.find("Waterfiends").orElseThrow().getStrategies().stream()
			.filter(s -> s.getCombatStyle() == CombatStyle.MELEE).findFirst().orElseThrow();
		assertEquals(AttackType.CRUSH, waterfiend.getAttackType());
		assertFalse(WeaponCombatRules.supportsAttackType("Emberlight", AttackType.CRUSH));
		assertTrue(WeaponCombatRules.hasTargetSpecificEffect(waterfiend, "Emberlight"));
		assertFalse(WeaponCombatRules.hasTargetSpecificEffect(waterfiend, "Crystal halberd"));

		GearStrategy gargoyle = TaskProfiles.find("Gargoyles").orElseThrow().getStrategies().get(0);
		assertTrue(WeaponCombatRules.hasTargetSpecificEffect(gargoyle, "Granite hammer"));
		assertFalse(WeaponCombatRules.hasTargetSpecificEffect(gargoyle, "Crystal halberd"));
	}

	@Test
	public void currentTaskAttributesIncludeIcefiendWaterfiendAndLesserNagua()
	{
		assertEquals(WeaponRule.DEMONBANE, TaskCombatCatalog.ruleFor("Icefiends"));
		assertTrue(TaskCombatCatalog.traitsFor("Icefiends").contains(TargetTrait.DEMON));
		assertEquals(ElementalWeakness.FIRE, TaskCombatCatalog.elementalWeaknessFor("Icefiends"));
		assertEquals(100, TaskCombatCatalog.elementalWeaknessPercentFor("Icefiends"));

		assertEquals(WeaponRule.DEMONBANE, TaskCombatCatalog.ruleFor("Waterfiends"));
		assertTrue(TaskCombatCatalog.traitsFor("Waterfiends").contains(TargetTrait.DEMON));
		assertTrue(TaskCombatCatalog.traitsFor("Lesser Nagua").contains(TargetTrait.SPECTRAL));
		assertFalse(TaskCombatCatalog.traitsFor("Ghouls").contains(TargetTrait.UNDEAD));
	}

	@Test
	public void kerisRankingIncludesRareTripleDamageProcAsExpectedValue()
	{
		GearStrategy kalphite = TaskProfiles.find("Kalphites").orElseThrow().getStrategies().get(0);
		double expected = 1.33 * (53.0 / 51.0);
		assertEquals(expected, WeaponCombatRules.damageMultiplier(kalphite, "Keris partisan"), 0.0001);
		assertEquals(1.33, WeaponCombatRules.accuracyMultiplier(kalphite, "Keris partisan of breaching"), 0.0001);
	}

	@Test
	public void leafBladedBattleaxeGetsTargetDamageWithoutHardPreference()
	{
		GearStrategy kurask = TaskProfiles.find("Kurask").orElseThrow().getStrategies().get(0);
		ItemEquipmentStats same = weapon(0, 50, 50, 50, 4);
		double axe = GearScorer.scoreStats(kurask, "Leaf-bladed battleaxe", EquipmentInventorySlot.WEAPON, same);
		double sword = GearScorer.scoreStats(kurask, "Leaf-bladed sword", EquipmentInventorySlot.WEAPON, same);
		assertTrue(axe > sword);
	}

	@Test
	public void smokeDevilCannonAndAncientsRemainPrimaryAfterCombatEnrichment()
	{
		SlayerTaskProfile smoke = TaskProfiles.find("Smoke devils").orElseThrow();
		assertTrue(smoke.getStrategies().get(0).isAncientAoe());
		assertTrue(NameMatcher.normalize(smoke.getStrategies().get(0).getName()).contains("cannon"));
		assertTrue(smoke.getStrategies().stream().anyMatch(s ->
			NameMatcher.normalize(s.getName()).contains("air magic")));
	}

	@Test
	public void virtusBeatsAncestralForEveryAncientAoeMethod()
	{
		ItemEquipmentStats virtus = magicArmour(2, 30);
		ItemEquipmentStats ancestral = magicArmour(3, 30);

		for (SlayerTaskProfile profile : new HashSet<>(TaskProfiles.catalogSnapshot().values()))
		{
			for (GearStrategy strategy : profile.getStrategies())
			{
				if (!strategy.isAncientAoe())
				{
					continue;
				}

				double virtusScore = GearScorer.scoreStats(
					strategy, "Virtus robe top", EquipmentInventorySlot.BODY, virtus);
				double ancestralScore = GearScorer.scoreStats(
					strategy, "Ancestral robe top", EquipmentInventorySlot.BODY, ancestral);
				assertTrue(profile.getKey(), virtusScore > ancestralScore);
			}
		}
	}

	@Test
	public void ancestralRetainsItsVisibleDamageLeadOutsideAncientCombatSpells()
	{
		GearStrategy ordinaryMagic = GearStrategy.builder()
			.name("Ordinary Magic")
			.combatStyle(CombatStyle.MAGIC)
			.build();
		ItemEquipmentStats virtus = magicArmour(2, 30);
		ItemEquipmentStats ancestral = magicArmour(3, 30);

		assertTrue(
			GearScorer.scoreStats(ordinaryMagic, "Ancestral robe top", EquipmentInventorySlot.BODY, ancestral)
				> GearScorer.scoreStats(ordinaryMagic, "Virtus robe top", EquipmentInventorySlot.BODY, virtus));
	}


	@Test
	public void newlyAuditedElementalAndMeleeWeaknessesAreEncoded()
	{
		assertEquals(ElementalWeakness.FIRE, TaskCombatCatalog.elementalWeaknessFor("Cave bugs"));
		assertEquals(50, TaskCombatCatalog.elementalWeaknessPercentFor("Cave bugs"));
		assertEquals(ElementalWeakness.FIRE, TaskCombatCatalog.elementalWeaknessFor("Cave horrors"));
		assertEquals(30, TaskCombatCatalog.elementalWeaknessPercentFor("Cave horrors"));
		assertEquals(ElementalWeakness.EARTH, TaskCombatCatalog.elementalWeaknessFor("Cave kraken"));
		assertEquals(50, TaskCombatCatalog.elementalWeaknessPercentFor("Cave kraken"));
		assertEquals(ElementalWeakness.EARTH, TaskCombatCatalog.elementalWeaknessFor("Cave slimes"));
		assertEquals(ElementalWeakness.EARTH, TaskCombatCatalog.elementalWeaknessFor("Dark beasts"));
		assertEquals(60, TaskCombatCatalog.elementalWeaknessPercentFor("Dark beasts"));
		assertEquals(AttackType.CRUSH, TaskCombatCatalog.meleeAttackTypeFor("Basilisks"));
		assertEquals(ElementalWeakness.EARTH, TaskCombatCatalog.elementalWeaknessFor("Basilisks"));
		assertEquals(40, TaskCombatCatalog.elementalWeaknessPercentFor("Basilisks"));
		assertEquals(AttackType.CRUSH, TaskCombatCatalog.meleeAttackTypeFor("Molanisks"));
		assertEquals(ElementalWeakness.EARTH, TaskCombatCatalog.elementalWeaknessFor("Molanisks"));
		assertEquals(60, TaskCombatCatalog.elementalWeaknessPercentFor("Molanisks"));
		assertEquals(ElementalWeakness.FIRE, TaskCombatCatalog.elementalWeaknessFor("Skeletal wyverns"));
		assertEquals(25, TaskCombatCatalog.elementalWeaknessPercentFor("Skeletal wyverns"));
		assertEquals(ElementalWeakness.EARTH, TaskCombatCatalog.elementalWeaknessFor("Suqahs"));
		assertEquals(20, TaskCombatCatalog.elementalWeaknessPercentFor("Suqahs"));
	}

	@Test
	public void correctedAirWeaknessValuesAreUsed()
	{
		assertEquals(ElementalWeakness.AIR, TaskCombatCatalog.elementalWeaknessFor("Banshees"));
		assertEquals(30, TaskCombatCatalog.elementalWeaknessPercentFor("Banshees"));
		assertEquals(ElementalWeakness.NONE, TaskCombatCatalog.elementalWeaknessFor("Bats"));
		assertEquals(0, TaskCombatCatalog.elementalWeaknessPercentFor("Bats"));
		assertEquals(ElementalWeakness.AIR, TaskCombatCatalog.elementalWeaknessFor("Revenants"));
		assertEquals(30, TaskCombatCatalog.elementalWeaknessPercentFor("Revenants"));
	}

	@Test
	public void broadVariantTasksDoNotInventOneElement()
	{
		for (String task : new String[]{"Bats", "Birds", "Bears", "Spiders"})
		{
			assertEquals(task, ElementalWeakness.NONE, TaskCombatCatalog.elementalWeaknessFor(task));
			assertEquals(task, 0, TaskCombatCatalog.elementalWeaknessPercentFor(task));
			assertTrue(task, TaskCombatCatalog.noteFor(task).contains("no single task-wide element"));
		}

		assertEquals(AttackType.CRUSH, TaskCombatCatalog.meleeAttackTypeFor("Skeletons"));
		assertTrue(TaskCombatCatalog.traitsFor("Skeletons").contains(TargetTrait.UNDEAD));
		assertEquals(ElementalWeakness.NONE, TaskCombatCatalog.elementalWeaknessFor("Skeletons"));
	}

	@Test
	public void blueDragonWeaknessIsLocationAware()
	{
		SlayerTaskProfile ordinary = TaskProfiles.find("Blue dragons", "Taverley Dungeon").orElseThrow();
		assertTrue(ordinary.getStrategies().stream().anyMatch(s ->
			s.getElementalWeakness() == ElementalWeakness.WATER && s.getElementalWeaknessPercent() == 50));

		SlayerTaskProfile tapoyauik = TaskProfiles.find("Blue dragons", "Ruins of Tapoyauik").orElseThrow();
		assertTrue(tapoyauik.getStrategies().stream().anyMatch(s ->
			s.getElementalWeakness() == ElementalWeakness.FIRE && s.getElementalWeaknessPercent() == 50));
		assertFalse(tapoyauik.getStrategies().stream().anyMatch(s ->
			NameMatcher.normalize(s.getName()).contains("water magic")));
	}

	@Test
	public void trollFireWeaknessIsLocationAware()
	{
		SlayerTaskProfile normal = TaskProfiles.find("Trolls", "Mount Quidamortem").orElseThrow();
		assertTrue(normal.getStrategies().stream().anyMatch(s ->
			s.getElementalWeakness() == ElementalWeakness.FIRE && s.getElementalWeaknessPercent() == 50));

		SlayerTaskProfile trollweiss = TaskProfiles.find("Trolls", "Trollweiss Mountain").orElseThrow();
		assertTrue(trollweiss.getStrategies().stream().anyMatch(s ->
			s.getElementalWeakness() == ElementalWeakness.FIRE && s.getElementalWeaknessPercent() == 100));
	}

	@Test
	public void spiritualCreaturesDoNotForceOneElementAcrossFactions()
	{
		assertEquals(ElementalWeakness.NONE, TaskCombatCatalog.elementalWeaknessFor("Spiritual creatures"));
		assertTrue(TaskCombatCatalog.noteFor("Spiritual creatures").contains("faction-dependent"));
	}

	@Test
	public void warpedCreaturesDoNotReceiveInventedTaskWideElement()
	{
		assertEquals(ElementalWeakness.NONE, TaskCombatCatalog.elementalWeaknessFor("Warped Creatures"));
		assertEquals(0, TaskCombatCatalog.elementalWeaknessPercentFor("Warped Creatures"));
	}

	@Test
	public void purgingStaffMethodRequiresMarkOfDarknessContext()
	{
		SlayerTaskProfile demon = TaskProfiles.find("Black demons").orElseThrow();
		assertTrue(demon.getStrategies().stream().anyMatch(s ->
			NameMatcher.normalize(s.getName()).contains("demonbane magic")
				&& NameMatcher.normalize(s.getName()).contains("mark of darkness")
				&& "purging staff".equals(s.getRequiredWeapon())));
	}

	private static ItemEquipmentStats weapon(int stab, int slash, int crush, int strength, int speed)
	{
		return ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.astab(stab)
			.aslash(slash)
			.acrush(crush)
			.str(strength)
			.aspeed(speed)
			.build();
	}

	private static ItemEquipmentStats magicArmour(int magicDamage, int magicAccuracy)
	{
		return ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.mdmg(magicDamage)
			.amagic(magicAccuracy)
			.build();
	}
}
