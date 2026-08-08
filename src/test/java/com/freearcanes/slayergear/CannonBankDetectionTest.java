package com.freearcanes.slayergear;

import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CannonBankDetectionTest
{
	@Test
	public void boxedDwarfCannonSetIsDetectedInBank()
	{
		ItemManager itemManager = mock(ItemManager.class);
		ItemComposition set = mock(ItemComposition.class);
		when(itemManager.getItemComposition(ItemID.DWARF_CANNON_SET)).thenReturn(set);
		when(itemManager.canonicalize(ItemID.DWARF_CANNON_SET)).thenReturn(ItemID.DWARF_CANNON_SET);
		when(set.getPlaceholderTemplateId()).thenReturn(-1);
		when(set.getName()).thenReturn("Dwarf cannon set");

		SmartSupplyAdvisor advisor = new SmartSupplyAdvisor(
			itemManager, new SlayerGearAdvisorConfig() {});
		SlayerTaskProfile profile = TaskProfiles.find("Dagannoth").orElseThrow();
		GearStrategy cannon = profile.getStrategies().stream()
			.filter(SmartSupplyAdvisor::isCannon)
			.findFirst()
			.orElseThrow();

		List<SupplyRecommendation> recommendations = advisor.recommend(
			profile,
			cannon,
			80,
			new Item[]{new Item(ItemID.DWARF_CANNON_SET, 1)},
			new Item[0]);
		List<SupplyRecommendation> setRecommendation = recommendations.stream()
			.filter(item -> "Cannon set".equals(item.getCategory()))
			.collect(Collectors.toList());
		List<SupplyRecommendation> missingParts = recommendations.stream()
			.filter(item -> "Cannon setup".equals(item.getCategory()))
			.collect(Collectors.toList());

		assertEquals(1, setRecommendation.size());
		assertEquals("Dwarf cannon set", setRecommendation.get(0).getItemName());
		assertEquals(SupplyStatus.BANKED, setRecommendation.get(0).getStatus());
		assertEquals(4, missingParts.size());
	}
}
