package com.freearcanes.slayergear;

/** Wiki-sourced NPC levels and style-specific defensive bonuses. */
final class TargetDefence
{
	private final String name;
	private final int defenceLevel;
	private final int magicLevel;
	private final int stab;
	private final int slash;
	private final int crush;
	private final int magic;
	private final int light;
	private final int standard;
	private final int heavy;

	TargetDefence(String name, int defenceLevel, int magicLevel,
		int stab, int slash, int crush, int magic, int light, int standard, int heavy)
	{
		this.name = name;
		this.defenceLevel = defenceLevel;
		this.magicLevel = magicLevel;
		this.stab = stab;
		this.slash = slash;
		this.crush = crush;
		this.magic = magic;
		this.light = light;
		this.standard = standard;
		this.heavy = heavy;
	}

	String getName() { return name; }
	String signature()
	{
		return defenceLevel + ":" + magicLevel + ":" + stab + ":" + slash + ":"
			+ crush + ":" + magic + ":" + light + ":" + standard + ":" + heavy;
	}

	int defenceRoll(GearStrategy strategy, String weaponName)
	{
		int bonus;
		int level;
		if (strategy.getCombatStyle() == CombatStyle.MAGIC)
		{
			level = magicLevel;
			bonus = magic;
		}
		else if (strategy.getCombatStyle() == CombatStyle.RANGED)
		{
			level = defenceLevel;
			String weapon = NameMatcher.normalize(weaponName);
			if (weapon.contains("crossbow") || weapon.contains("ballista") || weapon.contains("chinchompa"))
			{
				bonus = heavy;
			}
			else if (weapon.contains("blowpipe") || weapon.contains("dart")
				|| weapon.contains("knife") || weapon.contains("thrownaxe"))
			{
				bonus = light;
			}
			else
			{
				bonus = standard;
			}
		}
		else
		{
			level = defenceLevel;
			switch (strategy.getAttackType())
			{
				case STAB: bonus = stab; break;
				case SLASH: bonus = slash; break;
				case CRUSH: bonus = crush; break;
				default: bonus = Math.min(stab, Math.min(slash, crush)); break;
			}
		}
		return (level + 9) * (bonus + 64);
	}
}
