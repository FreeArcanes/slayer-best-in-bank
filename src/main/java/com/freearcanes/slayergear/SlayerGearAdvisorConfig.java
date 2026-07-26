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
		description = "Controls how many owned gear choices Best-in-Bank keeps per slot.",
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
		name = "Bank highlights",
		description = "Controls normal-bank recommendation markers and their colors.",
		position = 2
	)
	String highlightSection = "bankHighlights";

	@ConfigSection(
		name = "Prep reminder",
		description = "Controls the reminder shown after closing the bank.",
		position = 3
	)
	String reminderSection = "prepReminder";

	@Range(min = 1, max = 3)
	@ConfigItem(
		keyName = "alternativesPerSlot",
		name = "Choices per slot",
		description = "Number of ranked owned items to keep for each equipment slot.",
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
		description = "Skip expensive tradeable gear unless it is explicitly pinned. Useful for Wilderness Slayer.",
		position = 4,
		section = preferenceSection
	)
	default boolean lowRiskMode() { return false; }

	@Range(min = 50, max = 10000)
	@ConfigItem(
		keyName = "riskCapThousands",
		name = "Risk cap (k GP)",
		description = "Maximum GE value per tradeable equipment item while Low-risk mode is enabled.",
		position = 5,
		section = preferenceSection
	)
	default int riskCapThousands() { return 500; }

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