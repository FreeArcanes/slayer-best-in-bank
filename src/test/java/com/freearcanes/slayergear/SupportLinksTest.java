package com.freearcanes.slayergear;

import java.net.URI;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SupportLinksTest
{
	@Test
	public void discordSupportLinkIsAPlainHttpsInvite()
	{
		URI invite = URI.create(SupportLinks.DISCORD_INVITE);

		assertEquals("https", invite.getScheme());
		assertEquals("discord.gg", invite.getHost());
		assertEquals("/HU67cBGBnt", invite.getPath());
		assertNull(invite.getQuery());
		assertNull(invite.getFragment());
	}
}
