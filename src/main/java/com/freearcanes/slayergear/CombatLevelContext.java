package com.freearcanes.slayergear;

/** Effective offensive levels after visible boosts and active prayers. */
final class CombatLevelContext
{
	private final int attack;
	private final int strength;
	private final int rangedAttack;
	private final int rangedStrength;
	private final int atlatlStrength;
	private final int magicAttack;

	private CombatLevelContext(int attack, int strength, int rangedAttack,
		int rangedStrength, int atlatlStrength, int magicAttack)
	{
		this.attack = positive(attack);
		this.strength = positive(strength);
		this.rangedAttack = positive(rangedAttack);
		this.rangedStrength = positive(rangedStrength);
		this.atlatlStrength = positive(atlatlStrength);
		this.magicAttack = positive(magicAttack);
	}

	static CombatLevelContext effective(int boostedAttack, double attackPrayer,
		int boostedStrength, double strengthPrayer, int boostedRanged,
		double rangedAttackPrayer, double rangedStrengthPrayer,
		int boostedMagic, double magicPrayer)
	{
		return new CombatLevelContext(
			effectiveLevel(boostedAttack, attackPrayer),
			effectiveLevel(boostedStrength, strengthPrayer),
			effectiveLevel(boostedRanged, rangedAttackPrayer),
			effectiveLevel(boostedRanged, rangedStrengthPrayer),
			effectiveLevel(boostedStrength, rangedStrengthPrayer),
			effectiveLevel(boostedMagic, magicPrayer));
	}

	static CombatLevelContext unboosted(int attack, int strength, int magic, int ranged)
	{
		return effective(attack, 1, strength, 1, ranged, 1, 1, magic, 1);
	}

	private static int effectiveLevel(int boostedLevel, double prayerMultiplier)
	{
		return (int) Math.floor(Math.max(1, boostedLevel) * Math.max(1, prayerMultiplier)) + 8;
	}

	private static int positive(int value) { return Math.max(1, value); }
	int getAttack() { return attack; }
	int getStrength() { return strength; }
	int getRangedAttack() { return rangedAttack; }
	int getRangedStrength() { return rangedStrength; }
	int getAtlatlStrength() { return atlatlStrength; }
	int getMagicAttack() { return magicAttack; }
}
