package com.freearcanes.slayergear;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WikiStrategyAlignmentTest
{
	@Test
	public void aquanitesRankWikiBisMeleeAboveRapier()
	{
		GearStrategy strategy = TaskProfiles.find("Aquanites").orElseThrow().getStrategies().get(0);
		ItemEquipmentStats soulreaper = weapon(28, 134, 66, 121, 5);
		ItemEquipmentStats scythe = weapon(70, 110, 30, 75, 5);
		ItemEquipmentStats rapier = weapon(94, 55, 0, 89, 4);

		assertEquals(AttackType.BALANCED, strategy.getAttackType());
		assertTrue(strategy.getTargetTraits().contains(TargetTrait.SCYTHE_TWO_HIT));
		assertTrue(score(strategy, "Soulreaper axe", soulreaper) > score(strategy, "Ghrazi rapier", rapier));
		assertTrue(score(strategy, "Scythe of vitur", scythe) > score(strategy, "Ghrazi rapier", rapier));
	}

	@Test
	public void auditedProfilesUseWikiAttackStyles()
	{
		assertMainMeleeType("Araxxor", AttackType.CRUSH);
		assertMainMeleeType("Cerberus", AttackType.CRUSH);
		assertMainMeleeType("Duke Sucellus", AttackType.SLASH);
		assertMainMeleeType("Sarachnis", AttackType.CRUSH);
		assertMainMeleeType("Vardorvis", AttackType.SLASH);
		assertMainMeleeType("Skotizo", AttackType.SLASH);
		assertMainMeleeType("Vet'ion", AttackType.CRUSH);
		assertMainMeleeType("Gryphons", AttackType.STAB);
		assertMainMeleeType("Kalphites", AttackType.CRUSH);
		assertMainMeleeType("Waterfiends", AttackType.CRUSH);
		assertMainMeleeType("Wyrms", AttackType.BALANCED);
	}

	@Test
	public void wikiOathplateOverTorvaCasesAreExplicitAndScoreCorrectly()
	{
		// These are the audited Slayer/task-boss pages that rank Oathplate above
		// Torva for their primary offensive slash setup, rather than listing the
		// two armours as tied or placing Torva first.
		assertOathplateAboveTorva("Aquanites");
		assertOathplateAboveTorva("Dark beasts");
		assertOathplateAboveTorva("Duke Sucellus");
		assertOathplateAboveTorva("Vardorvis");
		assertOathplateAboveTorva("Skotizo");
	}

	@Test
	public void wikiTieOrTorvaFirstCasesDoNotReceiveOathplateOverride()
	{
		assertNoOathplateOverride("Abyssal demons");
		assertNoOathplateOverride("Nechryael");
		assertNoOathplateOverride("Gargoyles");
		assertNoOathplateOverride("Custodian stalker");
		assertNoOathplateOverride("The thermonuclear smoke devil");
		assertNoOathplateOverride("The grotesque guardians");
	}

	@Test
	public void largeBossProfilesModelScytheHitCount()
	{
		assertTrait("Araxxor", TargetTrait.SCYTHE_THREE_HIT);
		assertTrait("Cerberus", TargetTrait.SCYTHE_THREE_HIT);
		assertTrait("Duke Sucellus", TargetTrait.SCYTHE_THREE_HIT);
		assertTrait("Sarachnis", TargetTrait.SCYTHE_THREE_HIT);
		assertTrait("Kalphite Queen", TargetTrait.SCYTHE_THREE_HIT);
		assertTrait("Vardorvis", TargetTrait.SCYTHE_TWO_HIT);
	}

	@Test
	public void intrinsicWeaponMechanicsAreVisibleAndStyleSafe()
	{
		GearStrategy aquanite = TaskProfiles.find("Aquanites").orElseThrow().getStrategies().get(0);
		GearStrategy araxxor = TaskProfiles.find("Araxxor").orElseThrow().getStrategies().get(0);

		assertEquals(1.18, WeaponCombatRules.intrinsicDamageMultiplier(aquanite, "Soulreaper axe"), 0.0001);
		assertEquals("Sustained Soul stack value", WeaponCombatRules.intrinsicReason(aquanite, "Soulreaper axe"));
		assertEquals(1.50, WeaponCombatRules.intrinsicDamageMultiplier(aquanite, "Scythe of vitur"), 0.0001);
		assertEquals(1.75, WeaponCombatRules.intrinsicDamageMultiplier(araxxor, "Scythe of vitur"), 0.0001);
		assertTrue(!WeaponCombatRules.hasTargetSpecificEffect(araxxor, "Noxious halberd"));
	}

	private static void assertMainMeleeType(String task, AttackType expected)
	{
		GearStrategy melee = TaskProfiles.find(task).orElseThrow().getStrategies().stream()
			.filter(strategy -> strategy.getCombatStyle() == CombatStyle.MELEE)
			.findFirst().orElseThrow();
		assertEquals(task, expected, melee.getAttackType());
	}

	private static void assertTrait(String task, TargetTrait trait)
	{
		assertTrue(task, TaskProfiles.find(task).orElseThrow().getStrategies().get(0).getTargetTraits().contains(trait));
	}

	private static void assertOathplateAboveTorva(String task)
	{
		GearStrategy strategy = TaskProfiles.find(task).orElseThrow().getStrategies().stream()
			.filter(value -> value.getCombatStyle() == CombatStyle.MELEE)
			.findFirst().orElseThrow();
		assertTrue(task, strategy.getPreferredItems().stream()
			.anyMatch(item -> NameMatcher.normalize(item).contains("oathplate")));

		ItemEquipmentStats oathplateChest = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.aslash(16).str(4).dstab(105).dslash(128).dcrush(100).dmagic(-5).drange(112)
			.build();
		ItemEquipmentStats torvaBody = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.BODY.getSlotIdx())
			.str(6).prayer(1).dstab(117).dslash(111).dcrush(117).dmagic(-11).drange(142)
			.build();
		ItemEquipmentStats oathplateLegs = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.LEGS.getSlotIdx())
			.aslash(12).str(2).dstab(75).dslash(100).dcrush(73).dmagic(-3).drange(81)
			.build();
		ItemEquipmentStats torvaLegs = ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.LEGS.getSlotIdx())
			.str(4).prayer(1).dstab(87).dslash(78).dcrush(79).dmagic(-9).drange(102)
			.build();

		assertTrue(task + " body", GearScorer.scoreStats(strategy, "Oathplate chest",
			EquipmentInventorySlot.BODY, oathplateChest)
			> GearScorer.scoreStats(strategy, "Torva platebody",
				EquipmentInventorySlot.BODY, torvaBody));
		assertTrue(task + " legs", GearScorer.scoreStats(strategy, "Oathplate legs",
			EquipmentInventorySlot.LEGS, oathplateLegs)
			> GearScorer.scoreStats(strategy, "Torva platelegs",
				EquipmentInventorySlot.LEGS, torvaLegs));
	}

	private static void assertNoOathplateOverride(String task)
	{
		GearStrategy strategy = TaskProfiles.find(task).orElseThrow().getStrategies().stream()
			.filter(value -> value.getCombatStyle() == CombatStyle.MELEE)
			.findFirst().orElseThrow();
		assertTrue(task, strategy.getPreferredItems().stream()
			.noneMatch(item -> NameMatcher.normalize(item).contains("oathplate")));
	}

	private static double score(GearStrategy strategy, String name, ItemEquipmentStats stats)
	{
		return GearScorer.scoreStats(strategy, name, EquipmentInventorySlot.WEAPON, stats);
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
}
