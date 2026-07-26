package com.freearcanes.slayergear;

enum ElementalWeakness
{
	NONE,
	AIR,
	WATER,
	EARTH,
	FIRE;

	String displayName()
	{
		if (this == NONE) return "";
		String lower = name().toLowerCase(java.util.Locale.ENGLISH);
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}
}