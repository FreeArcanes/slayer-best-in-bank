package com.freearcanes.slayergear;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WeaponScoringRegressionTest
{
	@Test
	public void emberlightBeatsAbyssalWhipOnBlackDemonsInBothPriorities()
	{
		GearStrategy demon = TaskProfiles.find("Black demons").orElseThrow()
			.getStrategies().stream()
			.filter(s -> s.getCombatStyle() == CombatStyle.MELEE)
			.findFirst().orElseThrow();

		ItemEquipmentStats emberlight = melee(63, 70, 0, 13, 4);
		ItemEquipmentStats whip = melee(0, 82, 0, 82, 4);

		double balancedEmber = GearScorer.scoreStats(demon, "Emberlight",
			EquipmentInventorySlot.WEAPON, emberlight, GearPriority.BALANCED);
		double balancedWhip = GearScorer.scoreStats(demon, "Abyssal whip",
			EquipmentInventorySlot.WEAPON, whip, GearPriority.BALANCED);
		double prayerEmber = GearScorer.scoreStats(demon, "Emberlight",
			EquipmentInventorySlot.WEAPON, emberlight, GearPriority.PRAYER_FIRST);
		double prayerWhip = GearScorer.scoreStats(demon, "Abyssal whip",
			EquipmentInventorySlot.WEAPON, whip, GearPriority.PRAYER_FIRST);

		assertTrue("Emberlight must beat Abyssal whip on a demon", balancedEmber > balancedWhip);
		assertTrue("Prayer First must preserve combat-optimal weapon ordering", prayerEmber > prayerWhip);
	}

	@Test
	public void abyssalWhipBeatsEmberlightOffTarget()
	{
		GearStrategy ordinary = GearStrategy.builder()
			.name("Ordinary slash")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.SLASH)
			.build();

		double ember = GearScorer.scoreStats(ordinary, "Emberlight",
			EquipmentInventorySlot.WEAPON, melee(63, 70, 0, 13, 4));
		double whip = GearScorer.scoreStats(ordinary, "Abyssal whip",
			EquipmentInventorySlot.WEAPON, melee(0, 82, 0, 82, 4));

		assertTrue("Demonbane must not be hard-forced off target", whip > ember);
	}

	@Test
	public void graniteHammerIsStrongOnGargoylesButStillDynamic()
	{
		GearStrategy gargoyle = TaskProfiles.find("Gargoyles").orElseThrow()
			.getStrategies().get(0);

		double granite = GearScorer.scoreStats(gargoyle, "Granite hammer",
			EquipmentInventorySlot.WEAPON, melee(0, 0, 57, 56, 4));
		double cudgel = GearScorer.scoreStats(gargoyle, "Abyssal cudgel",
			EquipmentInventorySlot.WEAPON, melee(0, 0, 65, 70, 4));
		double exceptional = GearScorer.scoreStats(gargoyle, "Exceptional crush weapon",
			EquipmentInventorySlot.WEAPON, melee(0, 0, 160, 160, 4));

		assertTrue("Granite hammer should beat a normal strong Crush alternative", granite > cudgel);
		assertTrue("Golembane must remain dynamic rather than hard-forced", exceptional > granite);
	}

	@Test
	public void dragonHunterLanceBeatsRapierProxyOnDragons()
	{
		GearStrategy dragon = GearStrategy.builder()
			.name("Dragonbane melee")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.STAB)
			.weaponRule(WeaponRule.DRAGONBANE)
			.targetTrait(TargetTrait.DRAGON)
			.build();

		double lance = GearScorer.scoreStats(dragon, "Dragon hunter lance",
			EquipmentInventorySlot.WEAPON, melee(85, 65, 0, 70, 4));
		double rapier = GearScorer.scoreStats(dragon, "Ghrazi rapier",
			EquipmentInventorySlot.WEAPON, melee(94, 55, 0, 89, 4));

		assertTrue("Dragon hunter lance must receive whole-roll dragonbane value", lance > rapier);
	}

	@Test
	public void dragonHunterCrossbowBeatsDragonCrossbowOnDragons()
	{
		GearStrategy dragon = GearStrategy.builder()
			.name("Dragonbane ranged")
			.combatStyle(CombatStyle.RANGED)
			.weaponRule(WeaponRule.DRAGONBANE)
			.targetTrait(TargetTrait.DRAGON)
			.build();

		double hunter = GearScorer.scoreStats(dragon, "Dragon hunter crossbow",
			EquipmentInventorySlot.WEAPON, ranged(95, 0, 5));
		double generic = GearScorer.scoreStats(dragon, "Dragon crossbow",
			EquipmentInventorySlot.WEAPON, ranged(94, 0, 5));

		assertTrue("Dragon hunter crossbow target effect must matter", hunter > generic);
	}

	@Test
	public void dragonHunterWandBeatsGenericWandOnDragons()
	{
		GearStrategy dragon = GearStrategy.builder()
			.name("Dragonbane magic")
			.combatStyle(CombatStyle.MAGIC)
			.weaponRule(WeaponRule.DRAGONBANE)
			.targetTrait(TargetTrait.DRAGON)
			.build();

		double hunter = GearScorer.scoreStats(dragon, "Dragon hunter wand",
			EquipmentInventorySlot.WEAPON, magic(16, 10, 5));
		double generic = GearScorer.scoreStats(dragon, "Master wand",
			EquipmentInventorySlot.WEAPON, magic(20, 0, 5));

		assertTrue("Dragon hunter wand must scale the whole magic roll proxy", hunter > generic);
	}

	@Test
	public void kerisBreachingBeatsGenericHastaProxyOnKalphites()
	{
		GearStrategy kalphite = GearStrategy.builder()
			.name("Kalphite melee")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.STAB)
			.weaponRule(WeaponRule.KALPHITE)
			.targetTrait(TargetTrait.KALPHITE)
			.build();

		double keris = GearScorer.scoreStats(kalphite, "Keris partisan of breaching",
			EquipmentInventorySlot.WEAPON, melee(58, 57, 55, 45, 4));
		double hasta = GearScorer.scoreStats(kalphite, "Zamorakian hasta",
			EquipmentInventorySlot.WEAPON, melee(85, 65, 65, 75, 4));

		assertTrue("Keris Breaching should receive Kalphite accuracy/damage effects", keris > hasta);
	}

	@Test
	public void ratBoneMaceFlatMaxHitCanBeatWhipOnRats()
	{
		GearStrategy rat = GearStrategy.builder()
			.name("Rat melee")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.CRUSH)
			.weaponRule(WeaponRule.RATBANE)
			.targetTrait(TargetTrait.RAT)
			.build();

		double mace = GearScorer.scoreStats(rat, "Bone mace",
			EquipmentInventorySlot.WEAPON, melee(0, 0, 70, 70, 4));
		double whip = GearScorer.scoreStats(rat, "Abyssal whip",
			EquipmentInventorySlot.WEAPON, melee(0, 82, 0, 82, 4));

		assertTrue("Ratbone +10 max hit must materially affect ranking", mace > whip);
	}

	@Test
	public void shadePassiveIsModeledButNotHardForced()
	{
		GearStrategy shade = GearStrategy.builder()
			.name("Shade melee")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.BALANCED)
			.weaponRule(WeaponRule.SHADE)
			.targetTrait(TargetTrait.SHADE)
			.build();

		double gadderhammer = GearScorer.scoreStats(shade, "Gadderhammer",
			EquipmentInventorySlot.WEAPON, melee(0, 0, 35, 35, 7));
		double whip = GearScorer.scoreStats(shade, "Abyssal whip",
			EquipmentInventorySlot.WEAPON, melee(0, 82, 0, 82, 4));

		assertTrue("Target passive should not be a hard unconditional winner", whip > gadderhammer);
	}

	@Test
	public void chargedWildernessWeaponBeatsSameStatsUncharged()
	{
		GearStrategy wilderness = GearStrategy.builder()
			.name("Revenant ranged")
			.combatStyle(CombatStyle.RANGED)
			.targetTrait(TargetTrait.WILDERNESS)
			.build();

		ItemEquipmentStats same = ranged(75, 60, 4);
		double charged = GearScorer.scoreStats(wilderness, "Webweaver bow",
			EquipmentInventorySlot.WEAPON, same);
		double uncharged = GearScorer.scoreStats(wilderness, "Webweaver bow (u)",
			EquipmentInventorySlot.WEAPON, same);

		assertTrue("Only charged Wilderness weapon should get target scaling", charged > uncharged);
	}

	@Test
	public void currentSunspearVampyreBonusOutranksOlderFlailProxy()
	{
		GearStrategy vampyre = GearStrategy.builder()
			.name("Vampyre melee")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.SLASH)
			.weaponRule(WeaponRule.VAMPYRE)
			.targetTrait(TargetTrait.VAMPYRE)
			.build();

		double sunspear = GearScorer.scoreStats(vampyre, "Sunspear",
			EquipmentInventorySlot.WEAPON, melee(80, 95, 70, 85, 4));
		double flail = GearScorer.scoreStats(vampyre, "Blisterwood flail",
			EquipmentInventorySlot.WEAPON, melee(0, 72, 82, 63, 5));

		assertTrue("Current Sunspear Vampyre effect should materially affect ranking", sunspear > flail);
	}

	@Test
	public void preferredItemIsTieBreakerNotHardOverride()
	{
		GearStrategy preferred = GearStrategy.builder()
			.name("Preference test")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.SLASH)
			.preferredItem("weak sword")
			.build();

		double weak = GearScorer.scoreStats(preferred, "Weak sword",
			EquipmentInventorySlot.WEAPON, melee(0, 10, 0, 10, 4));
		double strong = GearScorer.scoreStats(preferred, "Clearly strong sword",
			EquipmentInventorySlot.WEAPON, melee(0, 100, 0, 100, 4));

		assertTrue("Curated item names cannot overwhelm a clearly stronger valid item", strong > weak);
	}

	@Test
	public void attackSpeedScalesWholeWeaponOffence()
	{
		GearStrategy ordinary = GearStrategy.builder()
			.name("Speed test")
			.combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.SLASH)
			.build();

		double fourTick = GearScorer.scoreStats(ordinary, "Four tick sword",
			EquipmentInventorySlot.WEAPON, melee(0, 70, 0, 70, 4));
		double fiveTick = GearScorer.scoreStats(ordinary, "Five tick sword",
			EquipmentInventorySlot.WEAPON, melee(0, 70, 0, 70, 5));

		assertTrue("Attack speed must scale the full attack contribution", fourTick > fiveTick);
	}

	@Test
	public void toxicBlowpipeCountsLoadedDartsAgainstSunlightCrossbow()
	{
		GearStrategy ordinary = GearStrategy.builder()
			.name("Ordinary ranged Slayer")
			.combatStyle(CombatStyle.RANGED)
			.build();

		double blowpipe = GearScorer.scoreStats(ordinary, "Toxic blowpipe",
			EquipmentInventorySlot.WEAPON, ranged(30, 20, 3));
		double sunlightCrossbow = GearScorer.scoreStats(ordinary, "Hunter's sunlight crossbow",
			EquipmentInventorySlot.WEAPON, ranged(79, 0, 4));
		double sunlightBolts = GearScorer.scoreStats(ordinary, "Sunlight antler bolts",
			EquipmentInventorySlot.AMMO, rangedAmmo(55));

		assertTrue("Loaded darts must keep Toxic blowpipe ahead of the sunlight crossbow package",
			blowpipe > sunlightCrossbow + sunlightBolts);
	}

	@Test
	public void venatorStrategyOnlyAllowsVenatorBow()
	{
		GearStrategy venator = GearStrategy.builder()
			.name("Venator multi-target")
			.combatStyle(CombatStyle.RANGED)
			.requiredWeapon("venator bow")
			.build();

		assertTrue(GearScorer.allowed(weapon("Venator bow", ranged(90, 25, 5)), venator));
		assertTrue(!GearScorer.allowed(weapon("Hunter's sunlight crossbow", ranged(79, 0, 4)), venator));
	}

	@Test
	public void jadProfileUsesOwnedBlowpipeMethodBeforeGenericRangedFallback()
	{
		SlayerTaskProfile jad = TaskProfiles.find("TzTok-Jad").orElseThrow();
		GearStrategy primary = jad.getStrategies().get(0);
		GearStrategy fallback = jad.getStrategies().get(1);

		assertTrue(NameMatcher.matchesAnyToken("Toxic blowpipe", primary.getRequiredWeapon()));
		assertTrue(GearScorer.allowed(weapon("Toxic blowpipe", ranged(30, 20, 3)), primary));
		assertTrue(!GearScorer.allowed(weapon("Hunter's sunlight crossbow", ranged(79, 0, 4)), primary));
		assertTrue(fallback.getRequiredWeapon() == null);
	}

	@Test
	public void thermyWikiOrderPlacesFangAheadOfNoxiousHalberd()
	{
		GearStrategy thermy = TaskProfiles.find("Thermonuclear smoke devil").orElseThrow()
			.getStrategies().get(0);
		double fang = GearScorer.scoreStats(thermy, "Osmumten's fang",
			EquipmentInventorySlot.WEAPON, melee(105, 75, 0, 103, 5));
		double halberd = GearScorer.scoreStats(thermy, "Noxious halberd",
			EquipmentInventorySlot.WEAPON, melee(95, 145, 0, 124, 5));

		assertTrue("Thermy must follow the Wiki method order even when raw stats favour Noxious halberd",
			fang > halberd);
	}

	@Test
	public void araxxorCrushMethodRejectsVoidwakerAsMainHand()
	{
		GearStrategy araxxor = TaskProfiles.find("Araxxor").orElseThrow()
			.getStrategies().get(0);

		assertTrue(GearScorer.allowed(weapon("Inquisitor's mace",
			melee(0, 0, 95, 89, 4)), araxxor));
		assertTrue("Voidwaker has no Crush attack style",
			!GearScorer.allowed(weapon("Voidwaker",
				melee(80, 80, -2, 80, 4)), araxxor));
	}

	private static ItemEquipmentStats melee(int stab, int slash, int crush, int strength, int speed)
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

	private static ItemEquipmentStats ranged(int accuracy, int strength, int speed)
	{
		return ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.arange(accuracy)
			.rstr(strength)
			.aspeed(speed)
			.build();
	}

	private static ItemEquipmentStats rangedAmmo(int strength)
	{
		return ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.AMMO.getSlotIdx())
			.rstr(strength)
			.build();
	}

	private static GearScorer.BankEquipment weapon(String name, ItemEquipmentStats stats)
	{
		return new GearScorer.BankEquipment(1, 1, name,
			EquipmentInventorySlot.WEAPON, stats, true, false);
	}

	private static ItemEquipmentStats magic(int accuracy, int damagePercent, int speed)
	{
		return ItemEquipmentStats.builder()
			.slot(EquipmentInventorySlot.WEAPON.getSlotIdx())
			.amagic(accuracy)
			.mdmg(damagePercent)
			.aspeed(speed)
			.build();
	}
}
