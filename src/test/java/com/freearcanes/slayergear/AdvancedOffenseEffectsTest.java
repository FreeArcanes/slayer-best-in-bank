package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AdvancedOffenseEffectsTest
{
	@Test
	public void rubyUsesSustainedTargetHpCurveAndKandarinProcRate()
	{
		TargetDefence target = target(1000, "", "false", 100, 0);
		CombatLevelContext normal = CombatLevelContext.unboosted(80, 80, 80, 99);
		CombatLevelContext diary = normal.withKandarinHardDiary(true);

		assertEquals(13.9, EnchantedBoltEffects.apply("Ruby bolts (e)",
			"Rune crossbow", 10, 0.5, 20, target, normal).getDamage(), 0.000001);
		assertTrue(EnchantedBoltEffects.apply("Ruby dragon bolts (e)",
			"Zaryte crossbow", 10, 0.5, 20, target, diary).getDamage() > 13.9);
		assertEquals(50, EnchantedBoltEffects.sustainedRubyProc(500, false), 0.000001);
		assertEquals(75, EnchantedBoltEffects.sustainedRubyProc(1000, false), 0.000001);
	}

	@Test
	public void diamondProcBypassesAccuracyAndRaisesMaxHit()
	{
		TargetDefence target = target(100, "", "false", 100, 0);
		double damage = EnchantedBoltEffects.apply("Diamond bolts (e)",
			"Rune crossbow", 2, 0.1, 20, target,
			CombatLevelContext.unboosted(80, 80, 80, 80)).getDamage();
		assertTrue(damage > 2);
	}

	@Test
	public void immuneTargetAttributesSuppressRelevantBoltEffects()
	{
		CombatLevelContext levels = CombatLevelContext.unboosted(80, 80, 80, 99);
		assertEquals(10, EnchantedBoltEffects.apply("Dragonstone bolts (e)",
			"Rune crossbow", 10, 0.5, 20,
			target(100, "dragon,fiery", "false", 100, 0), levels).getDamage(), 0.000001);
		assertEquals(10, EnchantedBoltEffects.apply("Onyx bolts (e)",
			"Rune crossbow", 10, 0.5, 20,
			target(100, "undead", "false", 100, 0), levels).getDamage(), 0.000001);
	}

	@Test
	public void twistedBowMatchesLowMagicExampleAndOutsideCap()
	{
		TargetDefence low = target(100, "", "false", 1, 0);
		assertEquals(0.405, TwistedBowEffect.accuracyMultiplier(low), 0.002);
		assertEquals(0.547, TwistedBowEffect.damageMultiplier(low), 0.002);
		TargetDefence capped = target(100, "", "false", 400, 500);
		assertEquals(250, TwistedBowEffect.targetMagic(capped));
		assertTrue(TwistedBowEffect.accuracyMultiplier(capped) <= 1.40);
		assertTrue(TwistedBowEffect.damageMultiplier(capped) <= 2.50);
	}

	@Test
	public void shadowUsesThreeTimesOutsideToaAndFourTimesInside()
	{
		GearStrategy outside = GearStrategy.builder().name("Shadow")
			.combatStyle(CombatStyle.MAGIC).build();
		GearStrategy inside = GearStrategy.builder().name("Shadow ToA")
			.combatStyle(CombatStyle.MAGIC).location("Tombs of Amascut").build();
		assertEquals(3, LoadoutOffenseEstimator.shadowMultiplier(outside));
		assertEquals(4, LoadoutOffenseEstimator.shadowMultiplier(inside));
	}

	@Test
	public void multiTargetEffectsUseDocumentedHitCounts()
	{
		assertEquals(18, LoadoutOffenseEstimator.venatorAggregateDamage(10, 0.5, 8), 0.000001);
		assertEquals(90, LoadoutOffenseEstimator.ancientAggregateDamage(10), 0.000001);
	}

	@Test
	public void eclipseBurnHonoursMonsterBurnResponse()
	{
		assertTrue(LoadoutOffenseEstimator.isImmuneToNormalBurn(
			target(100, "", "Normal", 1, 0)));
		assertTrue(!LoadoutOffenseEstimator.isImmuneToNormalBurn(
			target(100, "", "Weak", 1, 0)));
	}

	@Test
	public void killRateTextReportsSingleAndVariantTargets()
	{
		LoadoutOffenseEstimate single = LoadoutOffenseEstimate.range(
			5, 5, 0, "Target", "Method", 20, 20);
		assertEquals("20.0 sec estimated TTK · 180 kills/hr",
			SlayerGearPanel.killRateText(single));
		LoadoutOffenseEstimate variants = LoadoutOffenseEstimate.range(
			5, 8, 0, "variants", "", 10, 30);
		assertEquals("10.0–30.0 sec equivalent kill interval · 120–360 kills/hr",
			SlayerGearPanel.killRateText(variants));
	}

	private static TargetDefence target(int hp, String attributes,
		String burnResponse, int magicLevel, int magicAttack)
	{
		return new TargetDefence("Test", 100, magicLevel,
			0, 0, 0, 0, 0, 0, 0, hp, magicAttack, 1, attributes, burnResponse);
	}
}
