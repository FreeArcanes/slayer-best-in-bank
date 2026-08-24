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
	private final int boostedMagic;
	private final int magicDamagePrayerPercent;

	private CombatLevelContext(int attack, int strength, int rangedAttack,
		int rangedStrength, int atlatlStrength, int magicAttack,
		int boostedMagic, int magicDamagePrayerPercent)
	{
		this.attack = positive(attack);
		this.strength = positive(strength);
		this.rangedAttack = positive(rangedAttack);
		this.rangedStrength = positive(rangedStrength);
		this.atlatlStrength = positive(atlatlStrength);
		this.magicAttack = positive(magicAttack);
		this.boostedMagic = positive(boostedMagic);
		this.magicDamagePrayerPercent = Math.max(0, magicDamagePrayerPercent);
	}

	static CombatLevelContext effective(int boostedAttack, double attackPrayer,
		int boostedStrength, double strengthPrayer, int boostedRanged,
		double rangedAttackPrayer, double rangedStrengthPrayer,
		int boostedMagic, double magicPrayer, int magicDamagePrayerPercent)
	{
		return new CombatLevelContext(
			effectiveLevel(boostedAttack, attackPrayer),
			effectiveLevel(boostedStrength, strengthPrayer),
			effectiveLevel(boostedRanged, rangedAttackPrayer),
			effectiveLevel(boostedRanged, rangedStrengthPrayer),
			effectiveLevel(boostedStrength, rangedStrengthPrayer),
			effectiveMagicLevel(boostedMagic, magicPrayer), boostedMagic,
			magicDamagePrayerPercent);
	}

	static CombatLevelContext unboosted(int attack, int strength, int magic, int ranged)
	{
		return effective(attack, 1, strength, 1, ranged, 1, 1, magic, 1, 0);
	}

	private static int effectiveMagicLevel(int boostedLevel, double prayerMultiplier)
	{
		return (int) Math.floor(Math.max(1, boostedLevel) * Math.max(1, prayerMultiplier)) + 9;
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
	int getBoostedMagic() { return boostedMagic; }
	int getMagicDamagePrayerPercent() { return magicDamagePrayerPercent; }
}
