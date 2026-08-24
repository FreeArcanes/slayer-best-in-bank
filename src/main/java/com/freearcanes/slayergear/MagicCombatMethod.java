package com.freearcanes.slayergear;

/** A Magic attack whose base max hit can be determined without guessing. */
final class MagicCombatMethod
{
	private final String name;
	private final int baseMaxHit;
	private final boolean elemental;

	private MagicCombatMethod(String name, int baseMaxHit, boolean elemental)
	{
		this.name = name;
		this.baseMaxHit = Math.max(0, baseMaxHit);
		this.elemental = elemental;
	}

	static MagicCombatMethod resolve(GearStrategy strategy, String weaponName, int magicLevel)
	{
		String weapon = NameMatcher.normalize(weaponName);
		if (weapon.contains("warped sceptre"))
		{
			return method("Warped sceptre", (8 * magicLevel + 96) / 37);
		}
		if (weapon.contains("trident of the swamp"))
		{
			return method("Trident of the swamp", magicLevel / 3 - 2);
		}
		if (weapon.contains("trident of the seas"))
		{
			return method("Trident of the seas", magicLevel / 3 - 5);
		}
		if (weapon.contains("sanguinesti staff"))
		{
			return method("Sanguinesti staff", magicLevel / 3 + 1);
		}
		if (weapon.contains("tumeken") && weapon.contains("shadow"))
		{
			return method("Tumeken's shadow", magicLevel / 3 + 1);
		}
		if (strategy.isAncientAoe())
		{
			if (magicLevel >= 94) return method("Ice Barrage", 30);
			if (magicLevel >= 70) return method("Ice Burst", 22);
			return null;
		}
		if (strategy.getElementalWeakness() != ElementalWeakness.NONE)
		{
			return elemental(strategy.getElementalWeakness(), magicLevel);
		}
		return null;
	}

	private static MagicCombatMethod elemental(ElementalWeakness element, int level)
	{
		int surge;
		int wave;
		int blast;
		int bolt;
		int strike;
		switch (element)
		{
			case WATER: surge = 85; wave = 65; blast = 47; bolt = 23; strike = 5; break;
			case EARTH: surge = 90; wave = 70; blast = 53; bolt = 29; strike = 9; break;
			case FIRE: surge = 95; wave = 75; blast = 59; bolt = 35; strike = 13; break;
			default: surge = 81; wave = 62; blast = 41; bolt = 17; strike = 1; break;
		}
		if (level >= surge) return elementalMethod(element.displayName() + " Surge", 24);
		if (level >= wave) return elementalMethod(element.displayName() + " Wave", 20);
		if (level >= blast) return elementalMethod(element.displayName() + " Blast", 16);
		if (level >= bolt) return elementalMethod(element.displayName() + " Bolt", 12);
		if (level >= strike) return elementalMethod(element.displayName() + " Strike", 8);
		return null;
	}

	private static MagicCombatMethod method(String name, int baseMaxHit)
	{
		return baseMaxHit <= 0 ? null : new MagicCombatMethod(name, baseMaxHit, false);
	}

	private static MagicCombatMethod elementalMethod(String name, int baseMaxHit)
	{
		return baseMaxHit <= 0 ? null : new MagicCombatMethod(name, baseMaxHit, true);
	}

	String getName() { return name; }
	int getBaseMaxHit() { return baseMaxHit; }
	boolean isElemental() { return elemental; }
}
