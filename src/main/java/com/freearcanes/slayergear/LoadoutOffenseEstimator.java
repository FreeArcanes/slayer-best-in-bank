package com.freearcanes.slayergear;

import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;

/**
 * Applies the standard OSRS Wiki attack-roll, hit-chance, max-hit and average
 * damage formulas to loadouts with an unambiguous target.
 */
final class LoadoutOffenseEstimator
{
	private LoadoutOffenseEstimator() {}

	static double estimate(
		Map<EquipmentInventorySlot, GearRecommendation> loadout,
		Map<Integer, GearScorer.BankEquipment> equipmentByCanonicalId,
		GearStrategy strategy,
		TargetDefence target,
		CombatLevelContext levels)
	{
		if (loadout == null || loadout.isEmpty() || strategy == null || target == null
			|| levels == null) return 0;

		GearScorer.BankEquipment weapon = equipmentFor(
			loadout.get(EquipmentInventorySlot.WEAPON), equipmentByCanonicalId);
		String weaponName = weapon == null ? "" : weapon.name;
		String normalizedWeaponName = NameMatcher.normalize(weaponName);
		boolean eclipseAtlatl = normalizedWeaponName.contains("eclipse atlatl");

		int accuracyBonus = 0;
		int strengthBonus = 0;
		double magicDamagePercent = 0;
		String headgearName = "";
		int speed = 4;
		for (GearRecommendation recommendation : loadout.values())
		{
			GearScorer.BankEquipment item = equipmentByCanonicalId.get(recommendation.getCanonicalItemId());
			if (item == null || item.stats == null) continue;
			ItemEquipmentStats stats = item.stats;
			if (recommendation.getSlot() == EquipmentInventorySlot.HEAD)
			{
				headgearName = item.name;
			}
			switch (strategy.getCombatStyle())
			{
				case MAGIC:
					accuracyBonus += stats.getAmagic();
					magicDamagePercent += GearScorer.effectiveMagicDamageBonus(
						strategy, NameMatcher.normalize(item.name), stats);
					break;
				case RANGED:
					accuracyBonus += stats.getArange();
					// The atlatl uses the entire loadout's Melee Strength bonus,
					// not merely the weapon's own bonus.
					strengthBonus += eclipseAtlatl ? stats.getStr() : stats.getRstr();
					break;
				default:
					accuracyBonus += GearScorer.attackBonus(strategy.getAttackType(), stats);
					strengthBonus += stats.getStr();
					break;
			}
			if (recommendation.getSlot() == EquipmentInventorySlot.WEAPON)
			{
				if (stats.getAspeed() > 0) speed = stats.getAspeed();
				if (strategy.getCombatStyle() == CombatStyle.RANGED)
				{
					strengthBonus += WeaponCombatRules.intrinsicRangedStrength(item.name);
				}
			}
		}
		double taskAccuracyMultiplier = SlayerTaskHeadgear.accuracyMultiplier(
			headgearName, strategy.getCombatStyle());
		double taskDamageMultiplier = SlayerTaskHeadgear.damageMultiplier(
			headgearName, strategy.getCombatStyle());
		LoadoutCombatEffects loadoutEffects = LoadoutCombatEffects.resolve(
			loadout, strategy, weaponName);

		int accuracyLevel;
		double averageHit;
		int maximumHit;
		boolean elementalWeaknessApplies = false;
		switch (strategy.getCombatStyle())
		{
			case MAGIC:
				MagicCombatMethod magicMethod = MagicCombatMethod.resolve(
					strategy, weaponName, levels.getBoostedMagic());
				if (magicMethod == null) return 0;
				elementalWeaknessApplies = magicMethod.isElemental();
				accuracyLevel = levels.getMagicAttack();
				if (normalizedWeaponName.contains("tumeken")
					&& normalizedWeaponName.contains("shadow"))
				{
					accuracyBonus *= 3;
					magicDamagePercent = Math.min(100, magicDamagePercent * 3);
				}
				maximumHit = magicMaxHit(magicMethod.getBaseMaxHit(), magicDamagePercent,
					levels.getMagicDamagePrayerPercent(), elementalWeaknessApplies
						? strategy.getElementalWeaknessPercent() : 0);
				averageHit = maximumHit / 2.0;
				break;
			case RANGED:
				accuracyLevel = levels.getRangedAttack();
				maximumHit = maxHit((int) Math.floor((eclipseAtlatl
					? levels.getAtlatlStrength() : levels.getRangedStrength())
					* loadoutEffects.getEffectiveStrength()),
					strengthBonus);
				averageHit = maximumHit / 2.0;
				break;
			default:
				accuracyLevel = levels.getAttack();
				maximumHit = maxHit((int) Math.floor(levels.getStrength()
					* loadoutEffects.getEffectiveStrength()), strengthBonus);
				averageHit = maximumHit / 2.0;
				break;
		}
		maximumHit = (int) Math.floor(maximumHit * taskDamageMultiplier);
		maximumHit = (int) Math.floor(maximumHit * loadoutEffects.getFinalDamage());
		averageHit = maximumHit / 2.0;

		double attackRoll = Math.floor(accuracyLevel
			* loadoutEffects.getEffectiveAccuracy()) * (accuracyBonus + 64.0);
		attackRoll = Math.floor(attackRoll * taskAccuracyMultiplier);
		attackRoll = Math.floor(attackRoll * loadoutEffects.getFinalAccuracy());
		// Target-specific accuracy effects modify the attack roll before the
		// piecewise hit-chance comparison; they do not multiply hit chance.
		attackRoll = Math.floor(attackRoll
			* WeaponCombatRules.accuracyMultiplier(strategy, weaponName));
		if (elementalWeaknessApplies
			&& strategy.getElementalWeaknessPercent() > 0)
		{
			attackRoll = Math.floor(attackRoll
				* (1.0 + strategy.getElementalWeaknessPercent() / 100.0));
		}
		int defenceRoll = target.defenceRoll(strategy, weaponName);
		double accuracy = normalHitChance(attackRoll, defenceRoll);
		double damageMultiplier = WeaponCombatRules.damageMultiplier(strategy, weaponName)
			* WeaponCombatRules.intrinsicDamageMultiplier(strategy, weaponName);
		if (maximumHit > 0)
		{
			// Successful zero-damage rolls are converted to one damage in OSRS.
			averageHit += 1.0 / (maximumHit + 1.0);
		}
		return accuracy * averageHit * damageMultiplier / (Math.max(1, speed) * 0.6);
	}

