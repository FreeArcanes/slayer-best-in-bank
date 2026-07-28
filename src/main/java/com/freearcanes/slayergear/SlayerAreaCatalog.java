package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RuneLite's authoritative Slayer-area helper names.
 *
 * <p>The names mirror DB table 115 (SlayerArea). Keeping the aliases, travel
 * family, and cannon policy together prevents the travel and strategy catalogs
 * from silently disagreeing about an assigned area.</p>
 */
final class SlayerAreaCatalog
{
	enum TravelFamily
	{
		NONE,
		VARLAMORE,
		KOUREND,
		FAIRY_RING,
		DUELING,
		KARUULM,
		SLAYER_RING,
		GAMES_NECKLACE,
		KARAMJA,
		ARDOUGNE,
		TROLLHEIM,
		FREMENNIK,
		ELVEN,
		DRAKAN,
		DIGSITE,
		MYTHS_GUILD,
		SKULL_SCEPTRE,
		WATCHTOWER,
		DORGESH_KAAN
	}

	enum CannonPolicy
	{
		ALLOWED,
		DENIED,
		UNKNOWN
	}

	private static final Map<String, Area> BY_NAME = new LinkedHashMap<>();

	static
	{
		area("Crypt of Tonali", TravelFamily.VARLAMORE, CannonPolicy.UNKNOWN);
		area("Catacombs of Kourend", TravelFamily.KOUREND, CannonPolicy.DENIED, "Catacombs");
		area("Smoke Dungeon", TravelFamily.FAIRY_RING, CannonPolicy.ALLOWED);
		area("Smoke Devil Dungeon", TravelFamily.DUELING, CannonPolicy.ALLOWED);
		area("Karuulm Slayer Dungeon", TravelFamily.KARUULM, CannonPolicy.ALLOWED, "Brimstone Dungeon");
		area("Stronghold Slayer Dungeon", TravelFamily.SLAYER_RING, CannonPolicy.ALLOWED,
			"Stronghold Slayer Cave");
		area("Waterfall Dungeon", TravelFamily.GAMES_NECKLACE, CannonPolicy.ALLOWED);
		area("Brimhaven Dungeon", TravelFamily.KARAMJA, CannonPolicy.ALLOWED);
		area("Isle of Souls", TravelFamily.DUELING, CannonPolicy.UNKNOWN);
		area("Giants' Den", TravelFamily.KOUREND, CannonPolicy.ALLOWED, "Giants Den");
		area("Chasm of Fire", TravelFamily.KOUREND, CannonPolicy.ALLOWED);
		area("Taverley Dungeon", TravelFamily.GAMES_NECKLACE, CannonPolicy.ALLOWED);
		area("Witchaven Dungeon", TravelFamily.ARDOUGNE, CannonPolicy.UNKNOWN, "Witchhaven Dungeon");
		area("Slayer Tower", TravelFamily.SLAYER_RING, CannonPolicy.DENIED);
		area("God Wars Dungeon", TravelFamily.TROLLHEIM, CannonPolicy.DENIED);
		area("Kalphite Lair", TravelFamily.FAIRY_RING, CannonPolicy.ALLOWED);
		area("task-only Kalphite Cave", TravelFamily.FAIRY_RING, CannonPolicy.ALLOWED,
			"Kalphite Cave", "Kalphite Slayer Cave");
		area("Kraken Cove", TravelFamily.FAIRY_RING, CannonPolicy.DENIED);
		area("in the Lighthouse", TravelFamily.GAMES_NECKLACE, CannonPolicy.ALLOWED, "Lighthouse");
		area("Waterbirth Island", TravelFamily.FREMENNIK, CannonPolicy.ALLOWED,
			"Waterbirth Island Dungeon", "Waterbirth Dungeon");
		area("Lizardman Canyon", TravelFamily.KOUREND, CannonPolicy.ALLOWED);
		area("Molch", TravelFamily.KOUREND, CannonPolicy.UNKNOWN);
		area("Lizardman Settlement", TravelFamily.KOUREND, CannonPolicy.UNKNOWN);
		area("Death Plateau", TravelFamily.GAMES_NECKLACE, CannonPolicy.ALLOWED);
		area("Troll Stronghold", TravelFamily.TROLLHEIM, CannonPolicy.UNKNOWN);
		area("Keldagrim", TravelFamily.FREMENNIK, CannonPolicy.UNKNOWN);
		area("South of Mount Quidamortem", TravelFamily.KOUREND, CannonPolicy.ALLOWED,
			"Mount Quidamortem");
		area("Fremennik Isles", TravelFamily.FREMENNIK, CannonPolicy.ALLOWED, "Jatizso", "Neitiznot");
		area("Fremennik Slayer Dungeon", TravelFamily.SLAYER_RING, CannonPolicy.DENIED,
			"Rellekka Slayer Caves");
		area("Myths' Guild Dungeon", TravelFamily.MYTHS_GUILD, CannonPolicy.UNKNOWN, "Myths Guild");
		area("Mourner Tunnels", TravelFamily.ELVEN, CannonPolicy.ALLOWED, "Mourner Tunnel");
		area("Lithkren Vault", TravelFamily.DIGSITE, CannonPolicy.ALLOWED);
		area("Ancient Cavern", TravelFamily.GAMES_NECKLACE, CannonPolicy.DENIED);
		area("Stronghold of Security", TravelFamily.SKULL_SCEPTRE, CannonPolicy.ALLOWED);
		area("Fossil Island", TravelFamily.DIGSITE, CannonPolicy.UNKNOWN, "Fossil Island Wyvern Cave");
		area("Ogre Enclave", TravelFamily.WATCHTOWER, CannonPolicy.UNKNOWN);
		area("Brine Rat Cavern", TravelFamily.FREMENNIK, CannonPolicy.ALLOWED);
		area("Zanaris", TravelFamily.FAIRY_RING, CannonPolicy.UNKNOWN);
		area("Evil Chicken's Lair", TravelFamily.FAIRY_RING, CannonPolicy.UNKNOWN);
		area("The Abyss", TravelFamily.FAIRY_RING, CannonPolicy.UNKNOWN, "Abyss");
		area("Kebos Swamp", TravelFamily.KOUREND, CannonPolicy.UNKNOWN);
		area("The Battlefront", TravelFamily.KOUREND, CannonPolicy.UNKNOWN, "Battlefront");
		area("Forthos Dungeon", TravelFamily.KOUREND, CannonPolicy.DENIED, "Hosidius Dungeon");
		area("Iorwerth Dungeon", TravelFamily.ELVEN, CannonPolicy.ALLOWED);
		area("Jormungand's Prison", TravelFamily.FREMENNIK, CannonPolicy.ALLOWED,
			"Island of Stone", "Islands of Stone");
		area("Darkmeyer", TravelFamily.DRAKAN, CannonPolicy.UNKNOWN);
		area("Slepe", TravelFamily.DRAKAN, CannonPolicy.UNKNOWN);
		area("Meiyerditch Laboratories", TravelFamily.DRAKAN, CannonPolicy.ALLOWED,
			"Meiyerditch Laboratory");
		area("Poison Waste Dungeon", TravelFamily.ELVEN, CannonPolicy.ALLOWED);
		area("Neypotzli", TravelFamily.VARLAMORE, CannonPolicy.ALLOWED,
			"Perilous Moons Dungeon");
		area("Tapoyauik", TravelFamily.VARLAMORE, CannonPolicy.UNKNOWN,
			"Ruins of Tapoyauik");
		area("Asgarnian Ice Dungeon", TravelFamily.GAMES_NECKLACE, CannonPolicy.ALLOWED,
			"Ice Dungeon");
		area("Great Conch", TravelFamily.NONE, CannonPolicy.ALLOWED, "The Great Conch");
		area("Charred Dungeon", TravelFamily.NONE, CannonPolicy.ALLOWED,
			"Charred Island Dungeon");
		area("Vampyrium", TravelFamily.DRAKAN, CannonPolicy.UNKNOWN);
	}

