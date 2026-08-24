package com.freearcanes.slayergear;

import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;

/** Combat modifiers which only exist when multiple compatible items are worn. */
final class LoadoutCombatEffects
{
	private final double effectiveAccuracy;
	private final double effectiveStrength;
	private final double finalAccuracy;
	private final double finalDamage;

	private LoadoutCombatEffects(double effectiveAccuracy, double effectiveStrength,
		double finalAccuracy, double finalDamage)
	{
		this.effectiveAccuracy = effectiveAccuracy;
		this.effectiveStrength = effectiveStrength;
		this.finalAccuracy = finalAccuracy;
		this.finalDamage = finalDamage;
	}

	static LoadoutCombatEffects resolve(
		Map<EquipmentInventorySlot, GearRecommendation> loadout,
		GearStrategy strategy,
		String weaponName)
	{
		double effectiveAccuracy = 1.0;
		double effectiveStrength = 1.0;
		double finalAccuracy = 1.0;
		double finalDamage = 1.0;

		boolean eliteVoid = has(loadout, EquipmentInventorySlot.BODY, "elite void top")
			&& has(loadout, EquipmentInventorySlot.LEGS, "elite void robe");
		boolean voidBase = has(loadout, EquipmentInventorySlot.GLOVES, "void knight gloves")
			&& hasAny(loadout, EquipmentInventorySlot.BODY, "void knight top", "elite void top")
			&& hasAny(loadout, EquipmentInventorySlot.LEGS, "void knight robe", "elite void robe");
		if (voidBase && voidHelmMatches(loadout, strategy.getCombatStyle()))
		{
			switch (strategy.getCombatStyle())
			{
				case MAGIC:
					effectiveAccuracy = 1.45;
					if (eliteVoid) finalDamage = 1.05;
					break;
				case RANGED:
					effectiveAccuracy = 1.10;
					effectiveStrength = eliteVoid ? 1.125 : 1.10;
					break;
				default:
					effectiveAccuracy = 1.10;
					effectiveStrength = 1.10;
					break;
			}
		}

		String weapon = NameMatcher.normalize(weaponName);
		if (strategy.getCombatStyle() == CombatStyle.RANGED
			&& (weapon.contains("crystal bow") || weapon.contains("bow of faerdhinen")))
		{
			double accuracyPercent = 0;
			double damagePercent = 0;
			if (has(loadout, EquipmentInventorySlot.HEAD, "crystal helm"))
			{
				accuracyPercent += 5; damagePercent += 2.5;
			}
			if (has(loadout, EquipmentInventorySlot.BODY, "crystal body"))
			{
				accuracyPercent += 15; damagePercent += 7.5;
			}
			if (has(loadout, EquipmentInventorySlot.LEGS, "crystal legs"))
			{
				accuracyPercent += 10; damagePercent += 5;
			}
			finalAccuracy *= 1.0 + accuracyPercent / 100.0;
			finalDamage *= 1.0 + damagePercent / 100.0;
		}

		if (strategy.getCombatStyle() == CombatStyle.MELEE
			&& strategy.getAttackType() == AttackType.CRUSH)
		{
			int pieces = inquisitorPieces(loadout);
			if (pieces > 0)
			{
				boolean inquisitorMace = weapon.contains("inquisitor's mace");
				double perPiece = inquisitorMace ? 0.025 : 0.005;
				// The mace enhancement replaces the ordinary per-piece/full-set
				// structure: three mace-boosted pieces total 7.5%, not 8.5%.
				double bonus = pieces * perPiece
					+ (pieces == 3 && !inquisitorMace ? 0.01 : 0);
				finalAccuracy *= 1.0 + bonus;
				finalDamage *= 1.0 + bonus;
			}
		}
		return new LoadoutCombatEffects(effectiveAccuracy, effectiveStrength,
			finalAccuracy, finalDamage);
	}

	private static boolean voidHelmMatches(
		Map<EquipmentInventorySlot, GearRecommendation> loadout, CombatStyle style)
	{
		String token = style == CombatStyle.MAGIC ? "void mage helm"
			: style == CombatStyle.RANGED ? "void ranger helm" : "void melee helm";
		return has(loadout, EquipmentInventorySlot.HEAD, token);
	}

	private static int inquisitorPieces(Map<EquipmentInventorySlot, GearRecommendation> loadout)
	{
		int result = 0;
		if (has(loadout, EquipmentInventorySlot.HEAD, "inquisitor's great helm")) result++;
		if (has(loadout, EquipmentInventorySlot.BODY, "inquisitor's hauberk")) result++;
		if (has(loadout, EquipmentInventorySlot.LEGS, "inquisitor's plateskirt")) result++;
		return result;
	}

	private static boolean hasAny(Map<EquipmentInventorySlot, GearRecommendation> loadout,
		EquipmentInventorySlot slot, String... tokens)
	{
		for (String token : tokens) if (has(loadout, slot, token)) return true;
		return false;
	}

	private static boolean has(Map<EquipmentInventorySlot, GearRecommendation> loadout,
		EquipmentInventorySlot slot, String token)
	{
		GearRecommendation item = loadout.get(slot);
		return item != null && NameMatcher.normalize(item.getItemName()).contains(token);
	}

	double getEffectiveAccuracy() { return effectiveAccuracy; }
	double getEffectiveStrength() { return effectiveStrength; }
	double getFinalAccuracy() { return finalAccuracy; }
	double getFinalDamage() { return finalDamage; }
}
