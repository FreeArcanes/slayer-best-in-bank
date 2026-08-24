package com.freearcanes.slayergear;

import java.util.List;

/**
 * Owner-editable release details. Bump VERSION for each update (V1.80, V1.81,
 * V1.82, ...) and edit the notice lines below. A new version is shown once per
 * RuneLite profile, with a maximum of three chat lines.
 */
final class PluginRelease
{
	static final String VERSION = "V1.81";
	static final String DISPLAY_NAME = "Slayer Best in Bank - SBIB " + VERSION;
	static final List<String> NOTICE_LINES = List.of(
		"<col=ff981f>" + DISPLAY_NAME + "</col>",
		"- Target-aware DPS now includes Slayer helmet and Black mask bonuses.",
		"- Imbued, recoloured, charged, and shortened-name variants are recognized.");

	private PluginRelease() {}
}