	private SlayerAreaCatalog()
	{
	}

	private static void area(
		String helperName,
		TravelFamily travelFamily,
		CannonPolicy cannonPolicy,
		String... aliases)
	{
		Area area = new Area(helperName, travelFamily, cannonPolicy, aliases);
		index(area, helperName);
		for (String alias : aliases)
		{
			index(area, alias);
		}
	}

	private static void index(Area area, String name)
	{
		BY_NAME.put(NameMatcher.normalize(name), area);
	}

	static Optional<Area> find(String location)
	{
		String normalized = NameMatcher.normalize(location);
		if (normalized.isEmpty())
		{
			return Optional.empty();
		}
		Area exact = BY_NAME.get(normalized);
		if (exact != null)
		{
			return Optional.of(exact);
		}
		for (Map.Entry<String, Area> entry : BY_NAME.entrySet())
		{
			if (normalized.contains(entry.getKey()))
			{
				return Optional.of(entry.getValue());
			}
		}
		return Optional.empty();
	}

	static List<Area> areas()
	{
		Map<String, Area> unique = new LinkedHashMap<>();
		for (Area area : BY_NAME.values())
		{
			unique.put(area.helperName, area);
		}
		return Collections.unmodifiableList(new ArrayList<>(unique.values()));
	}

	static final class Area
	{
		private final String helperName;
		private final TravelFamily travelFamily;
		private final CannonPolicy cannonPolicy;
		private final String[] aliases;

		private Area(
			String helperName,
			TravelFamily travelFamily,
			CannonPolicy cannonPolicy,
			String[] aliases)
		{
			this.helperName = helperName;
			this.travelFamily = travelFamily;
			this.cannonPolicy = cannonPolicy;
			this.aliases = aliases.clone();
		}

		String getHelperName() { return helperName; }
		TravelFamily getTravelFamily() { return travelFamily; }
		CannonPolicy getCannonPolicy() { return cannonPolicy; }
		String[] getAliases() { return aliases.clone(); }
	}
}
