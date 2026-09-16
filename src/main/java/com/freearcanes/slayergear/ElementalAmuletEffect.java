package com.freearcanes.slayergear;

import net.runelite.api.EquipmentInventorySlot;

/** Models the hidden flat max-hit effect of the elemental amulets. */
final class ElementalAmuletEffect
{
	static final int MAX_HIT_BONUS = 2;
	private static final double HIGHEST_STANDARD_SPELL_BASE_MAX_HIT = 24.0;

	private ElementalAmuletEffect() {}

	static boolean applies(GearStrategy strategy, String itemName)
	{
		if (strategy == null
			|| strategy.getCombatStyle() != CombatStyle.MAGIC
			|| strategy.getElementalWeakness() == ElementalWeakness.NONE)
		{
			return false;
		}

		String name = NameMatcher.normalize(itemName);
		if (name.contains("elemental amulet")) return true;

		switch (strategy.getElementalWeakness())
		{
			case AIR: return name.contains("amulet of air");
			case WATER: return name.contains("amulet of water");
			case EARTH: return name.contains("amulet of earth");
			case FIRE: return name.contains("amulet of fire");
			default: return false;
		}
	}

	static double scoringBonus(
		GearStrategy strategy,
		String itemName,
		EquipmentInventorySlot slot)
	{
		if (slot != EquipmentInventorySlot.AMULET || !applies(strategy, itemName)) return 0;

		// scoreStats values each visible Magic-damage percentage point at 25.
		// A flat +2 is at least an 8.33% increase across standard spells, using
		// the highest base max hit (24) as the conservative comparison point.
		return MAX_HIT_BONUS * 100.0 / HIGHEST_STANDARD_SPELL_BASE_MAX_HIT * 25.0;
	}
}
