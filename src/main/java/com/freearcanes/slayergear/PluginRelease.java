package com.freearcanes.slayergear;

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
	static final Channel CHANNEL = Channel.DEVELOPMENT;
	static final String DEV_VERSION = "V.26";
	static final String PUBLIC_VERSION = "V1.81";

	static final List<String> CHANGE_LINES = List.of(
		"- Added Value mode, advisory task Objectives, and exact policy changes.",
		"- Added a side-by-side Objective comparison in the task sidebar.");

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
		return List.of(
			"<col=ff981f>" + heading + "</col>",
			CHANGE_LINES.get(0),
			CHANGE_LINES.get(1));
	}
}
