package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class MagicCombatMethodTest
{
	@Test
	public void resolvesPoweredStaffFormulasFromVisibleMagicLevel()
	{
		GearStrategy magic = GearStrategy.builder().combatStyle(CombatStyle.MAGIC).build();
		assertEquals(24, MagicCombatMethod.resolve(magic, "Warped sceptre", 99).getBaseMaxHit());
		assertEquals(28, MagicCombatMethod.resolve(magic, "Trident of the seas", 99).getBaseMaxHit());
		assertEquals(31, MagicCombatMethod.resolve(magic, "Trident of the swamp", 99).getBaseMaxHit());
		assertEquals(34, MagicCombatMethod.resolve(magic, "Sanguinesti staff", 99).getBaseMaxHit());
		assertEquals(34, MagicCombatMethod.resolve(magic, "Tumeken's shadow", 99).getBaseMaxHit());
	}

	@Test
	public void resolvesAncientAoeAtRealUnlockThresholds()
	{
		GearStrategy ancients = GearStrategy.builder()
			.combatStyle(CombatStyle.MAGIC).ancientAoe(true).build();
		assertNull(MagicCombatMethod.resolve(ancients, "Ancient staff", 69));
		assertEquals("Ice Burst", MagicCombatMethod.resolve(
			ancients, "Ancient staff", 70).getName());
		assertEquals("Ice Barrage", MagicCombatMethod.resolve(
			ancients, "Ancient staff", 94).getName());
	}

	@Test
	public void elementalThresholdsDoNotUnlockHigherElementEarly()
	{
		GearStrategy fire = GearStrategy.builder().combatStyle(CombatStyle.MAGIC)
			.elementalWeakness(ElementalWeakness.FIRE, 50).build();
		assertEquals("Fire Wave", MagicCombatMethod.resolve(fire, "Mystic fire staff", 94).getName());
		assertEquals("Fire Surge", MagicCombatMethod.resolve(fire, "Mystic fire staff", 95).getName());
	}

	@Test
	public void magicMaxHitFloorsPrimaryAndWeaknessSeparately()
	{
		assertEquals(39, LoadoutOffenseEstimator.magicMaxHit(24, 10, 4, 50));
	}

	@Test
	public void unidentifiedGenericMagicDoesNotGuessSpell()
	{
		GearStrategy generic = GearStrategy.builder().combatStyle(CombatStyle.MAGIC).build();
		assertNull(MagicCombatMethod.resolve(generic, "Ancient staff", 99));
	}

	@Test
	public void poweredStaffIsNotMistakenForElementalSpell()
	{
		GearStrategy airWeak = GearStrategy.builder().combatStyle(CombatStyle.MAGIC)
			.elementalWeakness(ElementalWeakness.AIR, 50).build();
		assertFalse(MagicCombatMethod.resolve(
			airWeak, "Trident of the swamp", 99).isElemental());
	}
}
