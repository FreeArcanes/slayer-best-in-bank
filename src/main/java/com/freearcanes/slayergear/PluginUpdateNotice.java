package com.freearcanes.slayergear;

import java.util.List;

/** Versioned, deliberately brief release notice shown once per RuneLite profile. */
final class PluginUpdateNotice
{
	static final String CONFIG_KEY = "lastUpdateNotice";
	static final String ID = "2026-08-24-dps-ammo";
	static final List<String> LINES = List.of(
		"<col=ff981f>Slayer Best in Bank updated:</col>",
		"• Smarter ranged ammo matching and Dizana variant support.",
		"• Target-aware DPS details with current boosts/prayers and a cleaner DPS menu.");

	private PluginUpdateNotice() {}

	static boolean shouldShow(String lastSeenId)
	{
		return !ID.equals(lastSeenId);
	}
}
