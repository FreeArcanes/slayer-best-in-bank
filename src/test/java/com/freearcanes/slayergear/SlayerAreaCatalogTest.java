package com.freearcanes.slayergear;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SlayerAreaCatalogTest
{
	private static final List<String> RUNELITE_HELPER_NAMES = Arrays.asList(
		"Crypt of Tonali", "Catacombs of Kourend", "Smoke Dungeon", "Smoke Devil Dungeon",
		"Karuulm Slayer Dungeon", "Stronghold Slayer Dungeon", "Waterfall Dungeon",
		"Brimhaven Dungeon", "Isle of Souls", "Giants' Den", "Chasm of Fire",
		"Taverley Dungeon", "Witchaven Dungeon", "Slayer Tower", "God Wars Dungeon",
		"Kalphite Lair", "task-only Kalphite Cave", "Kraken Cove", "in the Lighthouse",
		"Waterbirth Island", "Lizardman Canyon", "Molch", "Lizardman Settlement",
		"Death Plateau", "Troll Stronghold", "Keldagrim", "South of Mount Quidamortem",
		"Fremennik Isles", "Fremennik Slayer Dungeon", "Myths' Guild Dungeon",
		"Mourner Tunnels", "Lithkren Vault", "Ancient Cavern", "Stronghold of Security",
		"Fossil Island", "Ogre Enclave", "Brine Rat Cavern", "Zanaris",
		"Evil Chicken's Lair", "The Abyss", "Kebos Swamp", "The Battlefront",
		"Forthos Dungeon", "Iorwerth Dungeon", "Jormungand's Prison", "Darkmeyer",
		"Slepe", "Meiyerditch Laboratories", "Poison Waste Dungeon", "Neypotzli",
		"Tapoyauik", "Asgarnian Ice Dungeon", "Great Conch", "Charred Dungeon",
		"Vampyrium");

	@Test
	public void catalogExactlyCoversRuneLiteSlayerAreaTable()
	{
		assertEquals(55, SlayerAreaCatalog.areas().size());
		assertEquals(55, RUNELITE_HELPER_NAMES.size());
		for (String helperName : RUNELITE_HELPER_NAMES)
		{
			SlayerAreaCatalog.Area area = SlayerAreaCatalog.find(helperName).orElseThrow();
			assertEquals(helperName, area.getHelperName());
		}
	}

	@Test
	public void historicalAndStrategyAliasesResolveToAuthoritativeNames()
	{
		assertEquals("Karuulm Slayer Dungeon",
			SlayerAreaCatalog.find("Brimstone Dungeon").orElseThrow().getHelperName());
		assertEquals("task-only Kalphite Cave",
			SlayerAreaCatalog.find("Kalphite Slayer Cave").orElseThrow().getHelperName());
		assertEquals("Meiyerditch Laboratories",
			SlayerAreaCatalog.find("Meiyerditch Laboratory").orElseThrow().getHelperName());
		assertEquals("Great Conch",
			SlayerAreaCatalog.find("The Great Conch").orElseThrow().getHelperName());
	}

	@Test
	public void everyCarryableRouteProducesDestinationSpecificTravelAdvice()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig() { };
		for (SlayerAreaCatalog.Area area : SlayerAreaCatalog.areas())
		{
			List<TravelItemAdvisor.TravelRule> rules =
				TravelItemAdvisor.recommend(area.getHelperName(), null, config);
			if (area.getTravelFamily() == SlayerAreaCatalog.TravelFamily.NONE)
			{
				assertEquals(area.getHelperName(), 1, rules.size());
			}
			else
			{
				assertTrue(area.getHelperName(), rules.size() >= 2);
			}
		}
	}

	@Test
	public void cannonPolicyFailsClosedForUnknownAreas()
	{
		assertTrue(CannonTaskCatalog.isCannonAllowedAtAssignedLocation("Smoke Devil Dungeon"));
		assertFalse(CannonTaskCatalog.isCannonAllowedAtAssignedLocation("Slayer Tower"));
		assertFalse(CannonTaskCatalog.isCannonAllowedAtAssignedLocation("Crypt of Tonali"));
		assertFalse(CannonTaskCatalog.isCannonAllowedAtAssignedLocation("A brand new dungeon"));
	}
}
