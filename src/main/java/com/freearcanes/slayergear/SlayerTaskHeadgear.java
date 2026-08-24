package com.freearcanes.slayergear;

/** On-task accuracy and damage effects supplied by Black masks and Slayer helmets. */
final class SlayerTaskHeadgear
{
	private static final double MELEE_MULTIPLIER = 7.0 / 6.0;
	private static final double IMBUED_MULTIPLIER = 1.15;

	private SlayerTaskHeadgear() {}

	static double accuracyMultiplier(String itemName, CombatStyle style)
	{
		if (!isMaskOrSlayerHelmet(itemName)) return 1.0;
		if (style == CombatStyle.MELEE) return MELEE_MULTIPLIER;
		return isImbued(itemName) ? IMBUED_MULTIPLIER : 1.0;
	}

	static double damageMultiplier(String itemName, CombatStyle style)
	{
		return accuracyMultiplier(itemName, style);
	}

	private static boolean isMaskOrSlayerHelmet(String itemName)
	{
		String name = NameMatcher.normalize(itemName);
		return name.contains("black mask")
			|| name.contains("slayer helmet")
			|| name.contains("slayer helm");
	}

	private static boolean isImbued(String itemName)
	{
		return NameMatcher.normalize(itemName).contains("(i)");
	}
}
