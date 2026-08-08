package com.freearcanes.slayergear;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SupplyQuantityPlanningTest
{
	@Test
	public void cannonAmmoScalesWithRemainingTask()
	{
		assertEquals(100, SmartSupplyAdvisor.recommendedQuantity("Cannon ammo", 10));
		assertEquals(1200, SmartSupplyAdvisor.recommendedQuantity("Cannon ammo", 150));
		assertEquals("shots", SmartSupplyAdvisor.quantityUnit("Cannon ammo"));
	}

	@Test
	public void foodAndPotionTargetsStayWithinInventoryFriendlyCaps()
	{
		assertEquals(2, SmartSupplyAdvisor.recommendedQuantity("Food", 1));
		assertEquals(8, SmartSupplyAdvisor.recommendedQuantity("Food", 150));
		assertEquals(12, SmartSupplyAdvisor.recommendedQuantity("Food", 500));
		assertEquals(4, SmartSupplyAdvisor.recommendedQuantity("Prayer", 30));
		assertEquals(12, SmartSupplyAdvisor.recommendedQuantity("Prayer", 500));
		assertEquals("doses", SmartSupplyAdvisor.quantityUnit("Prayer"));
	}

	@Test
	public void eightyKillTaskProducesTwoSupportPotionsAndSafetyFood()
	{
		assertEquals(8, SmartSupplyAdvisor.recommendedQuantity("Combat boost", 80));
		assertEquals(8, SmartSupplyAdvisor.recommendedQuantity("Prayer regen", 80));
		assertEquals(8, SmartSupplyAdvisor.recommendedQuantity("Goading", 80));
		assertEquals(8, SmartSupplyAdvisor.recommendedQuantity("Prayer", 80));
		assertEquals(4, SmartSupplyAdvisor.recommendedQuantity("Food", 80));
	}

	@Test
	public void unknownTaskAmountDisablesQuantityTarget()
	{
		assertEquals(0, SmartSupplyAdvisor.recommendedQuantity("Food", -1));
		assertEquals(0, SmartSupplyAdvisor.recommendedQuantity("Food", 0));
	}

	@Test
	public void recommendationTracksPartialAndCompletePacking()
	{
		SupplyRecommendation partial = new SupplyRecommendation(
			1, 1, "Cannonball", "Cannon ammo", "Task estimate",
			SupplyStatus.PACKED_BANKED, true, 800, 300, 2000, "shots");
		assertTrue(partial.hasQuantityTarget());
		assertFalse(partial.hasRecommendedQuantityPacked());
		assertEquals(500, partial.getQuantityStillNeeded());

		SupplyRecommendation complete = new SupplyRecommendation(
			1, 1, "Cannonball", "Cannon ammo", "Task estimate",
			SupplyStatus.PACKED_BANKED, true, 800, 800, 1500, "shots");
		assertTrue(complete.hasRecommendedQuantityPacked());
		assertEquals(0, complete.getQuantityStillNeeded());
	}

	@Test
	public void fourDosePotionShowsRemainingManualClicks()
	{
		SupplyRecommendation twoPotions = new SupplyRecommendation(
			1, 1, "Divine super combat potion(4)", "Combat boost", "Task estimate",
			SupplyStatus.BANKED, false, 8, 0, 40, "doses");
		assertEquals(4, twoPotions.getUnitsPerWithdrawal());
		assertEquals(2, twoPotions.getWithdrawalsStillNeeded());

		SupplyRecommendation oneMorePotion = new SupplyRecommendation(
			1, 1, "Divine super combat potion(4)", "Combat boost", "Task estimate",
			SupplyStatus.PACKED_BANKED, false, 8, 4, 36, "doses");
		assertEquals(1, oneMorePotion.getWithdrawalsStillNeeded());
	}

	@Test
	public void supportPotionsAreConsideredForOrdinarySlayerMethods()
	{
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, new SlayerGearAdvisorConfig() {});
		GearStrategy melee = GearStrategy.builder()
			.name("Ordinary melee")
			.combatStyle(CombatStyle.MELEE)
			.build();
		List<String> categories = advisor.buildRules(
				TaskProfiles.find("Bloodveld").orElseThrow(), melee).stream()
			.map(SmartSupplyAdvisor.SupplyRule::getCategory)
			.collect(Collectors.toList());

		assertTrue(categories.contains("Goading"));
		assertTrue(categories.contains("Prayer regen"));
		assertTrue(categories.contains("Combat boost"));
		assertTrue(categories.contains("Prayer"));
	}

	@Test
	public void prayerPotionIsTheDefaultExclusivePrayerRestore()
	{
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, new SlayerGearAdvisorConfig() {});
		SmartSupplyAdvisor.SupplyRule prayer = advisor.buildRules(
			TaskProfiles.find("Bloodveld").orElseThrow(),
			GearStrategy.builder().name("Melee").combatStyle(CombatStyle.MELEE).build()).stream()
			.filter(rule -> "Prayer".equals(rule.getCategory()))
			.findFirst()
			.orElseThrow();

		assertEquals(1, prayer.getPreferredNames().size());
		assertEquals("prayer potion", prayer.getPreferredNames().get(0));
	}

	@Test
	public void superRestorePreferenceExcludesPrayerPotions()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig()
		{
			@Override
			public PrayerRestorePreference prayerRestorePreference()
			{
				return PrayerRestorePreference.SUPER_RESTORE;
			}
		};
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, config);
		SmartSupplyAdvisor.SupplyRule prayer = advisor.buildRules(
			TaskProfiles.find("Bloodveld").orElseThrow(),
			GearStrategy.builder().name("Melee").combatStyle(CombatStyle.MELEE).build()).stream()
			.filter(rule -> "Prayer".equals(rule.getCategory()))
			.findFirst()
			.orElseThrow();

		assertEquals(1, prayer.getPreferredNames().size());
		assertEquals("super restore", prayer.getPreferredNames().get(0));
	}

	@Test
	public void selectedSlayerBraceletIsIncludedAsAnInventorySwitch()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig()
		{
			@Override
			public boolean useSlayerBracelet()
			{
				return true;
			}

			@Override
			public SlayerBraceletPreference slayerBraceletPreference()
			{
				return SlayerBraceletPreference.SLAUGHTER;
			}
		};
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, config);

		SmartSupplyAdvisor.SupplyRule bracelet = advisor.buildRules(
			TaskProfiles.find("Bloodveld").orElseThrow(),
			GearStrategy.builder().name("Melee").combatStyle(CombatStyle.MELEE).build()).stream()
			.filter(rule -> "Slayer bracelet".equals(rule.getCategory()))
			.findFirst()
			.orElseThrow();

		assertEquals("bracelet of slaughter", bracelet.getPreferredNames().get(0));
	}

	@Test
	public void slayerBraceletsRemainOptIn()
	{
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, new SlayerGearAdvisorConfig() {});

		assertFalse(advisor.buildRules(
			TaskProfiles.find("Bloodveld").orElseThrow(),
			GearStrategy.builder().name("Melee").combatStyle(CombatStyle.MELEE).build()).stream()
			.anyMatch(rule -> "Slayer bracelet".equals(rule.getCategory())));
	}

	@Test
	public void warpedCreaturesRequireCrystalChimes()
	{
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, new SlayerGearAdvisorConfig() {});
		SlayerTaskProfile warped = TaskProfiles.find("Warped creatures").orElseThrow();

		SmartSupplyAdvisor.SupplyRule chimes = advisor.buildRules(
			warped, warped.getStrategies().get(0)).stream()
			.filter(rule -> rule.getPreferredNames().contains("crystal chime"))
			.findFirst()
			.orElseThrow();

		assertEquals("Task tool", chimes.getCategory());
	}

	@Test
	public void kalphitePrepIncludesPoisonProtectionAndGeneralSupplies()
	{
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, new SlayerGearAdvisorConfig() {});
		SlayerTaskProfile profile = TaskProfiles.find("Kalphites").orElseThrow();
		GearStrategy strategy = profile.getStrategies().get(0);
		List<SmartSupplyAdvisor.SupplyRule> rules = advisor.buildRules(profile, strategy);
		List<String> categories = rules.stream()
			.map(SmartSupplyAdvisor.SupplyRule::getCategory)
			.collect(Collectors.toList());
		SmartSupplyAdvisor.SupplyRule poison = rules.stream()
			.filter(rule -> "Poison protection".equals(rule.getCategory()))
			.findFirst()
			.orElseThrow();

		assertTrue(categories.contains("Poison protection"));
		assertTrue(categories.contains("Cannon ammo"));
		assertTrue(categories.contains("Combat boost"));
		assertTrue(categories.contains("Prayer"));
		assertTrue(categories.contains("Food"));
		assertTrue(categories.contains("Run energy"));
		assertTrue(poison.getPreferredNames().contains("antidote++"));
		assertTrue(poison.getPreferredNames().contains("superantipoison"));
		assertTrue(poison.getPreferredNames().contains("antipoison"));
		assertEquals(4, SmartSupplyAdvisor.recommendedQuantity("Poison protection", 16));
		assertEquals("doses", SmartSupplyAdvisor.quantityUnit("Poison protection"));

		SupplyRecommendation missingPoison = advisor.recommend(
			profile, strategy, 16, null, null).stream()
			.filter(supply -> "Poison protection".equals(supply.getCategory()))
			.findFirst()
			.orElseThrow();
		assertEquals(SupplyStatus.MISSING, missingPoison.getStatus());
		assertFalse(missingPoison.isRequired());
		assertTrue(missingPoison.hasQuantityTarget());
	}

	@Test
	public void bastionIsPreferredOverOrdinaryRangingPotion()
	{
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, new SlayerGearAdvisorConfig() {});
		GearStrategy ranged = GearStrategy.builder()
			.name("Ordinary ranged")
			.combatStyle(CombatStyle.RANGED)
			.build();
		SmartSupplyAdvisor.SupplyRule boost = advisor.buildRules(
				TaskProfiles.find("Bloodveld").orElseThrow(), ranged).stream()
			.filter(rule -> "Ranged boost".equals(rule.getCategory()))
			.findFirst()
			.orElseThrow();

		assertTrue(boost.getPreferredNames().indexOf("bastion potion")
			< boost.getPreferredNames().indexOf("ranging potion"));
	}

	@Test
	public void equipPathUsesFourColumnZigzag()
	{
		assertEquals(0, TieredBankLayout.zigzagColumn(0, 4));
		assertEquals(3, TieredBankLayout.zigzagColumn(3, 4));
		assertEquals(3, TieredBankLayout.zigzagColumn(4, 4));
		assertEquals(0, TieredBankLayout.zigzagColumn(7, 4));
		assertEquals(0, TieredBankLayout.zigzagColumn(8, 4));
	}

	@Test
	public void tripPlansCapTheNumberOfKillsBeingPrepared()
	{
		assertEquals(120, SmartSupplyAdvisor.plannedKillCount(
			TripPlan.FULL_ASSIGNMENT, 120, 80));
		assertEquals(40, SmartSupplyAdvisor.plannedKillCount(
			TripPlan.SHORT_TRIP, 120, 80));
		assertEquals(25, SmartSupplyAdvisor.plannedKillCount(
			TripPlan.SHORT_TRIP, 25, 80));
		assertEquals(80, SmartSupplyAdvisor.plannedKillCount(
			TripPlan.CUSTOM_KILLS, 120, 80));
		assertEquals(30, SmartSupplyAdvisor.plannedKillCount(
			TripPlan.CUSTOM_KILLS, 30, 80));
	}

	@Test
	public void safetyLevelsScaleAndRoundToWholeWithdrawals()
	{
		assertEquals(4, SmartSupplyAdvisor.applySupplyLevel(4, SupplyLevel.LIGHT, 4));
		assertEquals(8, SmartSupplyAdvisor.applySupplyLevel(8, SupplyLevel.NORMAL, 4));
		assertEquals(12, SmartSupplyAdvisor.applySupplyLevel(8, SupplyLevel.EXTRA, 4));
		assertEquals(6, SmartSupplyAdvisor.applySupplyLevel(4, SupplyLevel.EXTRA, 1));
	}

	@Test
	public void supportPotionTogglesRemoveOptionalRules()
	{
		SlayerGearAdvisorConfig disabled = new SlayerGearAdvisorConfig()
		{
			@Override
			public boolean useGoading() { return false; }

			@Override
			public boolean usePrayerRegen() { return false; }
		};
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, disabled);
		GearStrategy melee = GearStrategy.builder()
			.name("Melee")
			.combatStyle(CombatStyle.MELEE)
			.build();
		List<String> categories = advisor.buildRules(
				TaskProfiles.find("Greater demons").orElseThrow(), melee).stream()
			.map(SmartSupplyAdvisor.SupplyRule::getCategory)
			.collect(Collectors.toList());
		assertFalse(categories.contains("Goading"));
		assertFalse(categories.contains("Prayer regen"));
		assertTrue(categories.contains("Combat boost"));
	}

	@Test
	public void regularBoostPreferenceMovesDivineOptionsBehindRegularOptions()
	{
		SlayerGearAdvisorConfig regular = new SlayerGearAdvisorConfig()
		{
			@Override
			public boolean preferDivineBoosts() { return false; }
		};
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, regular);
		GearStrategy ranged = GearStrategy.builder()
			.name("Ranged")
			.combatStyle(CombatStyle.RANGED)
			.build();
		SmartSupplyAdvisor.SupplyRule boost = advisor.buildRules(
				TaskProfiles.find("Bloodveld").orElseThrow(), ranged).stream()
			.filter(rule -> "Ranged boost".equals(rule.getCategory()))
			.findFirst()
			.orElseThrow();
		assertTrue(boost.getPreferredNames().indexOf("bastion potion")
			< boost.getPreferredNames().indexOf("divine bastion potion"));
		assertFalse(SmartSupplyAdvisor.matchesPreferredSupply(
			"divine bastion potion(4)", "bastion potion"));
		assertTrue(SmartSupplyAdvisor.matchesPreferredSupply(
			"bastion potion(4)", "bastion potion"));
	}

	@Test
	public void taskSupplyOverrideKeysAreStableAndScoped()
	{
		assertEquals("supply.greater-demons.prayer-regen",
			SmartSupplyAdvisor.quantityOverrideKey("greater-demons", "Prayer regen"));
	}

	@Test
	public void potionEstimateBetaToggleOnlyDisablesPotionQuantities()
	{
		SlayerGearAdvisorConfig disabled = new SlayerGearAdvisorConfig()
		{
			@Override
			public boolean potionEstimatesEnabled() { return false; }
		};
		assertTrue(SmartSupplyAdvisor.isPotionQuantityCategory("Combat boost"));
		assertTrue(SmartSupplyAdvisor.isPotionQuantityCategory("Prayer regen"));
		assertFalse(SmartSupplyAdvisor.isPotionQuantityCategory("Food"));
		assertFalse(SmartSupplyAdvisor.quantityTargetEnabled(disabled, "Combat boost"));
		assertFalse(SmartSupplyAdvisor.quantityTargetEnabled(disabled, "Prayer"));
		assertTrue(SmartSupplyAdvisor.quantityTargetEnabled(disabled, "Food"));
		assertTrue(SmartSupplyAdvisor.quantityTargetEnabled(disabled, "Cannon ammo"));
	}

	@Test
	public void potionRecommendationsRemainConfiguredWhenEstimateIsDisabled()
	{
		SlayerGearAdvisorConfig disabled = new SlayerGearAdvisorConfig()
		{
			@Override
			public boolean potionEstimatesEnabled() { return false; }
		};
		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(null, disabled);
		GearStrategy melee = GearStrategy.builder()
			.name("Melee")
			.combatStyle(CombatStyle.MELEE)
			.build();
		List<String> categories = advisor.buildRules(
				TaskProfiles.find("Greater demons").orElseThrow(), melee).stream()
			.map(SmartSupplyAdvisor.SupplyRule::getCategory)
			.collect(Collectors.toList());
		assertTrue(categories.contains("Combat boost"));
		assertTrue(categories.contains("Prayer"));
		assertTrue(categories.contains("Goading"));
		assertTrue(categories.contains("Prayer regen"));
	}

	@Test
	public void zeroOverrideRemainsAdjustableWithoutEnteringTheTripPlan()
	{
		SupplyRecommendation disabled = new SupplyRecommendation(
			1, 1, "Goading potion(4)", "Goading", "Task estimate",
			SupplyStatus.BANKED, false, 8, 0, 0, 40, "doses");

		assertTrue(disabled.isQuantityAdjustable());
		assertFalse(disabled.hasQuantityTarget());
		assertFalse(disabled.isEnabledForTrip());
		assertFalse(TaskPrepReminderOverlay.shouldRemindSupply(disabled));
	}

	@Test
	public void presenceOnlyPotionStillProducesPostBankReminder()
	{
		SupplyRecommendation banked = new SupplyRecommendation(
			1, 1, "Prayer regeneration potion(4)", "Prayer regen", "Sustain",
			SupplyStatus.BANKED, false);
		SupplyRecommendation packed = new SupplyRecommendation(
			1, 1, "Prayer regeneration potion(4)", "Prayer regen", "Sustain",
			SupplyStatus.PACKED_BANKED, false);

		assertTrue(TaskPrepReminderOverlay.shouldRemindSupply(banked));
		assertFalse(TaskPrepReminderOverlay.shouldRemindSupply(packed));
	}

	@Test
	public void exactPotionDoseWidgetWinsOverCanonicalFallback()
	{
		Map<Integer, String> exact = new HashMap<>();
		exact.put(1, "one-dose");
		exact.put(4, "four-dose");
		Map<Integer, String> canonical = new HashMap<>();
		canonical.put(100, "first-canonical-widget");

		assertEquals("four-dose",
			TieredBankLayout.selectExactOrCanonical(exact, canonical, 4, 100));
		assertEquals("first-canonical-widget",
			TieredBankLayout.selectExactOrCanonical(exact, canonical, 2, 100));
	}

	@Test
	public void withdrawnPathEntriesKeepTheirOriginalReservedPositions()
	{
		// Six recommendations reserve two rows even if indices 0 and 1 have already
		// been withdrawn. The next visible items remain at original indices 2 and 3
		// instead of compacting back to columns 0 and 1.
		assertEquals(2, TieredBankLayout.rowsFor(6, 4));
		assertEquals(2, TieredBankLayout.zigzagColumn(2, 4));
		assertEquals(3, TieredBankLayout.zigzagColumn(3, 4));
		assertEquals(3, TieredBankLayout.zigzagColumn(4, 4));
	}

	@Test
	public void queuedQuantityAdjustmentsUseTheLatestStoredValue()
	{
		assertEquals(8, SlayerGearAdvisorPlugin.savedSupplyQuantity(null, 8));
		assertEquals(12, SlayerGearAdvisorPlugin.savedSupplyQuantity("12", 8));
		assertEquals(8, SlayerGearAdvisorPlugin.savedSupplyQuantity("invalid", 8));

		int first = SlayerGearAdvisorPlugin.adjustedSupplyQuantity(
			8, 4, false, SupplyQuantityAction.INCREASE);
		int second = SlayerGearAdvisorPlugin.adjustedSupplyQuantity(
			first, 4, false, SupplyQuantityAction.INCREASE);
		assertEquals(12, first);
		assertEquals(16, second);
		assertEquals(0, SlayerGearAdvisorPlugin.adjustedSupplyQuantity(
			4, 4, false, SupplyQuantityAction.DECREASE));
		assertEquals(4, SlayerGearAdvisorPlugin.adjustedSupplyQuantity(
			4, 4, true, SupplyQuantityAction.DECREASE));
	}

	@Test
	public void recommendationRefreshPolicyIncludesProfileSupplyOverrides()
	{
		assertTrue(SlayerGearAdvisorPlugin.isRecommendationConfigKey("tripPlan"));
		assertTrue(SlayerGearAdvisorPlugin.isRecommendationConfigKey(
			"prayerRestorePreference"));
		assertTrue(SlayerGearAdvisorPlugin.isRecommendationConfigKey(
			"supply.greater-demons.prayer-regen"));
		assertFalse(SlayerGearAdvisorPlugin.isRecommendationConfigKey("bestColor"));
		assertFalse(SlayerGearAdvisorPlugin.isRecommendationConfigKey(null));
	}
}
