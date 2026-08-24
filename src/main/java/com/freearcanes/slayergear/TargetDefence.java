package com.freearcanes.slayergear;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	private final int hitpoints;
	private final int magicAttack;
	private final int size;
	private final Set<String> attributes;
	private final String burnResponse;

	TargetDefence(String name, int defenceLevel, int magicLevel,
		int stab, int slash, int crush, int magic, int light, int standard, int heavy)
	{
		this(name, defenceLevel, magicLevel, stab, slash, crush, magic, light,
			standard, heavy, 0, 0, 1, "", "false");
	}

	TargetDefence(String name, int defenceLevel, int magicLevel,
		int stab, int slash, int crush, int magic, int light, int standard, int heavy,
		int hitpoints, int magicAttack, int size, String attributes, String burnResponse)
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
		this.hitpoints = Math.max(0, hitpoints);
		this.magicAttack = magicAttack;
		this.size = Math.max(1, size);
		Set<String> parsedAttributes = new HashSet<>();
		if (attributes != null)
		{
			for (String attribute : attributes.split(","))
			{
				String normalized = NameMatcher.normalize(attribute);
				if (!normalized.isEmpty()) parsedAttributes.add(normalized);
			}
		}
		this.attributes = Collections.unmodifiableSet(parsedAttributes);
		this.burnResponse = burnResponse == null ? "false" : burnResponse;
	}

	String getName() { return name; }
	int getHitpoints() { return hitpoints; }
	int getMagicLevel() { return magicLevel; }
	int getMagicAttack() { return magicAttack; }
	int getSize() { return size; }
	boolean hasAttribute(String attribute) { return attributes.contains(NameMatcher.normalize(attribute)); }
	String getBurnResponse() { return burnResponse; }
	String signature()
	{
		return defenceLevel + ":" + magicLevel + ":" + stab + ":" + slash + ":"
			+ crush + ":" + magic + ":" + light + ":" + standard + ":" + heavy
			+ ":" + hitpoints + ":" + magicAttack + ":" + size + ":" + attributes
			+ ":" + burnResponse;
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
