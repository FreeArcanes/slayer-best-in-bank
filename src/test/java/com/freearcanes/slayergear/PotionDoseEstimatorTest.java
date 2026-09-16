package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PotionDoseEstimatorTest
{
	private final SlayerGearAdvisorConfig noRegen = new SlayerGearAdvisorConfig()
	{
		@Override
		public boolean usePrayerRegen() { return false; }
	};

	@Test
	public void everyManualBossHasAnExplicitTripProfile()
	{
		for (String boss : BossSlayerCatalog.names())
		{
			assertTrue(boss, PotionDoseEstimator.hasEncounterProfile(boss));
		}
	}

	@Test
	public void prayerRestoreUsesThePlayersActualLevelAndPotionType()
	{
		assertEquals(31, PotionDoseEstimator.prayerRestorePerDose(99, false));
		assertEquals(32, PotionDoseEstimator.prayerRestorePerDose(99, true));
		assertEquals(24, PotionDoseEstimator.prayerRestorePerDose(70, false));
		assertEquals(25, PotionDoseEstimator.prayerRestorePerDose(70, true));
	}

	@Test
	public void araxxorCalibrationScalesWithKillsPrayerLevelAndGearBonus()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Araxxor").orElseThrow();
		GearStrategy strategy = profile.getStrategies().get(0);

		assertEquals(11, estimatePrayer(profile, strategy,
			new PotionEstimationContext("Araxxor", 99, 10, 103), 3, noRegen));
		assertEquals(5, estimatePrayer(profile, strategy,
			new PotionEstimationContext("Araxxor", 99, 0, 103), 1, noRegen));
		assertEquals(3, estimatePrayer(profile, strategy,
			new PotionEstimationContext("Araxxor", 99, 30, 103), 1, noRegen));
		assertEquals(5, estimatePrayer(profile, strategy,
			new PotionEstimationContext("Araxxor", 70, 10, 103), 1, noRegen));
	}

	@Test
	public void encounterTripCapsPreventFullBossAssignmentsFromFillingInventory()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Alchemical Hydra").orElseThrow();
		GearStrategy strategy = profile.getStrategies().get(0);
		int doses = estimatePrayer(profile, strategy,
			new PotionEstimationContext("Alchemical Hydra", 99, 10, 120),
			150, noRegen);

		assertEquals(34, doses); // ten-kill trip at 3.33 doses/kill
	}

	@Test
	public void prayerRegenerationReducesRatherThanDuplicatesRestoreDemand()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Alchemical Hydra").orElseThrow();
		GearStrategy strategy = profile.getStrategies().get(0);
		PotionEstimationContext context =
			new PotionEstimationContext("Alchemical Hydra", 99, 10, 120);
		SlayerGearAdvisorConfig withRegen = new SlayerGearAdvisorConfig() { };

		int without = estimatePrayer(profile, strategy, context, 10, noRegen);
		int with = estimatePrayer(profile, strategy, context, 10, withRegen);
		int regen = PotionDoseEstimator.estimate("Prayer regen", 10,
			profile, strategy, context, withRegen);

		assertEquals(34, without);
		assertEquals(3, regen);
		assertTrue(with < without);
	}

	@Test
	public void timedEffectsFollowCombatDurationInsteadOfRawKillCount()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Greater demons").orElseThrow();
		GearStrategy strategy = profile.getStrategies().get(0);
		PotionEstimationContext context =
			new PotionEstimationContext("Greater demons", 80, 10, 100);

		assertEquals(2, PotionDoseEstimator.estimate("Combat boost", 5,
			profile, strategy, context, noRegen));
		assertEquals(2, PotionDoseEstimator.estimate("Goading", 5,
			profile, strategy, context, noRegen));
		assertEquals(2, PotionDoseEstimator.estimate("Antifire", 5,
			profile, strategy, context, noRegen));
	}

	@Test
	public void rawBossDpsCannotEraseMechanicAndTransitionTime()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Araxxor").orElseThrow();
		GearStrategy strategy = profile.getStrategies().get(0);
		PotionEstimationContext unrealisticallyShortRawTtk =
			new PotionEstimationContext("Araxxor", 99, 10, 40);

		assertEquals(4, PotionDoseEstimator.estimate("Combat boost", 10,
			profile, strategy, unrealisticallyShortRawTtk, noRegen));
	}

	@Test
	public void waveEncountersUseWholeEncounterSupplyBudgets()
	{
		SlayerTaskProfile jad = TaskProfiles.find("TzTok-Jad").orElseThrow();
		SlayerTaskProfile zuk = TaskProfiles.find("TzKal-Zuk").orElseThrow();
		assertEquals(56, estimatePrayer(jad, jad.getStrategies().get(0),
			new PotionEstimationContext("TzTok-Jad", 99, 10, 0), 1, noRegen));
		assertEquals(40, estimatePrayer(zuk, zuk.getStrategies().get(0),
			new PotionEstimationContext("TzKal-Zuk", 99, 10, 0), 1, noRegen));
	}

	private static int estimatePrayer(SlayerTaskProfile profile,
		GearStrategy strategy, PotionEstimationContext context, int kills,
		SlayerGearAdvisorConfig config)
	{
		return PotionDoseEstimator.estimate(
			"Prayer", kills, profile, strategy, context, config);
	}
}
