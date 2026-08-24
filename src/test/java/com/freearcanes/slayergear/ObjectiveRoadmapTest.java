package com.freearcanes.slayergear;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObjectiveRoadmapTest
{
	@Test
	public void bossAndMultiTargetMethodsSuggestMaxDpsWithoutForcingIt()
	{
		SlayerTaskProfile boss = TaskProfiles.find("Araxxor").orElseThrow();
		ObjectiveAdvisor.Suggestion bossSuggestion = ObjectiveAdvisor.suggest(
			boss, boss.getStrategies().get(0));
		assertEquals(GearPriority.BALANCED, bossSuggestion.getObjective());

		SlayerTaskProfile dustDevils = TaskProfiles.find("Dust devils").orElseThrow();
		GearStrategy ancient = dustDevils.getStrategies().stream()
			.filter(GearStrategy::isAncientAoe).findFirst().orElseThrow();
		assertEquals(GearPriority.BALANCED,
			ObjectiveAdvisor.suggest(dustDevils, ancient).getObjective());
	}

	@Test
	public void defensiveAndOrdinaryMethodsGetSustainSuggestions()
	{
		GearStrategy defensive = GearStrategy.builder().name("Defensive")
			.combatStyle(CombatStyle.MELEE).magicDefenceWeight(0.25).build();
		assertEquals(GearPriority.DEFENCE_FIRST,
			ObjectiveAdvisor.suggest(null, defensive).getObjective());
		assertEquals(GearPriority.PRAYER_FIRST,
			ObjectiveAdvisor.suggest(null, GearStrategy.builder().build()).getObjective());
	}

	@Test
	public void comparisonPoliciesStateExactQuantityChanges()
	{
		assertTrue(ObjectiveAdvisor.policy(GearPriority.BALANCED).contains("+50%"));
		assertTrue(ObjectiveAdvisor.policy(GearPriority.PRAYER_FIRST).contains("+50%"));
		assertTrue(ObjectiveAdvisor.policy(GearPriority.DEFENCE_FIRST).contains("+50%"));
		assertTrue(ObjectiveAdvisor.policy(GearPriority.VALUE).contains("-50%"));
		assertTrue(ObjectiveAdvisor.changeFromMaxDps(GearPriority.VALUE)
			.contains("optional boost"));
	}

	@Test
	public void loadoutComparisonReportsRealChangedSlotsAndPanelText()
	{
		Map<EquipmentInventorySlot, GearRecommendation> selected = new EnumMap<>(EquipmentInventorySlot.class);
		Map<EquipmentInventorySlot, GearRecommendation> preview = new EnumMap<>(EquipmentInventorySlot.class);
		selected.put(EquipmentInventorySlot.BODY, recommendation("Strength body", EquipmentInventorySlot.BODY));
		preview.put(EquipmentInventorySlot.BODY, recommendation("Prayer body", EquipmentInventorySlot.BODY));
		String changes = GearScorer.objectiveGearChanges(selected, preview, false);
		assertTrue(changes.contains("body: Prayer body"));
		assertEquals("Gear: " + changes, SlayerGearPanel.objectiveLoadoutText(
			Arrays.asList(new ObjectiveLoadoutComparison(GearPriority.PRAYER_FIRST, changes)),
			GearPriority.PRAYER_FIRST));
		assertEquals("Same Tier 1 gear",
			GearScorer.objectiveGearChanges(selected, selected, false));
	}

	private static GearRecommendation recommendation(String name, EquipmentInventorySlot slot)
	{
		return GearRecommendation.builder().itemName(name).slot(slot).reason("test").build();
	}
}
