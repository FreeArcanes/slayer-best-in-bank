package com.freearcanes.slayergear;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadoutOffenseEstimatorTest
{
	@Test
	public void fasterStrongerRangedLoadoutEstimatesHigherOutput()
	{
		GearStrategy ranged = GearStrategy.builder().combatStyle(CombatStyle.RANGED).build();
		double slow = estimate(ranged, rangedWeapon(70, 60, 5), 80, 80);
		double fast = estimate(ranged, rangedWeapon(90, 85, 4), 80, 80);

		assertTrue(fast > slow);
	}

	@Test
	public void playerLevelAffectsEstimate()
	{
		GearStrategy melee = GearStrategy.builder()
			.combatStyle(CombatStyle.MELEE).attackType(AttackType.SLASH).build();
		ItemEquipmentStats weapon = ItemEquipmentStats.builder()
			.aslash(82).str(80).aspeed(4).build();

		assertTrue(estimate(melee, weapon, 99, 99) > estimate(melee, weapon, 60, 60));
	}

	@Test
	public void panelSubtitleReportsNeutralBenchmarkAndTierDifference()
	{
		Map<EquipmentInventorySlot, GearRecommendation> empty = new EnumMap<>(EquipmentInventorySlot.class);
		List<LoadoutTier> tiers = List.of(
			new LoadoutTier(1, empty, 0, 0, LoadoutOffenseEstimate.estimated(5.25, 0)),
			new LoadoutTier(2, empty, 0, 0, LoadoutOffenseEstimate.estimated(4.99, -5.0)));

		assertEquals("Est. 5.25 DPS · T2 -5.0%",
			SlayerGearPanel.offenseComparisonSubtitle(tiers));
		assertEquals("T2/T3 show swaps only",
			SlayerGearPanel.offenseComparisonSubtitle(List.of(new LoadoutTier(1, empty))));
	}

	@Test
	public void panelSubtitleShowsVariantDpsRange()
	{
		Map<EquipmentInventorySlot, GearRecommendation> empty = new EnumMap<>(EquipmentInventorySlot.class);
		LoadoutTier tier = new LoadoutTier(1, empty, 0, 0,
			LoadoutOffenseEstimate.range(3.25, 4.75, 0, "4 target variants"));
		assertEquals("Est. 3.25–4.75 DPS vs 4 target variants",
			SlayerGearPanel.offenseComparisonSubtitle(List.of(tier)));
		assertEquals("3.25–4.75 DPS", SlayerGearPanel.formatDps(tier.getOffenseEstimate()));
	}

	@Test
	public void hitChanceMatchesWikiFormulaOnBothBranches()
	{
		assertEquals(10000.0 / (2.0 * 20001.0),
			LoadoutOffenseEstimator.normalHitChance(10000, 20000), 0.0000001);
		assertEquals(1.0 - 10002.0 / (2.0 * 20001.0),
			LoadoutOffenseEstimator.normalHitChance(20000, 10000), 0.0000001);
	}

	@Test
	public void atlatlUsesWholeLoadoutMeleeStrengthAndStrengthLevel()
	{
		GearStrategy ranged = GearStrategy.builder().combatStyle(CombatStyle.RANGED).build();
		GearRecommendation weaponRecommendation = recommendation(
			1, "Eclipse atlatl", EquipmentInventorySlot.WEAPON);
		GearRecommendation amuletRecommendation = recommendation(
			2, "Strength amulet", EquipmentInventorySlot.AMULET);
		Map<EquipmentInventorySlot, GearRecommendation> loadout = new EnumMap<>(EquipmentInventorySlot.class);
		loadout.put(EquipmentInventorySlot.WEAPON, weaponRecommendation);
		loadout.put(EquipmentInventorySlot.AMULET, amuletRecommendation);
		Map<Integer, GearScorer.BankEquipment> equipment = Map.of(
			1, bankEquipment(1, "Eclipse atlatl", EquipmentInventorySlot.WEAPON,
				ItemEquipmentStats.builder().arange(87).str(40).aspeed(4).build()),
			2, bankEquipment(2, "Strength amulet", EquipmentInventorySlot.AMULET,
				ItemEquipmentStats.builder().str(10).build()));
		TargetDefence target = new TargetDefence("Test", 100, 1, 0, 0, 0, 0, 0, 0, 0);

		double highStrength = LoadoutOffenseEstimator.estimate(loadout, equipment, ranged,
			target, CombatLevelContext.unboosted(80, 99, 1, 80));
		double lowStrength = LoadoutOffenseEstimator.estimate(loadout, equipment, ranged,
			target, CombatLevelContext.unboosted(80, 50, 1, 80));
		assertTrue(highStrength > lowStrength);
	}

	@Test
	public void identifiedElementalMagicProducesTargetAwareDps()
	{
		GearStrategy air = GearStrategy.builder().combatStyle(CombatStyle.MAGIC)
			.elementalWeakness(ElementalWeakness.AIR, 50).build();
		GearRecommendation weaponRecommendation = recommendation(
			1, "Mystic air staff", EquipmentInventorySlot.WEAPON);
		Map<EquipmentInventorySlot, GearRecommendation> loadout = new EnumMap<>(EquipmentInventorySlot.class);
		loadout.put(EquipmentInventorySlot.WEAPON, weaponRecommendation);
		Map<Integer, GearScorer.BankEquipment> equipment = Map.of(1,
			bankEquipment(1, "Mystic air staff", EquipmentInventorySlot.WEAPON,
				ItemEquipmentStats.builder().amagic(14).mdmg(10).aspeed(5).build()));
		TargetDefence target = new TargetDefence("Weak target", 50, 50,
			0, 0, 0, 0, 0, 0, 0);
		CombatLevelContext context = CombatLevelContext.effective(
			80, 1, 80, 1, 80, 1, 1, 99, 1.25, 4);

		assertTrue(LoadoutOffenseEstimator.estimate(
			loadout, equipment, air, target, context) > 0);
	}

	@Test
	public void taskHeadgearRecognizesImbuesAndCosmeticVariants()
	{
		assertEquals(7.0 / 6.0, SlayerTaskHeadgear.accuracyMultiplier(
			"Black mask (10)", CombatStyle.MELEE), 0.000001);
		assertEquals(1.15, SlayerTaskHeadgear.accuracyMultiplier(
			"Twisted slayer helmet (i)", CombatStyle.RANGED), 0.000001);
		assertEquals(1.15, SlayerTaskHeadgear.damageMultiplier(
			"Tzkal slayer helm (i)", CombatStyle.MAGIC), 0.000001);
		assertEquals(1.0, SlayerTaskHeadgear.accuracyMultiplier(
			"Purple slayer helmet", CombatStyle.RANGED), 0.000001);
	}

	@Test
	public void imbuedSlayerHelmetRaisesRangedDpsWhileUnimbuedDoesNot()
	{
		GearStrategy ranged = GearStrategy.builder().combatStyle(CombatStyle.RANGED).build();
		double noHelmet = estimateWithHeadgear(ranged, null);
		double unimbued = estimateWithHeadgear(ranged, "Slayer helmet");
		double imbued = estimateWithHeadgear(ranged, "Black slayer helmet (i)");

		assertEquals(noHelmet, unimbued, 0.000001);
		assertTrue(imbued > unimbued);
	}

	private static ItemEquipmentStats rangedWeapon(int accuracy, int strength, int speed)
	{
		return ItemEquipmentStats.builder().arange(accuracy).rstr(strength).aspeed(speed).build();
	}

	private static double estimate(
		GearStrategy strategy, ItemEquipmentStats stats, int accuracyLevel, int strengthLevel)
	{
		GearRecommendation recommendation = GearRecommendation.builder()
			.itemId(1).canonicalItemId(1).itemName("Test weapon")
			.slot(EquipmentInventorySlot.WEAPON).build();
		Map<EquipmentInventorySlot, GearRecommendation> loadout =
			new EnumMap<>(EquipmentInventorySlot.class);
		loadout.put(EquipmentInventorySlot.WEAPON, recommendation);
		GearScorer.BankEquipment equipment = new GearScorer.BankEquipment(
			1, 1, "Test weapon", EquipmentInventorySlot.WEAPON, stats, true, false);
		TargetDefence target = new TargetDefence(
			"Test target", 100, 100, 0, 0, 0, 0, 0, 0, 0);
		return LoadoutOffenseEstimator.estimate(loadout, Map.of(1, equipment), strategy, target,
			CombatLevelContext.unboosted(
				accuracyLevel, strengthLevel, accuracyLevel, accuracyLevel));
	}

	private static double estimateWithHeadgear(GearStrategy strategy, String headgear)
	{
		Map<EquipmentInventorySlot, GearRecommendation> loadout =
			new EnumMap<>(EquipmentInventorySlot.class);
		loadout.put(EquipmentInventorySlot.WEAPON,
			recommendation(1, "Test bow", EquipmentInventorySlot.WEAPON));
		Map<Integer, GearScorer.BankEquipment> equipment = new java.util.HashMap<>();
		equipment.put(1, bankEquipment(1, "Test bow", EquipmentInventorySlot.WEAPON,
			ItemEquipmentStats.builder().arange(80).rstr(80).aspeed(4).build()));
		if (headgear != null)
		{
			loadout.put(EquipmentInventorySlot.HEAD,
				recommendation(2, headgear, EquipmentInventorySlot.HEAD));
			equipment.put(2, bankEquipment(2, headgear, EquipmentInventorySlot.HEAD,
				ItemEquipmentStats.builder().build()));
		}
		return LoadoutOffenseEstimator.estimate(loadout, equipment, strategy,
			new TargetDefence("Test", 100, 100, 0, 0, 0, 0, 0, 0, 0),
			CombatLevelContext.unboosted(80, 80, 80, 80));
	}

	private static GearRecommendation recommendation(
		int id, String name, EquipmentInventorySlot slot)
	{
		return GearRecommendation.builder().itemId(id).canonicalItemId(id)
			.itemName(name).slot(slot).build();
	}

	private static GearScorer.BankEquipment bankEquipment(
		int id, String name, EquipmentInventorySlot slot, ItemEquipmentStats stats)
	{
		return new GearScorer.BankEquipment(id, id, name, slot, stats, true, false);
	}
}
