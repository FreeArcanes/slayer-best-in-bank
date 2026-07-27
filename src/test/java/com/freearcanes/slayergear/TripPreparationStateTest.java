package com.freearcanes.slayergear;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;
import net.runelite.api.Item;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TripPreparationStateTest
{
	private static final IntUnaryOperator POTION_CANONICALIZER =
		itemId -> itemId == 101 || itemId == 102 ? 100 : itemId;

	@Test
	public void consumedPreparedSupplyUsesBankCloseSnapshotWithoutDuplicatingLiveDose()
	{
		TripPreparationState state = new TripPreparationState();
		SupplyRecommendation potion = new SupplyRecommendation(
			100, 100, "Divine super combat potion(4)", "Combat boost", "Boost",
			SupplyStatus.PACKED, false, 8, 8, 40, "doses");
		state.arm(
			readyRecommendations(Collections.singletonList(potion)),
			new Item[] {new Item(101, 2), new Item(200, 1)},
			POTION_CANONICALIZER);

		Item[] effective = state.suppliesForScoring(
			new Item[] {new Item(102, 1), new Item(200, 1)},
			POTION_CANONICALIZER);

		assertTrue(state.isActive());
		assertEquals(2, effective.length);
		assertEquals(2, quantityFor(effective, 101));
		assertEquals(0, quantityFor(effective, 102));
		assertEquals(1, quantityFor(effective, 200));
	}

	@Test
	public void deployedCannonPartRemainsPreparedUntilReset()
	{
		TripPreparationState state = new TripPreparationState();
		SupplyRecommendation cannonBase = new SupplyRecommendation(
			300, 300, "Cannon base", "Cannon setup", "Required cannon part",
			SupplyStatus.PACKED, true);
		state.arm(
			readyRecommendations(Collections.singletonList(cannonBase)),
			new Item[] {new Item(300, 1)},
			itemId -> itemId);

		assertEquals(1, quantityFor(
			state.suppliesForScoring(new Item[0], itemId -> itemId), 300));

		state.reset();
		assertFalse(state.isActive());
		assertEquals(0, state.suppliesForScoring(new Item[0], itemId -> itemId).length);
	}

	@Test
	public void incompleteBankExitDoesNotArmTripAllowance()
	{
		TripPreparationState state = new TripPreparationState();
		SupplyRecommendation missing = new SupplyRecommendation(
			300, 300, "Cannon base", "Cannon setup", "Required cannon part",
			SupplyStatus.BANKED, true);
		GearRecommendations incomplete = GearRecommendations.ready(
			"Dagannoth",
			80,
			null,
			null,
			Collections.emptyList(),
			Collections.emptyMap(),
			Collections.emptyList(),
			Collections.singletonList(missing),
			new ReadinessReport(
				0, 1, true, true, "Not required", 0, 0,
				Collections.singletonList("Pack Cannon base")),
			1);

		state.arm(incomplete, new Item[] {new Item(300, 1)}, itemId -> itemId);

		assertFalse(state.isActive());
	}

	@Test
	public void onlyARealAssignmentResetClearsTripLifecycle()
	{
		assertFalse(TripPreparationState.assignmentChanged(
			"Dagannoth", "Island of Stone", 80,
			"Dagannoth", "Island of Stone", 79));
		assertTrue(TripPreparationState.assignmentChanged(
			"Dagannoth", "Island of Stone", 1,
			"Dagannoth", "Island of Stone", 140));
		assertTrue(TripPreparationState.assignmentChanged(
			"Dagannoth", "Island of Stone", 80,
			"Dagannoth", "Lighthouse", 80));
		assertTrue(TripPreparationState.assignmentChanged(
			"Dagannoth", "Island of Stone", 80,
			"Bloodveld", "Meiyerditch Laboratory", 80));
	}

	private static GearRecommendations readyRecommendations(
		List<SupplyRecommendation> supplies)
	{
		return GearRecommendations.ready(
			"Dagannoth",
			80,
			null,
			null,
			Collections.emptyList(),
			Collections.emptyMap(),
			Collections.emptyList(),
			supplies,
			new ReadinessReport(
				1, 1, true, true, "Not required",
				supplies.size(), supplies.size(), Collections.emptyList()),
			1);
	}

	private static int quantityFor(Item[] items, int itemId)
	{
		return Arrays.stream(items)
			.filter(item -> item.getId() == itemId)
			.mapToInt(Item::getQuantity)
			.sum();
	}
}
