package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangedAmmoPolicyTest
{
	@Test
	public void specialCrossbowsOnlyAcceptTheirOwnAmmunition()
	{
		compatible("Hunters' sunlight crossbow", "Moonlight antler bolts");
		incompatible("Hunters' sunlight crossbow", "Diamond bolts (e)");
		compatible("Hunters' crossbow", "Long kebbit bolts");
		incompatible("Hunters' crossbow", "Rune bolts");
		compatible("Karil's crossbow", "Bolt rack");
		incompatible("Karil's crossbow", "Diamond bolts (e)");
		compatible("Dorgeshuun crossbow", "Bone bolts");
		compatible("Dorgeshuun crossbow", "Iron bolts");
		incompatible("Dorgeshuun crossbow", "Opal bolts (e)");
		incompatible("Dorgeshuun crossbow", "Mithril bolts");
	}

	@Test
	public void standardCrossbowsEnforceMetalAndGemTiers()
	{
		compatible("Bronze crossbow", "Opal bolts (e)");
		incompatible("Bronze crossbow", "Iron bolts");
		compatible("Adamant crossbow", "Diamond bolts (e)");
		incompatible("Adamant crossbow", "Runite bolts");
		compatible("Rune crossbow", "Onyx bolts (e)");
		compatible("Rune crossbow", "Amethyst broad bolts");
		incompatible("Rune crossbow", "Diamond dragon bolts (e)");
		compatible("Dragon hunter crossbow", "Diamond dragon bolts (e)");
	}

	@Test
	public void bowsEnforceArrowTiersAndSpecialAmmunition()
	{
		compatible("Shortbow", "Iron arrow");
		incompatible("Shortbow", "Steel arrow");
		compatible("Willow shortbow", "Mithril fire arrow");
		incompatible("Willow shortbow", "Adamant arrow");
		compatible("Bone shortbow", "Rune arrow");
		compatible("Bone shortbow", "Broad arrows");
		incompatible("Bone shortbow", "Amethyst arrow");
		incompatible("Willow shortbow", "Broad arrows");
		compatible("Magic shortbow (i)", "Amethyst arrow");
		incompatible("Magic shortbow (i)", "Dragon arrow");
		compatible("Twisted bow", "Dragon arrow");
		compatible("Training bow", "Training arrows");
		incompatible("Training bow", "Bronze arrow");
		compatible("Ogre bow", "Mithril brutal");
		incompatible("Ogre bow", "Adamant brutal");
		compatible("Comp ogre bow", "Rune brutal");
		incompatible("Comp ogre bow", "Rune arrow");
	}

	@Test
	public void weaponFamiliesUseTheirExactAmmoOrInternalCharges()
	{
		compatible("Eclipse atlatl", "Atlatl dart");
		incompatible("Eclipse atlatl", "Dragon dart");
		compatible("Heavy ballista", "Dragon javelin");
		compatible("Swamp lizard", "Guam tar");
		compatible("Orange salamander", "Marrentill tar");
		compatible("Red salamander", "Tarromin tar");
		compatible("Black salamander", "Harralander tar");
		compatible("Tecu salamander", "Irit tar");
		incompatible("Black salamander", "Irit tar");

		assertFalse(RangedAmmoPolicy.usesAmmoSlot("Webweaver bow"));
		assertFalse(RangedAmmoPolicy.usesAmmoSlot("Craw's bow"));
		assertFalse(RangedAmmoPolicy.usesAmmoSlot("Tonalztics of ralos"));
		assertFalse(RangedAmmoPolicy.usesAmmoSlot("Toxic blowpipe"));
		assertTrue(RangedAmmoPolicy.usesAmmoSlot("Venator bow"));
	}

	@Test
	public void visibleEmptyInactiveAndUnchargedWeaponsAreRejected()
	{
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Toxic blowpipe (empty)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Camphor blowpipe (empty)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Ironwood blowpipe (empty)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Rosewood blowpipe (empty)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Webweaver bow (u)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Craw's bow (u)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Venator bow (uncharged)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Tonalztics of ralos (uncharged)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Crystal bow (inactive)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Bow of faerdhinen (inactive)"));
		assertFalse(RangedAmmoPolicy.isUsableWeapon("Morrigan's javelin (bh)(inactive)"));
		assertTrue(RangedAmmoPolicy.isUsableWeapon("Webweaver bow"));
	}

	private static void compatible(String weapon, String ammo)
	{
		assertTrue(weapon + " should accept " + ammo, RangedAmmoPolicy.isCompatible(weapon, ammo));
	}

	private static void incompatible(String weapon, String ammo)
	{
		assertFalse(weapon + " should reject " + ammo, RangedAmmoPolicy.isCompatible(weapon, ammo));
	}
}
