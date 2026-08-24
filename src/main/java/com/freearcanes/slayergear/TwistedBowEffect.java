package com.freearcanes.slayergear;

/** Outside-Chambers Twisted bow target-Magic scaling. */
final class TwistedBowEffect
{
	private TwistedBowEffect() {}

	static double accuracyMultiplier(TargetDefence target)
	{
		return percentage(target, true) / 100.0;
	}

	static double damageMultiplier(TargetDefence target)
	{
		return percentage(target, false) / 100.0;
	}

	static int targetMagic(TargetDefence target)
	{
		return Math.min(250, Math.max(target.getMagicLevel(), target.getMagicAttack()));
	}

	private static double percentage(TargetDefence target, boolean accuracy)
	{
		double magic = targetMagic(target);
		double result = accuracy
			? 140.0 + (3.0 * magic - 10.0) / 100.0
				- Math.pow(3.0 * magic / 10.0 - 100.0, 2) / 100.0
			: 250.0 + (3.0 * magic - 14.0) / 100.0
				- Math.pow(3.0 * magic / 10.0 - 140.0, 2) / 100.0;
		return Math.max(0, Math.min(accuracy ? 140 : 250, result));
	}
}
