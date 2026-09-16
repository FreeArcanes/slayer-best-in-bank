package com.freearcanes.slayergear;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Duration-, loadout-, and encounter-aware potion dose estimator.
 *
 * <p>Boss baselines are deliberately conservative trip-planning values. The
 * strongest published inputs are calibrated directly (Hydra, Araxxor,
 * Cerberus, Vorkath, Zulrah, Sire, Duke and Vardorvis); the remaining manual
 * boss encounters use the same drain model with encounter-specific trip caps.
 * All values remain user-overridable per task.</p>
 */
final class PotionDoseEstimator
{
	private static final double REFERENCE_PRAYER_RESISTANCE = 80.0; // +10 bonus
	private static final double PRAYER_REGEN_POINTS_PER_DOSE = 66.0;
	private static final Map<String, Encounter> BOSSES = buildBosses();

	private PotionDoseEstimator() { }

	static int estimate(String category, int plannedKills,
		SlayerTaskProfile profile, GearStrategy strategy,
		PotionEstimationContext context, SlayerGearAdvisorConfig config)
	{
		if (plannedKills <= 0 || context == null)
		{
			return SmartSupplyAdvisor.recommendedQuantity(category, plannedKills);
		}
		// Per-kill stacked tools cover the selected trip plan directly. Unlike
		// potions, they do not need an encounter-duration or inventory-slot cap.
		if ("Fishing explosives".equals(category))
		{
			return SmartSupplyAdvisor.recommendedQuantity(category, plannedKills);
		}

		Encounter encounter = findEncounter(context.getTaskName(), profile, strategy);
		int kills = encounter == null
			? plannedKills : Math.min(plannedKills, encounter.maxKillsPerTrip);
		double durationSeconds = durationSeconds(kills, encounter, strategy, context);

		switch (category)
		{
			case "Prayer":
				return prayerDoses(kills, durationSeconds, encounter, profile,
					strategy, context, config);
			case "Prayer regen":
				// It is poor inventory value for trips shorter than most of one dose.
				return durationSeconds < 360 ? 0
					: timedDoses(durationSeconds, 8.0, 12);
			case "Combat boost":
			case "Ranged boost":
				return timedDoses(durationSeconds, 5.0, 12);
			case "Goading":
				return timedDoses(durationSeconds, 6.0, 12);
			case "Antifire":
				// Six minutes is safe for regular antifire and extended super antifire.
				return timedDoses(durationSeconds, 6.0, 16);
			case "Venom protection":
				// Extended anti-venom+ protects from venom for about 6.3 minutes.
				return timedDoses(durationSeconds, 6.3, 16);
			case "Poison protection":
				return timedDoses(durationSeconds, 6.0, 16);
			default:
				return SmartSupplyAdvisor.recommendedQuantity(category, kills);
		}
	}

	static boolean hasEncounterProfile(String bossName)
	{
		return BOSSES.containsKey(normalize(bossName));
	}

	static int prayerRestorePerDose(int prayerLevel, boolean superRestore)
	{
		return Math.max(1, prayerLevel) / 4 + (superRestore ? 8 : 7);
	}

	private static int prayerDoses(int kills, double durationSeconds,
		Encounter encounter, SlayerTaskProfile profile, GearStrategy strategy,
		PotionEstimationContext context, SlayerGearAdvisorConfig config)
	{
		boolean superRestore = config != null
			&& config.prayerRestorePreference() == PrayerRestorePreference.SUPER_RESTORE;
		int restore = prayerRestorePerDose(context.getPrayerLevel(), superRestore);
		double baseDoses;
		if (encounter != null && encounter.fixedPrayerDoses > 0)
		{
			baseDoses = encounter.fixedPrayerDoses;
		}
		else if (encounter != null && encounter.prayerDosesPerKill > 0)
		{
			baseDoses = encounter.prayerDosesPerKill * kills;
		}
		else
		{
			double pointsPerMinute = prayerPointsPerMinute(
				profile, strategy, encounter != null);
			double resistance = 60.0 + context.getPrayerBonus() * 2.0;
			double totalDrain = durationSeconds / 60.0
				* pointsPerMinute * 60.0 / resistance;
			// A normal trip starts from a full Prayer bar.
			baseDoses = Math.max(0, totalDrain - context.getPrayerLevel()) / restore;
		}

		// Published boss figures generally assume high Prayer and roughly +10
		// Prayer bonus. Scale them to the player's actual selected loadout.
		if (encounter != null
			&& (encounter.prayerDosesPerKill > 0 || encounter.fixedPrayerDoses > 0))
		{
			double resistance = 60.0 + context.getPrayerBonus() * 2.0;
			baseDoses *= REFERENCE_PRAYER_RESISTANCE / resistance;
			baseDoses *= 31.0 / restore;
		}

		if (config != null && config.usePrayerRegen() && durationSeconds >= 360)
		{
			int regenDoses = timedDoses(durationSeconds, 8.0, 12);
			baseDoses -= regenDoses * PRAYER_REGEN_POINTS_PER_DOSE / restore;
		}
		int maximum = encounter == null ? 32
			: encounter.fixedPrayerDoses > 0 ? 64 : 48;
		return clamp(1, maximum,
			(int) Math.ceil(Math.max(0.25, baseDoses)));
	}

