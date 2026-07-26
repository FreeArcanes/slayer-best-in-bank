package com.freearcanes.slayergear;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Location-aware cannon routes verified against the OSRS Wiki Slayer-task
 * guidance. A task appears here when there is a verified practical Slayer route
 * where a dwarf multicannon can be used. Cannonability is location-sensitive:
 * a task may have both cannonable and prohibited areas, so this catalog is about
 * viable task routes rather than claiming every spawn for the task accepts a cannon.
 */
final class CannonTaskCatalog
{
	private static final Map<String, CannonRoute> ROUTES = new LinkedHashMap<>();

	static
	{
		// Fast low/mid-level Slayer and point-boosting routes.
		route("Bats", "Limestone Mine", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "bats", "bat");
		route("Bears", "Ardougne Mine", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "bears", "bear");
		route("Birds", "Farmer Fred's chicken pen / Port Sarim", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "birds", "bird");
		route("Catablepon", "Stronghold of Security", CombatStyle.MAGIC, AttackType.BALANCED,
			"Cannon can speed the task while Magic exploits their low Magic defence.", "catablepon");
		route("Cows", "Lumbridge cow field", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "cows", "cow");
		route("Dogs", "Kharidian Desert - jackals", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "dogs", "dog");
		route("Dwarves", "White Wolf Tunnel", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "dwarves", "dwarf");
		route("Flesh crawlers", "Stronghold of Security", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon accelerates this otherwise simple melee task.", "flesh crawlers", "flesh crawler");
		route("Goblins", "Lumbridge / Stronghold of Security", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "goblins", "goblin");
		route("Hill giants", "Giants' Pit", CombatStyle.MELEE, AttackType.SLASH,
			"Use the cannon while safespotting to tag multiple giants.", "hill giants", "hill giant");
		route("Hobgoblins", "Hobgoblin Peninsula", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon-supported single-way route for fast task completion.", "hobgoblins", "hobgoblin");
		route("Ice warriors", "Asgarnian Ice Dungeon", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon-supported single-way route for fast task completion.", "ice warriors", "ice warrior");
		route("Icefiends", "Western Ice Mountain cannon tiles", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "icefiends", "icefiend");
		route("Kalphites", "Kalphite Cave / Kalphite Lair", CombatStyle.MELEE, AttackType.STAB,
			"High-throughput multicombat task where cannon damage greatly improves XP.", "kalphites", "kalphite");
		route("Lizards", "Karuulm Slayer Dungeon - sulphur lizards", CombatStyle.MELEE, AttackType.SLASH,
			"Dense cannonable sulphur-lizard route; avoids desert-lizard finishing items.",
			"lizards", "lizard", "desert lizards", "desert lizard");
		route("Minotaurs", "Stronghold of Security", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "minotaurs", "minotaur");
		route("Ogres", "Combat Training Camp / Feldip Hills", CombatStyle.MELEE, AttackType.SLASH,
			"Ogres are a classic low-risk multicannon target and a fast Vannaka-style assignment.", "ogres", "ogre");
		route("Crabs", "Rock Crabs", CombatStyle.RANGED, AttackType.BALANCED,
			"Use Rock Crabs for the cannon route; Sand Crabs cannot be targeted by a dwarf multicannon.",
			"crabs", "crab", "rock crabs", "rock crab");
		route("Ice giants", "Asgarnian Ice Dungeon", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon-assisted route in the Asgarnian Ice Dungeon for faster assignment completion.", "ice giants", "ice giant");
		route("Monkeys", "Karamja / Ardougne Zoo / Ape Atoll Dungeon", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "monkeys", "monkey");
		route("Pyrefiends", "Isle of Souls burnt forest", CombatStyle.MELEE, AttackType.SLASH,
			"Use the Isle of Souls route because the Fremennik Slayer Dungeon does not allow a cannon.",
			"pyrefiends", "pyrefiend");
		route("Rats", "West of Lumbridge / Varrock Sewers", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "rats", "rat");
		route("Scorpions", "Al Kharid mine / Legends' Guild basement", CombatStyle.MELEE, AttackType.CRUSH,
			"Fast cannon-assisted completion route.", "scorpions", "scorpion");
		route("Skeletons", "Edgeville Dungeon / Digsite Zarosian Temple", CombatStyle.MELEE, AttackType.CRUSH,
			"Fast cannon-assisted completion route.", "skeletons", "skeleton");
		route("Spiders", "Lumbridge / Wilderness spider routes", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "spiders", "spider");
		route("Wolves", "Stronghold of Security / White Wolf Mountain", CombatStyle.MELEE, AttackType.SLASH,
			"Fast cannon-assisted completion route.", "wolves", "wolf");
		route("Zombies", "West of Ectofuntus / Graveyard of Shadows", CombatStyle.MELEE, AttackType.CRUSH,
			"Fast cannon-assisted completion route.", "zombies", "zombie");
		route("Ghosts", "Taverley Dungeon / west of Ectofuntus", CombatStyle.MELEE, AttackType.CRUSH,
			"Fast cannon route using low-defence ghost variants; Salve bonuses remain useful.", "ghosts", "ghost");
		routeAt("Shades", "Sepulchre of Death / Mort'ton outskirts", CombatStyle.MELEE, AttackType.CRUSH,
			"Use the Sepulchre of Death or place the cannon outside Mort'ton within Loar shade wander range; the Catacombs of Kourend and Shade Catacombs are not cannon routes.",
			new String[] {"sepulchre of death", "stronghold of security", "mort ton"}, "shades", "shade");
		route("Vampyres", "West of Burgh de Rott", CombatStyle.MELEE, AttackType.SLASH,
			"Cannonable low-level Vampyre route; Vyrewatch still require the appropriate vampyre weapon.",
			"vampyres", "vampyre", "vampires", "vampire");

		route("Cave bugs", "Lumbridge Swamp Caves", CombatStyle.MELEE, AttackType.SLASH,
			"The Lumbridge Swamp Caves permit a dwarf multicannon and contain cave bugs.",
			"cave bugs", "cave bug");
		route("Cave crawlers", "Lumbridge Swamp Caves", CombatStyle.MELEE, AttackType.SLASH,
			"Use the Lumbridge Swamp Caves cannon route rather than the Fremennik Slayer Dungeon.",
			"cave crawlers", "cave crawler");
		route("Cave slimes", "Lumbridge Swamp Caves", CombatStyle.MELEE, AttackType.SLASH,
			"The Lumbridge Swamp Caves permit a dwarf multicannon and contain cave slimes.",
			"cave slimes", "cave slime");
		route("Rockslugs", "Lumbridge Swamp Caves", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon support is available in the Lumbridge Swamp Caves; bags of salt are still required to finish rockslugs.",
			"rockslugs", "rockslug");
		route("Crocodiles", "River Elid south of Pollnivneach", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon support speeds the open-desert crocodile assignment.",
			"crocodiles", "crocodile");
		route("Fever spiders", "Braindeath Island brewery basement", CombatStyle.RANGED, AttackType.BALANCED,
			"Cannon support is a practical fast-task option; keep Slayer-glove protection in mind when taking hits.",
			"fever spiders", "fever spider");
		route("Harpie bug swarms", "Karamja north-east of Tai Bwo Wannai", CombatStyle.MAGIC, AttackType.BALANCED,
			"Cannon can gather/tag the swarms while the player's main combat method finishes them; a lit bug lantern remains required.",
			"harpie bug swarms", "harpie bug swarm");
		route("Werewolves", "Canifis", CombatStyle.MELEE, AttackType.SLASH,
			"Canifis provides an open cannon-assisted route for werewolf assignments.",
			"werewolves", "werewolf");

		// Higher-level / efficiency routes. Locations intentionally avoid areas
		// such as the Catacombs where cannon placement is prohibited.
		routeAt("Abyssal demons", "Wilderness Slayer Cave", CombatStyle.MAGIC, AttackType.BALANCED,
			"The Wilderness Slayer Cave is cannonable and the cannon greatly speeds tagging/completion; Catacombs and Slayer Tower routes do not use one.",
			new String[] {"wilderness slayer cave"}, "abyssal demons", "abyssal demon");
		route("Aberrant spectres", "Stronghold Slayer Cave / Deepfin Mine", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon-assisted route with mandatory nose-peg/Slayer-helmet protection.",
			"aberrant spectres", "aberrant spectre");
		route("Ankou", "Stronghold Slayer Cave / Wilderness Slayer Cave", CombatStyle.MELEE, AttackType.SLASH,
			"Use a cannonable cave when prioritizing task speed over Catacombs AoE.", "ankou", "ankous");
		route("Araxytes", "Morytania Spider Cave", CombatStyle.RANGED, AttackType.BALANCED,
			"Cannon plus Venator is a high-throughput normal-Araxyte method.", "araxytes", "araxyte");
		route("Black dragons", "Taverley Dungeon / Wilderness Slayer Cave / other cannonable dragon lairs", CombatStyle.RANGED, AttackType.BALANCED,
			"Several normal/baby Black dragon locations allow cannon support; King Black Dragon and Catacombs routes do not.",
			"black dragons", "black dragon");
		route("Blue dragons", "Taverley Dungeon", CombatStyle.RANGED, AttackType.BALANCED,
			"Use a cannonable Taverley route when prioritizing assignment speed; Vorkath is a separate boss method.",
			"blue dragons", "blue dragon");
		route("Red dragons", "Brimhaven Dungeon", CombatStyle.RANGED, AttackType.BALANCED,
			"Brimhaven Dungeon permits dwarf multicannon use, giving Red dragon assignments a cannon-assisted route.",
			"red dragons", "red dragon");
		route("Black demons", "Chasm of Fire / Taverley Dungeon / Wilderness Slayer Cave", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon-supported demonbane route; do not use the Catacombs when cannoning.", "black demons", "black demon");
		routeAt("Bloodveld", "Meiyerditch Laboratory / Stronghold Slayer Cave / Iorwerth Dungeon", CombatStyle.RANGED, AttackType.BALANCED,
			"Cannon-supported route; mutated Bloodvelds pair especially well with multi-target damage.",
			new String[] {"meiyerditch", "stronghold slayer cave", "iorwerth dungeon"}, "bloodveld", "bloodvelds");
		route("Cave horrors", "Mos Le'Harmless Caves", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon increases kills per hour while the witchwood-icon safety requirement remains enforced.", "cave horrors", "cave horror");
		routeAt("Dagannoth", "Lighthouse / Jormungand's Prison / Waterbirth Island Dungeon", CombatStyle.MELEE, AttackType.SLASH,
			"Classic high-throughput cannon Slayer routes; Jormungand's Prison, the Lighthouse, and Waterbirth Island Dungeon all permit cannon use, while the Catacombs do not.",
			new String[] {"lighthouse", "jormungand", "waterbirth island dungeon", "waterbirth dungeon"}, "dagannoth", "dagannoths");
		route("Dark beasts", "Iorwerth Dungeon / Mourner Tunnels", CombatStyle.MELEE, AttackType.SLASH,
			"Both standard dark-beast locations permit a dwarf multicannon.", "dark beasts", "dark beast");
		routeAt("Dust devils", "Smoke Dungeon / Wilderness Slayer Cave", CombatStyle.MAGIC, AttackType.BALANCED,
			"Smoke Dungeon and Wilderness Slayer Cave provide cannon routes; the Catacombs do not. In Smoke Dungeon, use a cannon-permitted section.",
			new String[] {"smoke dungeon", "wilderness slayer cave"}, "dust devils", "dust devil");
		route("Elves", "Iorwerth Dungeon", CombatStyle.MELEE, AttackType.SLASH,
			"Iorwerth Dungeon provides a practical cannon-assisted elf assignment route.", "elves", "elf");
		routeAt("Fire giants", "Giants' Den / Stronghold Slayer Cave / Waterfall Dungeon", CombatStyle.MELEE, AttackType.SLASH,
			"These common Fire giant routes are cannonable; the Catacombs are the major exception.",
			new String[] {"giants den", "stronghold slayer cave", "waterfall dungeon", "brimhaven dungeon", "smoke dungeon"}, "fire giants", "fire giant");
		route("Greater demons", "Chasm of Fire / Wilderness Slayer Cave", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon-supported demonbane route; use a cannonable location rather than the Catacombs.", "greater demons", "greater demon");
		route("Gryphons", "Gryphon task area", CombatStyle.MELEE, AttackType.CRUSH,
			"Cannon support improves task throughput alongside the recommended heavy melee setup.", "gryphons", "gryphon");
		route("Hellhounds", "Stronghold Slayer Cave / Taverley Dungeon", CombatStyle.MELEE, AttackType.SLASH,
			"Straightforward cannon route; the Catacombs do not allow cannon placement.", "hellhounds", "hellhound");
		route("Jellies", "Wilderness Slayer Cave", CombatStyle.MELEE, AttackType.SLASH,
			"Location-specific cannon route when not using the Catacombs Ancient-Magicks method.", "jellies", "jelly");
		route("Lesser demons", "Chasm of Fire / Karamja Dungeon / Wilderness Slayer Cave", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon-supported demonbane route; the Catacombs do not allow cannons.", "lesser demons", "lesser demon");
		routeAt("Lizardmen", "Lizardman Canyon", CombatStyle.RANGED, AttackType.BALANCED,
			"Normal lizardmen are commonly cannoned in the canyon; the caves and temple prohibit cannon placement.",
			new String[] {"lizardman canyon", "canyon"}, "lizardmen", "lizardman");
		routeAt("Metal dragons", "Brimhaven Dungeon / Isle of Souls Dungeon / Lithkren Vault", CombatStyle.MELEE, AttackType.STAB,
			"All metal dragons except mithril dragons have cannonable routes. Use bronze/iron/steel in Brimhaven, iron dragons in the Isle of Souls Dungeon, or adamant/rune dragons in Lithkren Vault.",
			new String[] {"brimhaven dungeon", "isle of souls dungeon", "lithkren vault"}, "metal dragons");
		routeAt("Bronze dragons", "Brimhaven Dungeon", CombatStyle.MELEE, AttackType.STAB,
			"Bronze dragons can be cannoned in Brimhaven Dungeon.",
			new String[] {"brimhaven dungeon"}, "bronze dragons", "bronze dragon");
		routeAt("Iron dragons", "Brimhaven Dungeon / Isle of Souls Dungeon", CombatStyle.MELEE, AttackType.STAB,
			"Iron dragons can be cannoned in Brimhaven Dungeon or the Isle of Souls Dungeon.",
			new String[] {"brimhaven dungeon", "isle of souls dungeon"}, "iron dragons", "iron dragon");
		routeAt("Steel dragons", "Brimhaven Dungeon", CombatStyle.MELEE, AttackType.STAB,
			"Steel dragons can be cannoned in Brimhaven Dungeon.",
			new String[] {"brimhaven dungeon"}, "steel dragons", "steel dragon");
		routeAt("Adamant dragons", "Lithkren Vault", CombatStyle.MELEE, AttackType.STAB,
			"Adamant dragons in Lithkren Vault are cannonable.",
			new String[] {"lithkren vault"}, "adamant dragons", "adamant dragon");
		routeAt("Rune dragons", "Lithkren Vault", CombatStyle.MELEE, AttackType.STAB,
			"Rune dragons in Lithkren Vault are cannonable.",
			new String[] {"lithkren vault"}, "rune dragons", "rune dragon");
		route("Moss giants", "Brimhaven Dungeon / Iorwerth Dungeon", CombatStyle.MELEE, AttackType.SLASH,
			"Use a cannonable location rather than the Catacombs for faster completion.", "moss giants", "moss giant");
		routeAt("Nechryael", "Iorwerth Dungeon / Charred Dungeon / Wilderness Slayer Cave", CombatStyle.MELEE, AttackType.SLASH,
			"Location-specific cannon route; Slayer Tower and Catacombs setups do not use a cannon.",
			new String[] {"iorwerth dungeon", "charred dungeon", "wilderness slayer cave"}, "nechryael", "nechryaels");
		route("Smoke devils", "Smoke Devil Dungeon", CombatStyle.MAGIC, AttackType.BALANCED,
			"Use the cannon to lure/group smoke devils, then Burst/Barrage the stack for high Slayer XP.",
			"smoke devils", "smoke devil");
		route("Suqahs", "Lunar Isle", CombatStyle.MELEE, AttackType.SLASH,
			"Classic cannon task where the extra hits dramatically improve task speed.", "suqahs", "suqah");
		route("Trolls", "Death Plateau / Trollheim cannon route", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon is strongly recommended for fast troll assignments.", "trolls", "troll");
		route("Warped creatures", "Poison Waste Dungeon", CombatStyle.MELEE, AttackType.CRUSH,
			"Cannon support improves throughput against warped terrorbirds/tortoises.", "warped creatures", "warped creature");
		routeAt("Waterfiends", "Iorwerth Dungeon", CombatStyle.MELEE, AttackType.CRUSH,
			"Iorwerth Dungeon is the cannonable Waterfiend route; Ancient Cavern and Kraken Cove are not.",
			new String[] {"iorwerth dungeon"}, "waterfiends", "waterfiend");
		routeAt("Wyrms", "Neypotzli - wyrmlings", CombatStyle.RANGED, AttackType.BALANCED,
			"Use Wyrmlings in Neypotzli for the cannon route; normal Karuulm Wyrms and Lava Strykewyrms are not cannonable.",
			new String[] {"neypotzli"}, "wyrms", "wyrm");
		route("Green dragons", "Wilderness Slayer Cave", CombatStyle.RANGED, AttackType.BALANCED,
			"Wilderness cannon route; use low-risk gear and keep player-killer escape options in mind.",
			"green dragons", "green dragon");
		route("Scabarites", "Sophanem Dungeon - cavern level", CombatStyle.MELEE, AttackType.STAB,
			"The cavern level is multicombat and cannonable; the maze level and Uzer Mastaba are not.",
			"scabarites", "scabarite", "minions of scabaras", "minion of scabaras");
		route("Skeletal wyverns", "Asgarnian Ice Dungeon - lower cave", CombatStyle.MELEE, AttackType.CRUSH,
			"Cannons are allowed only in the lower wyvern cave and can speed melee kills; the upper Slayer-only area is a no-cannon zone.",
			"skeletal wyverns", "skeletal wyvern");
		routeAt("Drakes", "Karuulm Slayer Dungeon - Drake area", CombatStyle.MELEE, AttackType.STAB,
			"The current Slayer guide recommends a dwarf multicannon to speed Drake kills slightly.",
			new String[] {"karuulm slayer dungeon", "karuulm"}, "drakes", "drake");
		routeAt("Hydras", "Karuulm Slayer Dungeon - normal Hydra area", CombatStyle.RANGED, AttackType.BALANCED,
			"Normal Hydra areas permit a cannon; this does not apply to the Alchemical Hydra boss room.",
			new String[] {"karuulm slayer dungeon", "karuulm"}, "hydras", "hydra");
		route("Custodian stalkers", "Stalker Den - south-west multicombat area", CombatStyle.RANGED, AttackType.BALANCED,
			"The south-west Stalker Den is multicombat and explicitly permits cannons; Venator pairs well with the route.",
			"custodian stalkers", "custodian stalker", "juvenile custodian stalkers", "juvenile custodian stalker",
			"mature custodian stalkers", "mature custodian stalker", "elder custodian stalkers", "elder custodian stalker");
		routeAt("Lesser Nagua", "Neypotzli - Sulphur Nagua", CombatStyle.MELEE, AttackType.CRUSH,
			"Neypotzli permits a dwarven cannon for Slayer tasks; only offer this route when the assignment is compatible with Neypotzli.",
			new String[] {"neypotzli"}, "lesser nagua", "lesser naguas", "nagua");
		route("Brine rats", "Brine Rat Cavern", CombatStyle.MELEE, AttackType.SLASH,
			"Cannon-assisted Brine Rat Cavern route for faster assignment completion.", "brine rats", "brine rat");
	}

	private CannonTaskCatalog()
	{
	}

	static Optional<CannonRoute> find(String taskName)
	{
		return Optional.ofNullable(ROUTES.get(normalize(taskName)));
	}

	static Optional<CannonRoute> find(String taskName, String assignedLocation)
	{
		Optional<CannonRoute> route = find(taskName);
		if (!route.isPresent() || !route.get().supportsAssignedLocation(assignedLocation)) return Optional.empty();
		return route;
	}

	/**
	 * Konar assignments are location-locked. Do not advertise a cannon method when
	 * RuneLite reports one of the well-known no-cannon areas. A blank location
	 * means the assignment is not location-locked, so the catalog's suggested
	 * cannon route remains valid.
	 */
	static boolean isCannonAllowedAtAssignedLocation(String assignedLocation)
	{
		String location = normalize(assignedLocation);
		if (location.isEmpty()) return true;
		return !(location.contains("catacombs of kourend")
			|| location.contains("slayer tower")
			|| location.contains("fremennik slayer dungeon")
			|| location.contains("god wars dungeon")
			|| location.contains("wyvern cave")
			|| location.contains("revenant cave")
			|| location.contains("forthos dungeon")
			|| location.contains("killerwatt plane")
			|| location.contains("kraken cove")
			|| location.contains("lair of tarn")
			|| location.contains("mogre camp")
			|| location.contains("lizardman caves")
			|| location.contains("lizardman temple")
			|| location.contains("mor ul rek")
			|| location.contains("fight cave")
			|| location.contains("fight pit"));
	}

	static int routeCount()
	{
		return (int) ROUTES.values().stream().distinct().count();
	}

	private static void route(String displayName, String location, CombatStyle style, AttackType attackType,
		String rationale, String... aliases)
	{
		addRoute(new CannonRoute(displayName, location, style, attackType, rationale, new String[0]), aliases);
	}

	private static void routeAt(String displayName, String location, CombatStyle style, AttackType attackType,
		String rationale, String[] assignedLocationTokens, String... aliases)
	{
		addRoute(new CannonRoute(displayName, location, style, attackType, rationale, assignedLocationTokens), aliases);
	}

	private static void addRoute(CannonRoute route, String... aliases)
	{
		for (String alias : aliases)
		{
			ROUTES.put(normalize(alias), route);
		}
	}

	private static String normalize(String value)
	{
		if (value == null) return "";
		String normalized = value.toLowerCase(Locale.ENGLISH).trim();
		if (normalized.startsWith("the ")) normalized = normalized.substring(4);
		return normalized.replaceAll("[^a-z0-9]+", " ").trim();
	}

	static final class CannonRoute
	{
		private final String displayName;
		private final String location;
		private final CombatStyle combatStyle;
		private final AttackType attackType;
		private final String rationale;
		private final String[] assignedLocationTokens;

		private CannonRoute(String displayName, String location, CombatStyle combatStyle, AttackType attackType,
			String rationale, String[] assignedLocationTokens)
		{
			this.displayName = displayName;
			this.location = location;
			this.combatStyle = combatStyle;
			this.attackType = attackType;
			this.rationale = rationale;
			this.assignedLocationTokens = assignedLocationTokens == null ? new String[0] : assignedLocationTokens.clone();
		}

		private boolean supportsAssignedLocation(String assignedLocation)
		{
			if (!isCannonAllowedAtAssignedLocation(assignedLocation)) return false;
			String locationName = normalize(assignedLocation);
			if (locationName.isEmpty() || assignedLocationTokens.length == 0) return true;
			for (String token : assignedLocationTokens)
			{
				if (locationName.contains(normalize(token))) return true;
			}
			return false;
		}

		String getDisplayName() { return displayName; }
		String getLocation() { return location; }
		CombatStyle getCombatStyle() { return combatStyle; }
		AttackType getAttackType() { return attackType; }
		String getRationale() { return rationale; }
	}
}