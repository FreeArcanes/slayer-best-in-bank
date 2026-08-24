package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CombatLevelContextTest
{
	@Test
	public void appliesBoostsAndPrayerMultipliersBeforeAddingEight()
	{
		CombatLevelContext context = CombatLevelContext.effective(
			118, 1.20, 118, 1.23, 112, 1.20, 1.23, 108, 1.25, 4);

		assertEquals(149, context.getAttack());
		assertEquals(153, context.getStrength());
		assertEquals(142, context.getRangedAttack());
		assertEquals(145, context.getRangedStrength());
		assertEquals(153, context.getAtlatlStrength());
		assertEquals(144, context.getMagicAttack());
		assertEquals(108, context.getBoostedMagic());
		assertEquals(4, context.getMagicDamagePrayerPercent());
	}

	@Test
	public void atlatlUsesRangedPrayerOnVisibleStrengthLevel()
	{
		CombatLevelContext context = CombatLevelContext.effective(
			80, 1, 90, 1, 80, 1.20, 1.23, 80, 1, 0);

		assertEquals(98, context.getStrength());
		assertEquals(118, context.getAtlatlStrength());
	}
}
