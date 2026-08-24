package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EquipmentChargePolicyTest
{
	@Test
	public void rejectsExplicitlyUnusableChargeStates()
	{
		assertFalse(EquipmentChargePolicy.isUsable("Scythe of vitur (uncharged)"));
		assertFalse(EquipmentChargePolicy.isUsable("Webweaver bow (u)"));
		assertFalse(EquipmentChargePolicy.isUsable("Crystal body (inactive)"));
		assertFalse(EquipmentChargePolicy.isUsable("Dharok's platebody 0"));
		assertFalse(EquipmentChargePolicy.isUsable("Some weapon (broken)"));
		assertFalse(EquipmentChargePolicy.isUsable("Toxic blowpipe (empty)"));
		assertFalse(EquipmentChargePolicy.isUsable("Uncharged trident"));
		assertFalse(EquipmentChargePolicy.isUsable("Torva platebody (damaged)"));
		assertFalse(EquipmentChargePolicy.isUsable("Venator bow (uncharged)"));
	}

	@Test
	public void keepsFunctionalDegradableVariantsAndAddsChargeReminder()
	{
		assertTrue(EquipmentChargePolicy.isUsable("Dharok's platebody 25"));
		assertTrue(EquipmentChargePolicy.isUsable("Scythe of vitur"));
		assertTrue(EquipmentChargePolicy.isUsable("Dragonfire shield (uncharged)"));
		assertTrue(EquipmentChargePolicy.note("Scythe of vitur").contains("verify charges"));
		assertTrue(EquipmentChargePolicy.note("Crystal body").contains("charge-dependent"));
		assertTrue(EquipmentChargePolicy.note("Bow of faerdhinen (c)").isEmpty());
		assertTrue(EquipmentChargePolicy.note("Abyssal whip").isEmpty());
	}
}
