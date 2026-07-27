package com.freearcanes.slayergear;

import net.runelite.client.util.LinkBrowser;

final class SupportLinks
{
	static final String DISCORD_INVITE = "https://discord.gg/HU67cBGBnt";

	private SupportLinks()
	{
	}

	static void openDiscord()
	{
		LinkBrowser.browse(DISCORD_INVITE);
	}
}
