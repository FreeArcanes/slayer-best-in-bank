package com.freearcanes.slayergear;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TripCostEstimatorTest
{
	@Test
	public void pricesWholePotionWithdrawalsAndStackedShots()
	{
		ItemManager itemManager = mock(ItemManager.class);
		when(itemManager.getItemPrice(100)).thenReturn(12_000);
		when(itemManager.getItemPrice(200)).thenReturn(40);
		SupplyRecommendation prayer = supply(100, "Prayer potion(4)",
			"Prayer restore", 10, "doses");
		SupplyRecommendation ammo = supply(200, "Diamond bolts (e)",
			"Ranged ammo", 250, "shots");

		TripCostEstimate estimate = TripCostEstimator.estimate(
			Arrays.asList(prayer, ammo), itemManager, 20);

		assertEquals(46_000, estimate.getTotalGp());
		assertEquals(2_300, estimate.getGpPerKill());
		assertEquals(2, estimate.getBreakdown().size());
		assertTrue(estimate.getBreakdown().get(0).startsWith("Prayer restore"));
	}

	@Test
	public void ignoresDisabledAndUnpricedSupplies()
	{
		ItemManager itemManager = mock(ItemManager.class);
		SupplyRecommendation disabled = new SupplyRecommendation(
			300, 300, "Food", "Food", "test", SupplyStatus.BANKED, false,
			5, 0, 0, 0, "items");

		TripCostEstimate estimate = TripCostEstimator.estimate(
			java.util.Collections.singletonList(disabled), itemManager, 10);

		assertEquals(0, estimate.getTotalGp());
		assertTrue(estimate.getBreakdown().isEmpty());
	}

	@Test
	public void modelsCompatibleWornAmmoWithVisibleAssemblerRecovery()
	{
		ItemManager itemManager = mock(ItemManager.class);
		ItemStats stats = mock(ItemStats.class);
		when(stats.getEquipment()).thenReturn(ItemEquipmentStats.builder().aspeed(4).build());
		when(itemManager.getItemStats(1)).thenReturn(stats);
		when(itemManager.getItemPrice(2)).thenReturn(100);
		Map<EquipmentInventorySlot, GearRecommendation> items =
			new EnumMap<>(EquipmentInventorySlot.class);
		items.put(EquipmentInventorySlot.WEAPON,
			recommendation(1, "Rune crossbow", EquipmentInventorySlot.WEAPON));
		items.put(EquipmentInventorySlot.AMMO,
			recommendation(2, "Diamond bolts (e)", EquipmentInventorySlot.AMMO));
		items.put(EquipmentInventorySlot.CAPE,
			recommendation(3, "Ava's assembler", EquipmentInventorySlot.CAPE));
		LoadoutTier tier = new LoadoutTier(1, items, 0, 0,
			LoadoutOffenseEstimate.range(4, 4, 0, "Target", "Ranged", 24, 24));
		GearRecommendations recommendations = ready(tier, CombatStyle.RANGED);

		TripCostEstimate estimate = TripCostEstimator.estimate(
			recommendations, itemManager, 10);

		assertEquals(2_000, estimate.getTotalGp());
		assertTrue(estimate.getBreakdown().get(0).startsWith("Worn ammunition"));
	}

	@Test
	public void modelsPoweredStaffRunesAndCoinCharges()
	{
		ItemManager itemManager = mock(ItemManager.class);
		ItemStats stats = mock(ItemStats.class);
		when(stats.getEquipment()).thenReturn(ItemEquipmentStats.builder().aspeed(4).build());
		when(itemManager.getItemStats(1)).thenReturn(stats);
		when(itemManager.getItemPrice(net.runelite.api.gameval.ItemID.DEATHRUNE)).thenReturn(100);
		when(itemManager.getItemPrice(net.runelite.api.gameval.ItemID.CHAOSRUNE)).thenReturn(50);
		when(itemManager.getItemPrice(net.runelite.api.gameval.ItemID.FIRERUNE)).thenReturn(5);
		Map<EquipmentInventorySlot, GearRecommendation> items =
			new EnumMap<>(EquipmentInventorySlot.class);
		items.put(EquipmentInventorySlot.WEAPON,
			recommendation(1, "Trident of the seas", EquipmentInventorySlot.WEAPON));
		LoadoutTier tier = new LoadoutTier(1, items, 0, 0,
			LoadoutOffenseEstimate.range(4, 4, 0, "Target", "Trident of the seas", 2.4, 2.4));
		GearRecommendations recommendations = ready(tier, CombatStyle.MAGIC);

		TripCostEstimate estimate = TripCostEstimator.estimate(
			recommendations, itemManager, 10);

		assertEquals(1_850, estimate.getTotalGp());
		assertTrue(estimate.getBreakdown().get(0).startsWith("Combat casts"));
	}

	private static SupplyRecommendation supply(int id, String name, String category,
		int quantity, String unit)
	{
		return new SupplyRecommendation(id, id, name, category, "test",
			SupplyStatus.BANKED, true, quantity, 0, quantity, unit);
	}

	private static GearRecommendation recommendation(int id, String name,
		EquipmentInventorySlot slot)
	{
		return GearRecommendation.builder().itemId(id).canonicalItemId(id)
			.itemName(name).slot(slot).rank(1).build();
	}

	private static GearRecommendations ready(LoadoutTier tier, CombatStyle style)
	{
		Map<EquipmentInventorySlot, java.util.List<GearRecommendation>> bySlot =
			new EnumMap<>(EquipmentInventorySlot.class);
		for (Map.Entry<EquipmentInventorySlot, GearRecommendation> entry
			: tier.getItems().entrySet())
		{
			bySlot.put(entry.getKey(), Collections.singletonList(entry.getValue()));
		}
		return GearRecommendations.ready("Test", 10, null,
			GearStrategy.builder().name("Test").combatStyle(style).build(),
			Collections.emptyList(), bySlot, Collections.singletonList(tier),
			Collections.emptyList(), Collections.emptyList(), ReadinessReport.empty(), 3);
	}
}
