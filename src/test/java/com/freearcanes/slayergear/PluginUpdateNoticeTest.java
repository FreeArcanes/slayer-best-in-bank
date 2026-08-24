package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PluginUpdateNoticeTest
{
	@Test
	public void noticeIsBriefAndOnlyNewForDifferentStoredId()
	{
		assertEquals("sbib-dev-V.20", PluginUpdateNotice.ID);
		assertEquals("<col=ff981f>Dev Update V.20</col>",
			PluginUpdateNotice.LINES.get(0));
		assertEquals("<col=ff981f>Slayer Best in Bank - SBIB V1.8"
			+ " - Report bugs in the Discord</col>",
			PluginRelease.noticeLines(PluginRelease.Channel.PUBLIC).get(0));
		assertEquals("sbib-public-V1.8",
			PluginRelease.noticeId(PluginRelease.Channel.PUBLIC));
		assertTrue(PluginUpdateNotice.LINES.size() >= 1);
		assertTrue(PluginUpdateNotice.LINES.size() <= 3);
		assertTrue(PluginUpdateNotice.shouldShow(null));
		assertTrue(PluginUpdateNotice.shouldShow("older-update"));
		assertFalse(PluginUpdateNotice.shouldShow(PluginUpdateNotice.ID));
	}
}
