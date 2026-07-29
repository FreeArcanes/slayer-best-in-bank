package com.freearcanes.slayergear;

import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CannonTaskCatalogTest
{
	@Test
	public void smokeDevilsExposeBarrageCannonMethod()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Smoke devils").orElseThrow();
		assertTrue(CannonTaskCatalog.find("Smoke devils").isPresent());
		assertTrue(profile.getStrategies().get(0).isAncientAoe());
		assertTrue(hasCannon(profile.getStrategies()));
	}

	@Test
	public void mixedLocationTasksRespectAssignedArea()
	{
		assertFalse(CannonTaskCatalog.find("Bloodveld", "Catacombs of Kourend").isPresent());
		assertTrue(CannonTaskCatalog.find("Bloodveld", "Meiyerditch Laboratory").isPresent());
		assertFalse(CannonTaskCatalog.find("Waterfiends", "Ancient Cavern").isPresent());
		assertTrue(CannonTaskCatalog.find("Waterfiends", "Iorwerth Dungeon").isPresent());
		assertFalse(CannonTaskCatalog.find("Wyrms", "Karuulm Slayer Dungeon").isPresent());
		assertTrue(CannonTaskCatalog.find("Wyrms", "Neypotzli").isPresent());
		assertFalse(CannonTaskCatalog.find("Lizardmen", "Lizardman Settlement").isPresent());
		assertTrue(CannonTaskCatalog.find("Lizardmen", "Lizardman Canyon").isPresent());
		assertFalse(CannonTaskCatalog.find("Lesser Nagua", "Ruins of Tapoyauik").isPresent());
		assertTrue(CannonTaskCatalog.find("Lesser Nagua", "Neypotzli").isPresent());
		assertFalse(CannonTaskCatalog.find("Dagannoth", "Catacombs of Kourend").isPresent());
		assertTrue(CannonTaskCatalog.find("Dagannoth", "Lighthouse").isPresent());
		assertTrue(CannonTaskCatalog.find("Dagannoth", "Jormungand's Prison").isPresent());
		assertTrue(CannonTaskCatalog.find("Dagannoth", "Waterbirth Island Dungeon").isPresent());
		assertFalse(CannonTaskCatalog.find("Shades", "Shade Catacombs").isPresent());
		assertTrue(CannonTaskCatalog.find("Shades", "Sepulchre of Death").isPresent());
		assertTrue(CannonTaskCatalog.find("Shades", "Mort'ton").isPresent());
		assertFalse(CannonTaskCatalog.find("Metal dragons", "Ancient Cavern").isPresent());
		assertTrue(CannonTaskCatalog.find("Metal dragons", "Brimhaven Dungeon").isPresent());
		assertTrue(CannonTaskCatalog.find("Metal dragons", "Isle of Souls Dungeon").isPresent());
		assertTrue(CannonTaskCatalog.find("Metal dragons", "Lithkren Vault").isPresent());
		assertFalse(CannonTaskCatalog.find("Metal dragons", "Catacombs of Kourend").isPresent());
	}

	@Test
	public void existingCannonStrategiesUseAssignedCannonLocation()
	{
		SlayerTaskProfile dagannoth = TaskProfiles.find("Dagannoth", "Jormungand's Prison").orElseThrow();
		GearStrategy cannon = dagannoth.getStrategies().stream().filter(SmartSupplyAdvisor::isCannon).findFirst().orElseThrow();
		assertEquals("Jormungand's Prison", cannon.getLocation());
		assertEquals(1, dagannoth.getStrategies().stream()
			.filter(SmartSupplyAdvisor::isCannon).count());

		SlayerTaskProfile bloodveld = TaskProfiles.find("Bloodveld", "Iorwerth Dungeon").orElseThrow();
		GearStrategy bloodveldCannon = bloodveld.getStrategies().stream().filter(SmartSupplyAdvisor::isCannon).findFirst().orElseThrow();
		assertEquals("Iorwerth Dungeon", bloodveldCannon.getLocation());
	}

	@Test
	public void islandOfStoneDagannothsUseJormungandCannonSupport()
	{
		assertTrue(CannonTaskCatalog.find("Dagannoths", "Island of Stone").isPresent());
		assertTrue(CannonTaskCatalog.find("Dagannoth", "Islands of Stone").isPresent());

		SlayerTaskProfile profile = TaskProfiles.find("Dagannoths", "Island of Stone").orElseThrow();
		GearStrategy cannon = profile.getStrategies().stream()
			.filter(SmartSupplyAdvisor::isCannon)
			.findFirst()
			.orElseThrow();
		assertEquals("Island of Stone", cannon.getLocation());
	}

	@Test
	public void unrestrictedDagannothsExposeEachCannonLocation()
	{
		SlayerTaskProfile profile = TaskProfiles.find("Dagannoth").orElseThrow();

		assertTrue(hasCannonAt(profile.getStrategies(), "Lighthouse"));
		assertTrue(hasCannonAt(profile.getStrategies(), "Island of Stone"));
		assertTrue(hasCannonAt(
			profile.getStrategies(), "Waterbirth Island Dungeon"));
	}

	@Test
	public void currentCannonRoutesIncludeNewerAndProtectionHeavyTasks()
	{
		assertTrue(CannonTaskCatalog.find("Drakes").isPresent());
		assertTrue(CannonTaskCatalog.find("Hydras").isPresent());
		assertTrue(CannonTaskCatalog.find("Skeletal wyverns").isPresent());
		assertTrue(CannonTaskCatalog.find("Custodian stalkers").isPresent());
		assertTrue(CannonTaskCatalog.find("Cave bugs").isPresent());
		assertTrue(CannonTaskCatalog.find("Cave crawlers").isPresent());
		assertTrue(CannonTaskCatalog.find("Cave slimes").isPresent());
		assertTrue(CannonTaskCatalog.find("Rockslugs").isPresent());
		assertTrue(CannonTaskCatalog.find("Harpie bug swarms").isPresent());
		assertTrue(CannonTaskCatalog.find("Fever spiders").isPresent());
	}

	@Test
	public void knownNoCannonTasksRemainExcluded()
	{
		assertFalse(CannonTaskCatalog.find("Basilisks").isPresent());
		assertFalse(CannonTaskCatalog.find("Gargoyles").isPresent());
		assertFalse(CannonTaskCatalog.find("Spiritual creatures").isPresent());
		assertFalse(CannonTaskCatalog.find("Kurask").isPresent());
		assertFalse(CannonTaskCatalog.find("Fossil Island wyverns").isPresent());
		assertFalse(CannonTaskCatalog.find("Frost dragons").isPresent());
		assertFalse(CannonTaskCatalog.find("Mogres").isPresent());
	}

	@Test
	public void catalogHasBroadVerifiedCoverage()
	{
		assertTrue(CannonTaskCatalog.routeCount() >= 76);
	}

	private static boolean hasCannon(List<GearStrategy> strategies)
	{
		return strategies.stream().anyMatch(SmartSupplyAdvisor::isCannon);
	}

	private static boolean hasCannonAt(
		List<GearStrategy> strategies,
		String location)
	{
		return strategies.stream()
			.anyMatch(strategy -> SmartSupplyAdvisor.isCannon(strategy)
				&& location.equals(strategy.getLocation()));
	}
}
