package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.runelite.api.EquipmentInventorySlot;

final class TaskSafetyRules
{
	private TaskSafetyRules() {}

	static List<GearRequirement> gearRequirements(String taskKey, GearStrategy strategy)
	{
		return gearRequirements(taskKey, strategy, false);
	}

	static List<GearRequirement> gearRequirements(String taskKey, GearStrategy strategy, boolean kourendEliteComplete)
	{
		List<GearRequirement> requirements = new ArrayList<>();
		String key = taskKey == null ? "" : taskKey.toLowerCase(Locale.ENGLISH);

		if (contains(key, "aberrant-spectres"))
		{
			requirements.add(head("Slayer helmet or nose peg required", "slayer helmet|slayer helm|nose peg"));
		}
		if (contains(key, "banshee"))
		{
			requirements.add(head("Earmuffs or Slayer helmet required", "earmuffs|slayer helmet|slayer helm"));
		}
		if (contains(key, "sourhog"))
		{
			requirements.add(head("Reinforced goggles or Slayer helmet required", "reinforced goggles|slayer helmet|slayer helm"));
		}
		if (contains(key, "dust-devils", "smoke-devils"))
		{
			requirements.add(head("Facemask or Slayer helmet required", "facemask|slayer helmet|slayer helm"));
		}
		if (contains(key, "wall-beasts"))
		{
			requirements.add(head("Spiny helmet or Slayer helmet required", "spiny helmet|slayer helmet|slayer helm"));
		}
		if (contains(key, "fever-spiders"))
		{
			requirements.add(GearRequirement.anyOf(
				"Slayer gloves required",
				GearRequirement.option(EquipmentInventorySlot.GLOVES, "slayer gloves")));
		}
		if (contains(key, "basilisk", "cockatrice"))
		{
			requirements.add(shield("Mirror shield or V's shield required", "mirror shield|v's shield"));
		}
		if (contains(key, "harpie-bug-swarms"))
		{
			requirements.add(shield("Lit bug lantern required", "lit bug lantern"));
		}
		if (contains(key, "killerwatts"))
		{
			requirements.add(GearRequirement.anyOf("Insulated boots required",
				GearRequirement.option(EquipmentInventorySlot.BOOTS, "insulated boots")));
		}
		if (!kourendEliteComplete && contains(key, "drakes", "wyrms", "hydras"))
		{
			requirements.add(GearRequirement.anyOf("Karuulm heat-protection boots required",
				GearRequirement.option(EquipmentInventorySlot.BOOTS,
					"boots of stone|boots of brimstone|granite boots")));
		}
		if (contains(key, "skeletal-wyverns", "fossil-wyverns"))
		{
			requirements.add(shield(
				"Wyvern breath protection required",
				"elemental shield|mind shield|dragonfire shield|ancient wyvern shield"));
		}
		if (contains(key, "cave-horrors"))
		{
			requirements.add(GearRequirement.anyOf(
				"Witchwood icon required for the recommended melee setup",
				GearRequirement.option(EquipmentInventorySlot.AMULET, "witchwood icon")));
		}

		if (strategy != null && strategy.getRequiredOffhand() != null)
		{
			requirements.add(shield("Required off-hand", strategy.getRequiredOffhand()));
		}
		return dedupe(requirements);
	}

	private static GearRequirement head(String label, String tokens)
	{
		return GearRequirement.anyOf(label, GearRequirement.option(EquipmentInventorySlot.HEAD, tokens));
	}

	private static GearRequirement shield(String label, String tokens)
	{
		return GearRequirement.anyOf(label, GearRequirement.option(EquipmentInventorySlot.SHIELD, tokens));
	}

	private static boolean contains(String key, String... values)
	{
		for (String value : values)
		{
			if (key.contains(value)) return true;
		}
		return false;
	}

	private static List<GearRequirement> dedupe(List<GearRequirement> input)
	{
		List<GearRequirement> output = new ArrayList<>();
		for (GearRequirement requirement : input)
		{
			boolean duplicate = false;
			for (GearRequirement existing : output)
			{
				if (sameRequirement(existing, requirement))
				{
					duplicate = true;
					break;
				}
			}
			if (!duplicate) output.add(requirement);
		}
		return Collections.unmodifiableList(output);
	}

	private static boolean sameRequirement(GearRequirement a, GearRequirement b)
	{
		for (GearRequirement.Option ao : a.getOptions())
		{
			for (GearRequirement.Option bo : b.getOptions())
			{
				if (ao.getSlot() == bo.getSlot() && ao.hasSameTokens(bo)) return true;
			}
		}
		return false;
	}
}