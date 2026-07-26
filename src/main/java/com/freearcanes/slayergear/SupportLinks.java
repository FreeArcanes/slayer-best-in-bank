package com.freearcanes.slayergear;

import net.runelite.client.util.LinkBrowser;

final class SupportLinks
{
	static final String DISCORD_INVITE = "https://discord.gg/hmGW7JGmRF";

	private SupportLinks()
	{
	}

	static void openDiscord()
	{
		LinkBrowser.browse(DISCORD_INVITE);
	}
}
