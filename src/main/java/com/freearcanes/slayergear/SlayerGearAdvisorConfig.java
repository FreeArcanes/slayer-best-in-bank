package com.freearcanes.slayergear;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(SlayerGearAdvisorConfig.GROUP)
public interface SlayerGearAdvisorConfig extends Config
{
	String GROUP = "slayergearadvisor";

	@ConfigSection(
		name = "Recommendations",
		description = "Controls how many coherent owned loadouts Best-in-Bank builds.",
		position = 0
	)
	String recommendationSection = "recommendations";

	@ConfigSection(
		name = "Gear preferences",
		description = "Optional personal overrides for the loadout solver.",
		position = 1
	)
	String preferenceSection = "gearPreferences";

	@ConfigSection(
		name = "Trip planning",
		description = "Controls supply estimates while preserving mandatory task safety.",
		position = 2
	)
	String tripPlanningSection = "tripPlanning";

	@ConfigSection(
		name = "Bank highlights",
		description = "Controls normal-bank recommendation markers and their colors.",
		position = 3
	)
	String highlightSection = "bankHighlights";

	@ConfigSection(
		name = "Prep reminder",
		description = "Controls the reminder shown after closing the bank.",
		position = 4
	)
	String reminderSection = "prepReminder";

	@Range(min = 1, max = 3)
	@ConfigItem(
		keyName = "alternativesPerSlot",
		name = "Loadout tiers",
		description = "Number of coherent owned loadouts to build. Tier 2 and Tier 3 show only the swaps that differ from Tier 1.",
		position = 1,
		section = recommendationSection
	)
	default int alternativesPerSlot()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "gearPriority",
		name = "Gear priority",
		description = "Balanced keeps normal DPS-oriented scoring. Prayer First strongly favors owned Prayer-bonus gear while preserving mandatory Slayer mechanics and target-specific weapons.",
		position = 1,
		section = preferenceSection
	)
	default GearPriority gearPriority()
	{
		return GearPriority.BALANCED;
	}

	@ConfigItem(
		keyName = "pinnedItems",
		name = "Always prefer",
		description = "Comma-separated item-name fragments to strongly prefer when valid for the selected strategy.",
		position = 2,
		section = preferenceSection
	)
	default String pinnedItems() { return ""; }

	@ConfigItem(
		keyName = "excludedItems",
		name = "Never recommend",
		description = "Comma-separated item-name fragments the solver must not recommend.",
		position = 3,
		section = preferenceSection
	)
	default String excludedItems() { return ""; }

	@ConfigItem(
		keyName = "lowRiskMode",
		name = "Low-risk mode",
		description = "Build the strongest complete equipment loadout whose combined GE guide value fits the configured cap. Task-required protection and explicitly pinned items remain hard overrides.",
		position = 4,
		section = preferenceSection
	)
	default boolean lowRiskMode() { return false; }

	@Range(min = 50, max = 10000)
	@ConfigItem(
		keyName = "riskCapThousands",
		name = "Loadout cap (k GP)",
		description = "Maximum combined GE guide value for the recommended equipment loadout. Uses one of each selected item; required or pinned items can exceed the cap.",
		position = 5,
		section = preferenceSection
	)
	default int riskCapThousands() { return 500; }

	@ConfigItem(
		keyName = "tripPlan",
		name = "Trip length",
		description = "Full assignment scales from the remaining task, Short trip plans for at most 40 kills, and Custom uses the configured kill count.",
		position = 1,
		section = tripPlanningSection
	)
	default TripPlan tripPlan() { return TripPlan.FULL_ASSIGNMENT; }

	@Range(min = 10, max = 250)
	@ConfigItem(
		keyName = "customTripKills",
		name = "Custom kills",
		description = "Number of kills used when Trip length is Custom kills.",
		position = 2,
		section = tripPlanningSection
	)
	default int customTripKills() { return 80; }

	@ConfigItem(
		keyName = "potionEstimatesEnabled",
		name = "Potion Estimate (BETA)",
		description = "Shows estimated potion counts based on remaining kills, trip length, combat method, and supply preferences. Estimates are advisory only: actual use varies with stats, gear, location, Prayer use, damage taken, and kill speed. Disable this to keep potion recommendations without quantity targets.",
		position = 3,
		section = tripPlanningSection
	)
	default boolean potionEstimatesEnabled() { return true; }

	@ConfigItem(
		keyName = "foodSafety",
		name = "Food safety",
		description = "Adjusts the automatic food estimate.",
		position = 4,
		section = tripPlanningSection
	)
	default SupplyLevel foodSafety() { return SupplyLevel.NORMAL; }

	@ConfigItem(
		keyName = "prayerSafety",
		name = "Prayer safety",
		description = "Adjusts Prayer and restoration supply estimates.",
		position = 5,
		section = tripPlanningSection
	)
	default SupplyLevel prayerSafety() { return SupplyLevel.NORMAL; }

	@ConfigItem(
		keyName = "useGoading",
		name = "Suggest Goading",
		description = "Include owned Goading potions in automatic trip preparation.",
		position = 6,
		section = tripPlanningSection
	)
	default boolean useGoading() { return true; }

	@ConfigItem(
		keyName = "usePrayerRegen",
		name = "Suggest Prayer regeneration",
		description = "Include owned Prayer regeneration potions in automatic trip preparation.",
		position = 7,
		section = tripPlanningSection
	)
	default boolean usePrayerRegen() { return true; }

	@ConfigItem(
		keyName = "preferDivineBoosts",
		name = "Prefer Divine boosts",
		description = "Prefer owned Divine combat boosts over their regular versions.",
		position = 8,
		section = tripPlanningSection
	)
	default boolean preferDivineBoosts() { return true; }

	@ConfigItem(
		keyName = "highlightsEnabled",
		name = "Show highlights",
		description = "Show tier outlines and rank markers on recommended items in the normal bank.",
		position = 1,
		section = highlightSection
	)
	default boolean highlightsEnabled()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "bestColor",
		name = "Tier 1 color",
		description = "Color used for the best-owned recommendation.",
		position = 2,
		section = highlightSection
	)
	default Color bestColor()
	{
		return new Color(255, 193, 7, 255);
	}

	@Alpha
	@ConfigItem(
		keyName = "alternativeColor",
		name = "Tier 2 color",
		description = "Color used for the second-best owned recommendation.",
		position = 3,
		section = highlightSection
	)
	default Color alternativeColor()
	{
		return new Color(67, 214, 205, 255);
	}

	@Alpha
	@ConfigItem(
		keyName = "tierThreeColor",
		name = "Tier 3 color",
		description = "Color used for the fallback owned recommendation.",
		position = 4,
		section = highlightSection
	)
	default Color tierThreeColor()
	{
		return new Color(170, 170, 170, 255);
	}

	@ConfigItem(
		keyName = "prepReminderEnabled",
		name = "Show reminder",
		description = "Show a small in-game prep reminder after closing the bank when required gear is missing or useful task supplies were left behind.",
		position = 1,
		section = reminderSection
	)
	default boolean prepReminderEnabled()
	{
		return true;
	}
}
