package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts RuneLite's assigned Slayer area into optional carried travel items.
 * The assigned area is authoritative; a curated strategy location is used only
 * when RuneLite has no location-locked assignment.
 */
final class TravelItemAdvisor
{
	private TravelItemAdvisor()
	{
	}

	static List<TravelRule> recommend(
		String assignedLocation,
		GearStrategy strategy,
		SlayerGearAdvisorConfig config)
	{
		if (config == null || !config.travelSuggestionsEnabled())
		{
			return Collections.emptyList();
		}

		String destination = assignedLocation == null || assignedLocation.trim().isEmpty()
			? strategy == null ? "" : strategy.getLocation()
			: assignedLocation.trim();
		String location = NameMatcher.normalize(destination);
		List<TravelRule> rules = new ArrayList<>();
		addHomeTeleport(rules, config.homeTeleportPreference());

		java.util.Optional<SlayerAreaCatalog.Area> assignedArea =
			SlayerAreaCatalog.find(destination);
		if (assignedArea.isPresent())
		{
			addAreaTravel(rules, assignedArea.get(), destination, config);
			return rules;
		}

		if (contains(location,
			"slayer tower",
			"fremennik slayer dungeon",
			"rellekka slayer caves",
			"stronghold slayer cave"))
		{
			rules.add(rule(
				"Slayer ring",
				"Fast access to this Slayer destination",
				slayerRingNames(config.slayerRingPreference())));
		}
		else if (contains(location, "catacombs", "chasm of fire", "kourend"))
		{
			rules.add(rule(
				"Kourend teleport",
				"Travel toward " + destination,
				kourendNames(config.kourendTeleportPreference())));
		}
		else if (contains(location, "karuulm"))
		{
			rules.add(rule(
				"Mount Karuulm travel",
				"Travel toward the Karuulm Slayer Dungeon",
				merge(
					new String[]{"rada's blessing"},
					fairyRingNames(config.fairyRingPreference()))));
		}
		else if (contains(location, "fossil island"))
		{
			rules.add(rule(
				"Digsite pendant",
				"Travel to Fossil Island",
				"digsite pendant"));
		}
		else if (contains(location,
			"prifddinas",
			"iorwerth",
			"lletya",
			"mourner tunnel"))
		{
			rules.add(rule(
				"Teleport crystal",
				"Travel toward the assigned elven location",
				"eternal teleport crystal",
				"teleport crystal"));
		}
		else if (contains(location,
			"meiyerditch",
			"morytania spider nest",
			"darkmeyer",
			"vampyrium"))
		{
			rules.add(rule(
				"Drakan's medallion",
				"Travel toward the assigned Morytania location",
				"drakan's medallion"));
		}
		else if (contains(location, "god wars", "trollheim"))
		{
			rules.add(spellRoute(
				"Trollheim teleport",
				"Travel toward God Wars Dungeon",
				"trollheim teleport",
				config.spellTeleportPreference()));
		}
		else if (contains(location, "lunar isle"))
		{
			rules.add(spellRoute(
				"Lunar Isle teleport",
				"Travel to Lunar Isle",
				"lunar isle teleport",
				config.spellTeleportPreference()));
		}
		else if (contains(location,
			"taverley",
			"burthorpe",
			"lighthouse",
			"waterfall dungeon",
			"ancient cavern"))
		{
			rules.add(rule(
				"Games necklace",
				"Travel near " + destination,
				"games necklace"));
		}
		else if (contains(location,
			"brimhaven",
			"karamja",
			"harpie",
			"tai bwo wannai"))
		{
			rules.add(rule(
				"Karamja travel",
				"Travel toward the assigned Karamja location",
				"karamja gloves",
				"tai bwo wannai teleport"));
		}
		else if (contains(location,
			"zanaris",
			"killerwatt",
			"kalphite",
			"kharidian desert",
			"smoke dungeon"))
		{
			rules.add(rule(
				"Fairy ring access",
				"Use a nearby fairy-ring route",
				fairyRingNames(config.fairyRingPreference())));
		}
		else if (contains(location, "dorgesh-kaan"))
		{
			rules.add(rule(
				"Dorgesh-kaan sphere",
				"Travel toward Dorgesh-Kaan",
				"dorgesh-kaan sphere"));
		}

		return rules;
	}