	private static double prayerPointsPerMinute(
		SlayerTaskProfile profile, GearStrategy strategy, boolean bossEncounter)
	{
		String advice = normalize(profile == null ? "" : profile.getProtectionAdvice());
		boolean protection = advice.contains("protect from")
			|| advice.contains("protection prayer")
			|| advice.contains("use prayer");
		if (strategy != null && strategy.isAncientAoe()) return 60.0;
		return protection || bossEncounter ? 58.0 : 38.0;
	}

	private static int timedDoses(double durationSeconds, double minutesPerDose, int maximum)
	{
		return clamp(1, maximum,
			(int) Math.ceil(durationSeconds / (minutesPerDose * 60.0)));
	}

	private static double durationSeconds(int kills, Encounter encounter,
		GearStrategy strategy, PotionEstimationContext context)
	{
		double secondsPerKill = context.getSecondsPerKill();
		if (encounter != null && encounter.fixedDurationSeconds <= 0)
		{
			// Raw HP/DPS omits phase transitions, movement and forced downtime.
			// Never let that make a boss duration shorter than its encounter floor.
			secondsPerKill = Math.max(secondsPerKill,
				encounter.fallbackSecondsPerKill);
		}
		if (secondsPerKill <= 0)
		{
			secondsPerKill = encounter == null
				? strategy != null && (strategy.isAncientAoe()
					|| SmartSupplyAdvisor.isVenator(strategy)) ? 7.0 : 18.0
				: encounter.fallbackSecondsPerKill;
		}
		if (encounter != null && encounter.fixedDurationSeconds > 0)
		{
			return encounter.fixedDurationSeconds;
		}
		double downtime = encounter == null ? 2.5 : encounter.downtimeSeconds;
		return Math.max(30.0, secondsPerKill * kills
			+ downtime * Math.max(0, kills - 1));
	}

	private static Encounter findEncounter(String taskName,
		SlayerTaskProfile profile, GearStrategy strategy)
	{
		Encounter direct = BOSSES.get(normalize(taskName));
		if (direct != null) return direct;
		if (profile != null)
		{
			direct = BOSSES.get(normalize(profile.getDisplayName()));
			if (direct != null) return direct;
		}
		String method = normalize(strategy == null ? "" : strategy.getName());
		for (Map.Entry<String, Encounter> entry : BOSSES.entrySet())
		{
			if (method.contains(entry.getKey())) return entry.getValue();
		}
		return null;
	}

