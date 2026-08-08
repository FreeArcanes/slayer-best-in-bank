package com.freearcanes.slayergear;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wiki-aligned fast routes for assignments issued by Turael or Aya.
 *
 * These profiles deliberately target the low-level assignment variant rather
 * than inheriting a broad task's boss aliases.  The first method uses a toxic
 * blowpipe when the account owns one; the second keeps the mode useful by
 * selecting the best other owned ranged weapon.
 */
final class TuraelSpeedProfiles
{
	private static final String STRATEGY_PREFIX = "Turael/Aya speed";
	private static final Map<String, Route> ROUTES = new LinkedHashMap<>();

	static
	{
		route("Banshees", "Slayer Tower", false, "banshees", "banshee");
		route("Bats", "Silvarea, north of the Digsite", true, "bats", "bat");
		route("Bears", "South-west of the Legends' Guild", true, "bears", "bear");
		route("Birds", "West of the Champions' Guild", true, "birds", "bird");
		route("Cave bugs", "Dorgesh-Kaan South Dungeon", false, "cave bugs", "cave bug");
		route("Cave crawlers", "Fremennik Slayer Dungeon", false, "cave crawlers", "cave crawler");
		route("Cave slime", "Dorgesh-Kaan South Dungeon", false, "cave slime", "cave slimes");
		route("Cows", "Lumbridge cow field", true, "cows", "cow");
		route("Crawling Hands", "Slayer Tower", false, "crawling hands", "crawling hand");
		route("Dogs", "East of Sophanem", true, "dogs", "dog");
		route("Dwarves", "White Wolf Mountain tunnel pub", false, "dwarves", "dwarf");
		route("Ghosts", "Catacombs of Kourend", false, "ghosts", "ghost");
		route("Goblins", "East of Lumbridge", true, "goblins", "goblin");
		route("Icefiends", "Ice Mountain", true, "icefiends", "icefiend");
		route("Kalphites", "Kalphite Cave - workers", true, "kalphites", "kalphite");
		route("Lizards", "North of Nardah fairy ring", true, "lizards", "lizard", "desert lizards", "desert lizard");
		route("Minotaurs", "Stronghold of Security - first level", false, "minotaurs", "minotaur");
		route("Monkeys", "Ardougne Zoo", false, "monkeys", "monkey");
		route("Rats", "Varrock Sewers", false, "rats", "rat");
		route("Scorpions", "Al Kharid Mine", true, "scorpions", "scorpion");
		route("Skeletons", "Digsite Dungeon", true, "skeletons", "skeleton");
		route("Spiders", "Outside the H.A.M. Hideout", true, "spiders", "spider");
		route("Wolves", "White Wolf Mountain", true, "wolves", "wolf");
		route("Zombies", "Alice's farm west of the Ectofuntus", true, "zombies", "zombie");
	}

	private TuraelSpeedProfiles()
	{
	}

	static SlayerTaskProfile apply(
		SlayerTaskProfile ordinary,
		String taskName)
	{
		Route route = ROUTES.get(NameMatcher.normalize(taskName));
		if (ordinary == null || route == null)
		{
			return ordinary;
		}

		String method = route.cannonRecommended
			? STRATEGY_PREFIX + " - cannon + blowpipe"
			: STRATEGY_PREFIX + " - blowpipe";
		String fallback = route.cannonRecommended
			? STRATEGY_PREFIX + " - cannon + ranged"
			: STRATEGY_PREFIX + " - ranged";
		String rationale = route.cannonRecommended
			? "Targets the Wiki's low-level variant with fast ranged attacks and a dwarf multicannon."
			: "Targets the Wiki's low-level variant with fast ranged attacks.";

		return SlayerTaskProfile.builder()
			.key(ordinary.getKey())
			.displayName(route.displayName)
			.summary("Turael/Aya speed mode: finish the smallest assignment quickly; do not substitute a boss or high-level variant.")
			.protectionAdvice(ordinary.getProtectionAdvice())
			.strategy(GearStrategy.builder()
				.name(method)
				.location(route.location)
				.rationale(rationale)
				.combatStyle(CombatStyle.RANGED)
				.requiredWeapon("toxic blowpipe")
				.preferredItem("toxic blowpipe")
				.build())
			.strategy(GearStrategy.builder()
				.name(fallback)
				.location(route.location)
				.rationale(rationale + " Uses the best compatible ranged weapon owned when no blowpipe is available.")
				.combatStyle(CombatStyle.RANGED)
				.preferredItem("toxic blowpipe")
				.build())
			.build();
	}

	static boolean supports(String taskName)
	{
		return taskName != null && ROUTES.containsKey(NameMatcher.normalize(taskName));
	}

	static boolean isSpeedStrategy(GearStrategy strategy)
	{
		return strategy != null
			&& NameMatcher.normalize(strategy.getName())
				.startsWith(NameMatcher.normalize(STRATEGY_PREFIX));
	}

	static int supportedTaskCount()
	{
		return (int) ROUTES.values().stream().distinct().count();
	}

	private static void route(
		String displayName,
		String location,
		boolean cannonRecommended,
		String... aliases)
	{
		Route route = new Route(displayName, location, cannonRecommended);
		for (String alias : aliases)
		{
			ROUTES.put(NameMatcher.normalize(alias), route);
		}
	}

	private static final class Route
	{
		private final String displayName;
		private final String location;
		private final boolean cannonRecommended;

		private Route(
			String displayName,
			String location,
			boolean cannonRecommended)
		{
			this.displayName = displayName;
			this.location = location;
			this.cannonRecommended = cannonRecommended;
		}
	}
}
