package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.List;

/**
 * Owner-editable release details. Local development and public Plugin Hub
 * versions intentionally use separate counters so testers never confuse a dev
 * build with a missing public release.
 */
final class PluginRelease
{
	enum Channel { DEVELOPMENT, PUBLIC }

	// Keep DEVELOPMENT locally. Change to PUBLIC only as part of publishing.
	static final Channel CHANNEL = Channel.PUBLIC;
	static final String DEV_VERSION = "V.28";
	static final String PUBLIC_VERSION = "V1.82";

	static final List<String> CHANGE_LINES = List.of(
		"- Fixed Fossil Island wyvern methods and ranged weapon/ammo matching, including quiver variants.",
		"- Added target-aware DPS, live boosts/prayers, combat effects, TTK, kills/hr, and readable comparisons.",
		"- Added Why? explanations and task-aware Objectives with comparisons, switching, and supply policies.",
		"- Added charge-safe gear, validated presets, trip/GP-per-kill costs, and observed task summaries.");

	private PluginRelease() {}

	static String noticeId(Channel channel)
	{
		return channel == Channel.DEVELOPMENT
			? "sbib-dev-" + DEV_VERSION : "sbib-public-" + PUBLIC_VERSION;
	}

	static List<String> noticeLines(Channel channel)
	{
		String heading = channel == Channel.DEVELOPMENT
			? "Dev Update " + DEV_VERSION
			: "Slayer Best in Bank - SBIB " + PUBLIC_VERSION
				+ " - Report bugs in the Discord";
		List<String> lines = new ArrayList<>();
		lines.add("<col=ff981f>" + heading + "</col>");
		for (int index = 0; index < Math.min(4, CHANGE_LINES.size()); index++)
		{
			lines.add(CHANGE_LINES.get(index));
		}
		return List.copyOf(lines);
	}
}
