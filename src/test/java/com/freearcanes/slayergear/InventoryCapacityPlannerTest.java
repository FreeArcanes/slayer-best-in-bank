package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InventoryCapacityPlannerTest
{
	private final InventoryCapacityPlanner planner = new InventoryCapacityPlanner();

	@Test
	public void optionalFoodIsReducedFirstToFitTheInventory()
	{
		List<GearRecommendation> gear = List.of(
			gear(1001, EquipmentInventorySlot.HEAD),
			gear(1002, EquipmentInventorySlot.BODY));
		List<SupplyRecommendation> supplies = List.of(
			supply("Anglerfish", "Food", false, 8, 8, 0, "items"),
			supply("Goading potion(4)", "Goading", false, 4, 4, 0, "doses"));

		GearRecommendations fitted = planner.apply(
			recommendations(gear, supplies),
			occupiedInventory(20),
			true,
			false);

		SupplyRecommendation food = supplyByCategory(fitted, "Food");
		SupplyRecommendation goading = supplyByCategory(fitted, "Goading");
		assertEquals(5, food.getRecommendedQuantity());
		assertEquals(8, food.getRequestedQuantity());
		assertTrue(food.isCapacityAdjusted());
		assertEquals(4, goading.getRecommendedQuantity());
		assertEquals(31, fitted.getInventoryPlan().getRequestedSlots());
		assertEquals(28, fitted.getInventoryPlan().getPlannedSlots());
		assertTrue(fitted.getInventoryPlan().fits());
		assertTrue(fitted.getInventoryPlan().wasTrimmed());
	}

	@Test
	public void requiredSuppliesAreNeverTrimmed()
	{
		SupplyRecommendation requiredPrayer = supply(
			"Prayer potion(4)", "Prayer", true, 8, 8, 0, "doses");

		GearRecommendations fitted = planner.apply(
			recommendations(Collections.emptyList(), List.of(requiredPrayer)),
			occupiedInventory(27),
			true,
			false);

		assertEquals(8, supplyByCategory(fitted, "Prayer").getRecommendedQuantity());
		assertFalse(supplyByCategory(fitted, "Prayer").isCapacityAdjusted());
		assertEquals(29, fitted.getInventoryPlan().getPlannedSlots());
		assertEquals(1, fitted.getInventoryPlan().getOverBy());
		assertFalse(fitted.getInventoryPlan().fits());
	}

	@Test
	public void packedPotionOnlyPlansTheRemainingPotionSlot()
	{
		SupplyRecommendation prayer = new SupplyRecommendation(
			2001,
			2001,
			"Prayer potion(4)",
			"Prayer",
			"Sustain",
			SupplyStatus.PACKED_BANKED,
			false,
			8,
			8,
			4,
			40,
			"doses");

		assertEquals(1, InventoryCapacityPlanner.additionalSlots(prayer));
		GearRecommendations fitted = planner.apply(
			recommendations(Collections.emptyList(), List.of(prayer)),
			new Item[]{new Item(2001, 1)},
			true,
			false);
		assertEquals(2, fitted.getInventoryPlan().getPlannedSlots());
	}

	@Test
	public void prepFocusCyclesThroughEveryView()
	{
		assertEquals(PrepFocusMode.MISSING, PrepFocusMode.ALL.next());
		assertEquals(PrepFocusMode.GEAR, PrepFocusMode.MISSING.next());
		assertEquals(PrepFocusMode.SUPPLIES, PrepFocusMode.GEAR.next());
		assertEquals(PrepFocusMode.ALL, PrepFocusMode.SUPPLIES.next());
	}

	private static GearRecommendations recommendations(
		List<GearRecommendation> gear,
		List<SupplyRecommendation> supplies)
	{
		Map<EquipmentInventorySlot, GearRecommendation> selected =
			new EnumMap<>(EquipmentInventorySlot.class);
		Map<EquipmentInventorySlot, List<GearRecommendation>> bySlot =
			new EnumMap<>(EquipmentInventorySlot.class);
		for (GearRecommendation recommendation : gear)
		{
			selected.put(recommendation.getSlot(), recommendation);
			bySlot.put(recommendation.getSlot(), List.of(recommendation));
		}

		List<LoadoutTier> tiers = selected.isEmpty()
			? Collections.emptyList()
			: List.of(new LoadoutTier(1, selected));
		return GearRecommendations.ready(
			"Greater demons",
			80,
			null,
			null,
			Collections.emptyList(),
			bySlot,
			tiers,
			supplies,
			new ReadinessReport(
				0,
				gear.size(),
				true,
				true,
				"Not required",
				0,
				supplies.size(),
				Collections.emptyList()),
			gear.size());
	}

	private static GearRecommendation gear(
		int itemId,
		EquipmentInventorySlot slot)
	{
		return GearRecommendation.builder()
			.itemId(itemId)
			.canonicalItemId(itemId)
			.itemName("Gear " + itemId)
			.slot(slot)
			.rank(1)
			.reason("Test")
			.banked(true)
			.packed(false)
			.build();
	}

	private static SupplyRecommendation supply(
		String itemName,
		String category,
		boolean required,
		int automatic,
		int recommended,
		int packed,
		String unit)
	{
		return new SupplyRecommendation(
			itemName.hashCode(),
			itemName.hashCode(),
			itemName,
			category,
			"Test",
			packed > 0 ? SupplyStatus.PACKED_BANKED : SupplyStatus.BANKED,
			required,
			automatic,
			recommended,
			packed,
			100,
			unit);
	}

	private static SupplyRecommendation supplyByCategory(
		GearRecommendations recommendations,
		String category)
	{
		return recommendations.getSupplies().stream()
			.filter(supply -> category.equals(supply.getCategory()))
			.findFirst()
			.orElseThrow();
	}

	private static Item[] occupiedInventory(int count)
	{
		List<Item> items = new ArrayList<>();
		for (int index = 0; index < count; index++)
		{
			items.add(new Item(10_000 + index, 1));
		}
		return items.toArray(new Item[0]);
	}
}
