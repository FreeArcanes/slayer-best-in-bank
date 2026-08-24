package com.freearcanes.slayergear;

import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoadoutCombatEffectsTest
{
	@Test
	public void completeVoidSetsUseStyleAndEliteSpecificEffects()
	{
		Map<EquipmentInventorySlot, GearRecommendation> ranged = voidSet(
			"Void ranger helm (or)", "Elite void top (or)", "Elite void robe (or)");
		LoadoutCombatEffects rangedEffects = LoadoutCombatEffects.resolve(ranged,
			GearStrategy.builder().combatStyle(CombatStyle.RANGED).build(), "Magic shortbow (i)");
		assertEquals(1.10, rangedEffects.getEffectiveAccuracy(), 0.000001);
		assertEquals(1.125, rangedEffects.getEffectiveStrength(), 0.000001);

		Map<EquipmentInventorySlot, GearRecommendation> magic = voidSet(
			"Void mage helm", "Elite void top", "Elite void robe");
		LoadoutCombatEffects magicEffects = LoadoutCombatEffects.resolve(magic,
			GearStrategy.builder().combatStyle(CombatStyle.MAGIC).build(), "Trident of the swamp");
		assertEquals(1.45, magicEffects.getEffectiveAccuracy(), 0.000001);
		assertEquals(1.05, magicEffects.getFinalDamage(), 0.000001);
	}

	@Test
	public void incompleteOrWrongStyleVoidGetsNoEffect()
	{
		Map<EquipmentInventorySlot, GearRecommendation> wrong = voidSet(
			"Void melee helm", "Void knight top", "Void knight robe");
		wrong.remove(EquipmentInventorySlot.GLOVES);
		LoadoutCombatEffects effects = LoadoutCombatEffects.resolve(wrong,
			GearStrategy.builder().combatStyle(CombatStyle.RANGED).build(), "Yew shortbow");
		assertEquals(1.0, effects.getEffectiveAccuracy(), 0.000001);
		assertEquals(1.0, effects.getEffectiveStrength(), 0.000001);
	}

	@Test
	public void crystalPiecesApplyOnlyToCompatibleCrystalBows()
	{
		Map<EquipmentInventorySlot, GearRecommendation> loadout = new EnumMap<>(EquipmentInventorySlot.class);
		loadout.put(EquipmentInventorySlot.HEAD, item(1, "Crystal helm (inactive)", EquipmentInventorySlot.HEAD));
		loadout.put(EquipmentInventorySlot.BODY, item(2, "Crystal body (c)", EquipmentInventorySlot.BODY));
		loadout.put(EquipmentInventorySlot.LEGS, item(3, "Crystal legs", EquipmentInventorySlot.LEGS));
		GearStrategy ranged = GearStrategy.builder().combatStyle(CombatStyle.RANGED).build();

		LoadoutCombatEffects bowfa = LoadoutCombatEffects.resolve(
			loadout, ranged, "Bow of faerdhinen (c)");
		assertEquals(1.30, bowfa.getFinalAccuracy(), 0.000001);
		assertEquals(1.15, bowfa.getFinalDamage(), 0.000001);
		assertEquals(1.0, LoadoutCombatEffects.resolve(
			loadout, ranged, "Twisted bow").getFinalDamage(), 0.000001);
	}

	@Test
	public void inquisitorCountsPiecesCrushStyleAndMaceEnhancement()
	{
		Map<EquipmentInventorySlot, GearRecommendation> loadout = new EnumMap<>(EquipmentInventorySlot.class);
		loadout.put(EquipmentInventorySlot.HEAD, item(1, "Inquisitor's great helm", EquipmentInventorySlot.HEAD));
		loadout.put(EquipmentInventorySlot.BODY, item(2, "Inquisitor's hauberk", EquipmentInventorySlot.BODY));
		loadout.put(EquipmentInventorySlot.LEGS, item(3, "Inquisitor's plateskirt", EquipmentInventorySlot.LEGS));
		GearStrategy crush = GearStrategy.builder().combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.CRUSH).build();

		assertEquals(1.025, LoadoutCombatEffects.resolve(
			loadout, crush, "Scythe of vitur").getFinalDamage(), 0.000001);
		assertEquals(1.075, LoadoutCombatEffects.resolve(
			loadout, crush, "Inquisitor's mace").getFinalDamage(), 0.000001);
		GearStrategy slash = GearStrategy.builder().combatStyle(CombatStyle.MELEE)
			.attackType(AttackType.SLASH).build();
		assertEquals(1.0, LoadoutCombatEffects.resolve(
			loadout, slash, "Inquisitor's mace").getFinalDamage(), 0.000001);
	}

	private static Map<EquipmentInventorySlot, GearRecommendation> voidSet(
		String helm, String body, String legs)
	{
		Map<EquipmentInventorySlot, GearRecommendation> result = new EnumMap<>(EquipmentInventorySlot.class);
		result.put(EquipmentInventorySlot.HEAD, item(1, helm, EquipmentInventorySlot.HEAD));
		result.put(EquipmentInventorySlot.BODY, item(2, body, EquipmentInventorySlot.BODY));
		result.put(EquipmentInventorySlot.LEGS, item(3, legs, EquipmentInventorySlot.LEGS));
		result.put(EquipmentInventorySlot.GLOVES,
			item(4, "Void knight gloves (or)", EquipmentInventorySlot.GLOVES));
		return result;
	}

	private static GearRecommendation item(int id, String name, EquipmentInventorySlot slot)
	{
		return GearRecommendation.builder().itemId(id).canonicalItemId(id)
			.itemName(name).slot(slot).build();
	}
}