	private static Map<String, Encounter> buildBosses()
	{
		Map<String, Encounter> values = new LinkedHashMap<>();
		// name, doses/kill, trip kill cap, fallback kill seconds, downtime
		boss(values, "Abyssal Sire", 1.45, 10, 92, 12);
		boss(values, "Alchemical Hydra", 3.33, 10, 120, 8);
		boss(values, "Amoxliatl", 0, 12, 95, 9);
		boss(values, "Araxxor", 3.43, 10, 103, 10);
		boss(values, "Barrows Brothers", 0, 6, 75, 15);
		boss(values, "Branda the Fire Queen", 0, 15, 80, 12);
		boss(values, "Brutus", 0, 35, 25, 8);
		boss(values, "Bryophyta", 0, 10, 55, 15);
		boss(values, "Callisto", 0, 8, 105, 12);
		boss(values, "Cerberus", 2.00, 8, 85, 10);
		boss(values, "Chaos Elemental", 0, 8, 95, 15);
		boss(values, "Chaos Fanatic", 0, 15, 45, 10);
		boss(values, "Commander Zilyana", 0, 18, 90, 20);
		boss(values, "Crazy Archaeologist", 0, 15, 45, 10);
		boss(values, "Dagannoth Kings", 0, 18, 75, 15);
		boss(values, "Demonic Gorillas", 0, 20, 55, 5);
		boss(values, "Deranged Archaeologist", 0, 15, 45, 10);
		boss(values, "Duke Sucellus", 1.60, 12, 106, 22);
		boss(values, "Eldric the Ice King", 0, 15, 80, 12);
		boss(values, "General Graardor", 0, 18, 75, 20);
		boss(values, "Giant Mole", 0, 20, 45, 8);
		boss(values, "Grotesque Guardians", 0, 10, 120, 15);
		boss(values, "K'ril Tsutsaroth", 0, 15, 80, 20);
		boss(values, "Kalphite Queen", 0, 6, 115, 15);
		boss(values, "King Black Dragon", 0, 12, 80, 10);
		boss(values, "Kraken", 0, 25, 55, 12);
		boss(values, "Kree'arra", 0, 15, 95, 20);
		boss(values, "Maggot King", 0, 12, 90, 12);
		boss(values, "Obor", 0, 10, 55, 15);
		boss(values, "Phantom Muspah", 0, 8, 150, 10);
		boss(values, "Sarachnis", 0, 12, 75, 10);
		boss(values, "Scorpia", 0, 12, 65, 10);
		boss(values, "Scurrius", 0, 15, 60, 8);
		boss(values, "Shellbane Gryphon", 0, 12, 90, 12);
		boss(values, "Skotizo", 0, 5, 110, 15);
		boss(values, "The Leviathan", 0, 8, 135, 12);
		boss(values, "The Whisperer", 0, 8, 140, 12);
		boss(values, "Thermonuclear Smoke Devil", 0, 20, 55, 10);
		boss(values, "Tormented Demons", 0, 15, 75, 8);
		fixed(values, "TzKal-Zuk", 40, 7_200);
		fixed(values, "TzTok-Jad", 56, 3_600);
		boss(values, "Vardorvis", 2.00, 12, 112, 10);
		boss(values, "Venenatis", 0, 8, 105, 12);
		boss(values, "Vet'ion", 0, 10, 95, 12);
		boss(values, "Vorkath", 2.13, 6, 120, 15);
		boss(values, "Zulrah", 2.00, 4, 120, 30);
		return values;
	}

	private static void boss(Map<String, Encounter> values, String name,
		double doses, int tripKills, double seconds, double downtime)
	{
		values.put(normalize(name),
			new Encounter(doses, tripKills, seconds, downtime, 0, 0));
	}

	private static void fixed(Map<String, Encounter> values, String name,
		int prayerDoses, double durationSeconds)
	{
		values.put(normalize(name),
			new Encounter(0, 1, durationSeconds, 0, prayerDoses, durationSeconds));
	}

	private static int clamp(int minimum, int maximum, int value)
	{
		return Math.max(minimum, Math.min(maximum, value));
	}

	private static String normalize(String value)
	{
		return value == null ? "" : NameMatcher.normalize(value)
			.toLowerCase(Locale.ENGLISH);
	}

	private static final class Encounter
	{
		private final double prayerDosesPerKill;
		private final int maxKillsPerTrip;
		private final double fallbackSecondsPerKill;
		private final double downtimeSeconds;
		private final int fixedPrayerDoses;
		private final double fixedDurationSeconds;

		private Encounter(double prayerDosesPerKill, int maxKillsPerTrip,
			double fallbackSecondsPerKill, double downtimeSeconds,
			int fixedPrayerDoses, double fixedDurationSeconds)
		{
			this.prayerDosesPerKill = prayerDosesPerKill;
			this.maxKillsPerTrip = maxKillsPerTrip;
			this.fallbackSecondsPerKill = fallbackSecondsPerKill;
			this.downtimeSeconds = downtimeSeconds;
			this.fixedPrayerDoses = fixedPrayerDoses;
			this.fixedDurationSeconds = fixedDurationSeconds;
		}
	}
}
