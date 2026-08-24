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
		int attackLevel,
		int strengthLevel,
		int magicLevel,
		int rangedLevel)
	{
		if (loadout == null || loadout.isEmpty() || strategy == null || target == null
			|| strategy.getCombatStyle() == CombatStyle.MAGIC) return 0;

		GearScorer.BankEquipment weapon = equipmentFor(
			loadout.get(EquipmentInventorySlot.WEAPON), equipmentByCanonicalId);
		String weaponName = weapon == null ? "" : weapon.name;
		String normalizedWeaponName = NameMatcher.normalize(weaponName);
		boolean eclipseAtlatl = normalizedWeaponName.contains("eclipse atlatl");

		int accuracyBonus = 0;
		int strengthBonus = 0;
		int speed = 4;
		for (GearRecommendation recommendation : loadout.values())
		{
			GearScorer.BankEquipment item = equipmentByCanonicalId.get(recommendation.getCanonicalItemId());
			if (item == null || item.stats == null) continue;
			ItemEquipmentStats stats = item.stats;
			switch (strategy.getCombatStyle())
			{
				case MAGIC:
					accuracyBonus += stats.getAmagic();
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

		int accuracyLevel;
		double averageHit;
		switch (strategy.getCombatStyle())
		{
			case MAGIC: return 0;
			case RANGED:
				accuracyLevel = Math.max(1, rangedLevel);
				averageHit = maxHit(eclipseAtlatl ? strengthLevel : rangedLevel,
					strengthBonus) / 2.0;
				break;
			default:
				accuracyLevel = Math.max(1, attackLevel);
				averageHit = maxHit(strengthLevel, strengthBonus) / 2.0;
				break;
		}

		double attackRoll = (accuracyLevel + 8.0) * (accuracyBonus + 64.0);
		// Target-specific accuracy effects modify the attack roll before the
		// piecewise hit-chance comparison; they do not multiply hit chance.
		attackRoll = Math.floor(attackRoll
			* WeaponCombatRules.accuracyMultiplier(strategy, weaponName));
		int defenceRoll = target.defenceRoll(strategy, weaponName);
		double accuracy = normalHitChance(attackRoll, defenceRoll);
		double damageMultiplier = WeaponCombatRules.damageMultiplier(strategy, weaponName)
			* WeaponCombatRules.intrinsicDamageMultiplier(strategy, weaponName);
		int maximumHit = (int) Math.round(averageHit * 2.0);
		if (maximumHit > 0)
		{
			// Successful zero-damage rolls are converted to one damage in OSRS.
			averageHit += 1.0 / (maximumHit + 1.0);
		}
		return accuracy * averageHit * damageMultiplier / (Math.max(1, speed) * 0.6);
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

	private static int maxHit(int level, int strengthBonus)
	{
		return Math.max(0, (int) Math.floor(
			0.5 + (Math.max(1, level) + 8.0) * (strengthBonus + 64.0) / 640.0));
	}
}