	private static void addAreaTravel(
		List<TravelRule> rules,
		SlayerAreaCatalog.Area area,
		String destination,
		SlayerGearAdvisorConfig config)
	{
		switch (area.getTravelFamily())
		{
			case VARLAMORE:
				rules.add(rule("Varlamore travel", "Travel toward " + destination,
					"pendant of ates", "quetzal whistle"));
				break;
			case KOUREND:
				rules.add(rule("Kourend teleport", "Travel toward " + destination,
					kourendNames(config.kourendTeleportPreference())));
				break;
			case FAIRY_RING:
				rules.add(rule("Fairy ring access", "Use a nearby fairy-ring route",
					fairyRingNames(config.fairyRingPreference())));
				break;
			case DUELING:
				rules.add(rule("Ring of dueling", "Travel toward " + destination,
					"ring of dueling"));
				break;
			case KARUULM:
				rules.add(rule("Mount Karuulm travel", "Travel toward the Karuulm Slayer Dungeon",
					merge(new String[]{"rada's blessing"}, fairyRingNames(config.fairyRingPreference()))));
				break;
			case SLAYER_RING:
				rules.add(rule("Slayer ring", "Fast access to this Slayer destination",
					slayerRingNames(config.slayerRingPreference())));
				break;
			case GAMES_NECKLACE:
				rules.add(rule("Games necklace", "Travel near " + destination, "games necklace"));
				break;
			case KARAMJA:
				rules.add(rule("Karamja travel", "Travel toward " + destination,
					"karamja gloves", "tai bwo wannai teleport"));
				break;
			case ARDOUGNE:
				rules.add(spellRoute("Ardougne teleport", "Travel toward " + destination,
					"ardougne teleport", config.spellTeleportPreference()));
				break;
			case TROLLHEIM:
				rules.add(spellRoute("Trollheim teleport", "Travel toward " + destination,
					"trollheim teleport", config.spellTeleportPreference()));
				break;
			case FREMENNIK:
				rules.add(rule("Fremennik travel", "Travel toward " + destination,
					"enchanted lyre", "waterbirth teleport", "games necklace"));
				break;
			case ELVEN:
				rules.add(rule("Teleport crystal", "Travel toward the assigned elven location",
					"eternal teleport crystal", "teleport crystal"));
				break;
			case DRAKAN:
				rules.add(rule("Drakan's medallion", "Travel toward " + destination,
					"drakan's medallion"));
				break;
			case DIGSITE:
				rules.add(rule("Digsite pendant", "Travel toward " + destination,
					"digsite pendant"));
				break;
			case MYTHS_GUILD:
				rules.add(rule("Mythical cape", "Travel toward the Myths' Guild",
					"mythical cape"));
				break;
			case SKULL_SCEPTRE:
				rules.add(rule("Skull sceptre", "Travel to the Stronghold of Security",
					"skull sceptre"));
				break;
			case WATCHTOWER:
				rules.add(spellRoute("Watchtower teleport", "Travel toward the Ogre Enclave",
					"watchtower teleport", config.spellTeleportPreference()));
				break;
			case DORGESH_KAAN:
				rules.add(rule("Dorgesh-kaan sphere", "Travel toward Dorgesh-Kaan",
					"dorgesh-kaan sphere"));
				break;
			case NONE:
			default:
				// Some destinations, such as Sailing islands, have no useful
				// carryable teleport item. Do not invent a packing requirement.
				break;
		}
	}

