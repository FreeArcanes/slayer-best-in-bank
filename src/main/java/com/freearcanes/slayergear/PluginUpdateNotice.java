package com.freearcanes.slayergear;

import java.util.List;

/** Versioned, deliberately brief release notice shown once per RuneLite profile. */
final class PluginUpdateNotice
{
	static final String CONFIG_KEY = "lastUpdateNotice";
	static final String ID = "sbib-" + PluginRelease.VERSION;
	static final List<String> LINES = PluginRelease.NOTICE_LINES;

	private PluginUpdateNotice() {}

	static boolean shouldShow(String lastSeenId)
	{
		return !ID.equals(lastSeenId);
	}
}