	static int magicMaxHit(int baseMaxHit, double equipmentDamagePercent,
		int prayerDamagePercent, int elementalWeaknessPercent)
	{
		int primary = (int) Math.floor(baseMaxHit
			* (1.0 + Math.max(0, equipmentDamagePercent) / 100.0
				+ Math.max(0, prayerDamagePercent) / 100.0));
		return primary + (int) Math.floor(baseMaxHit
			* Math.max(0, elementalWeaknessPercent) / 100.0);
	}

	private static GearScorer.BankEquipment equipmentFor(
		GearRecommendation recommendation,
		Map<Integer, GearScorer.BankEquipment> equipmentByCanonicalId)
	{
		return recommendation == null ? null
			: equipmentByCanonicalId.get(recommendation.getCanonicalItemId());
	}

	static double normalHitChance(double attackRoll, double defenceRoll)
	{
		if (attackRoll <= 0) return 0;
		if (attackRoll > defenceRoll)
		{
			return 1.0 - (defenceRoll + 2.0) / (2.0 * (attackRoll + 1.0));
		}
		return attackRoll / (2.0 * (defenceRoll + 1.0));
	}

	private static int maxHit(int effectiveLevel, int strengthBonus)
	{
		return Math.max(0, (int) Math.floor(
			0.5 + Math.max(1, effectiveLevel) * (strengthBonus + 64.0) / 640.0));
	}
}