	private static void addHomeTeleport(
		List<TravelRule> rules,
		HomeTeleportPreference preference)
	{
		HomeTeleportPreference selected = preference == null
			? HomeTeleportPreference.TELEPORT_TO_HOUSE : preference;
		switch (selected)
		{
			case MAX_CAPE:
				rules.add(rule(
					"Max cape",
					"Preferred home and player-owned-house travel",
					"max cape"));
				break;
			case CONSTRUCTION_CAPE:
				rules.add(rule(
					"Construction cape",
					"Preferred home and player-owned-house travel",
					"construct. cape",
					"construction cape"));
				break;
			case RUNES:
				rules.add(rule(
					"Teleport to House runes",
					"Law rune reminder; ensure an Air and Earth rune source is also available",
					"law rune"));
				break;
			case NONE:
				break;
			case TELEPORT_TO_HOUSE:
			default:
				rules.add(rule(
					"Teleport to house",
					"Preferred home and player-owned-house travel",
					"teleport to house"));
				break;
		}
	}

	private static TravelRule spellRoute(
		String fallback,
		String reason,
		String tabletName,
		SpellTeleportPreference preference)
	{
		if (preference == SpellTeleportPreference.MAX_CAPE_FIRST)
		{
			return rule(fallback, reason, "max cape", tabletName, "law rune");
		}
		if (preference == SpellTeleportPreference.RUNES_FIRST)
		{
			return rule(fallback, reason, "law rune", tabletName);
		}
		return rule(fallback, reason, tabletName, "law rune");
	}

	private static String[] slayerRingNames(SlayerRingPreference preference)
	{
		return preference == SlayerRingPreference.CHARGED_FIRST
			? new String[]{"slayer ring (", "eternal slayer ring"}
			: new String[]{"eternal slayer ring", "slayer ring ("};
	}

	private static String[] fairyRingNames(FairyRingPreference preference)
	{
		if (preference == FairyRingPreference.MAX_CAPE_FIRST)
		{
			return new String[]{"max cape", "quest point cape", "lunar staff", "dramen staff"};
		}
		if (preference == FairyRingPreference.LUNAR_STAFF_FIRST)
		{
			return new String[]{"lunar staff", "dramen staff", "quest point cape"};
		}
		if (preference == FairyRingPreference.DRAMEN_STAFF_FIRST)
		{
			return new String[]{"dramen staff", "lunar staff", "quest point cape"};
		}
		return new String[]{"quest point cape", "lunar staff", "dramen staff"};
	}

	private static String[] kourendNames(KourendTeleportPreference preference)
	{
		if (preference == KourendTeleportPreference.MAX_CAPE_FIRST)
		{
			return new String[]{"max cape", "xeric's talisman", "rada's blessing", "kourend castle teleport"};
		}
		if (preference == KourendTeleportPreference.RADAS_BLESSING_FIRST)
		{
			return new String[]{"rada's blessing", "xeric's talisman", "kourend castle teleport"};
		}
		if (preference == KourendTeleportPreference.KOUREND_TELEPORT_FIRST)
		{
			return new String[]{"kourend castle teleport", "xeric's talisman", "rada's blessing"};
		}
		return new String[]{"xeric's talisman", "rada's blessing", "kourend castle teleport"};
	}

	private static String[] merge(String[] first, String[] second)
	{
		String[] result = new String[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private static boolean contains(String location, String... fragments)
	{
		for (String fragment : fragments)
		{
			if (location.contains(NameMatcher.normalize(fragment)))
			{
				return true;
			}
		}
		return false;
	}

	private static TravelRule rule(
		String fallback,
		String reason,
		String... preferredNames)
	{
		return new TravelRule(fallback, reason, preferredNames);
	}

	static final class TravelRule
	{
		private final String fallback;
		private final String reason;
		private final String[] preferredNames;

		private TravelRule(
			String fallback,
			String reason,
			String[] preferredNames)
		{
			this.fallback = fallback;
			this.reason = reason;
			this.preferredNames = preferredNames.clone();
		}

		String getFallback() { return fallback; }
		String getReason() { return reason; }
		String[] getPreferredNames() { return preferredNames.clone(); }
	}
}
