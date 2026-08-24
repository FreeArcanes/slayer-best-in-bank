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
		assertEquals("Slayer Best in Bank - SBIB V1.81", PluginRelease.DISPLAY_NAME);
		assertEquals("sbib-V1.81", PluginUpdateNotice.ID);
		assertTrue(PluginUpdateNotice.LINES.size() >= 1);
		assertTrue(PluginUpdateNotice.LINES.size() <= 3);
		assertTrue(PluginUpdateNotice.shouldShow(null));
		assertTrue(PluginUpdateNotice.shouldShow("older-update"));
		assertFalse(PluginUpdateNotice.shouldShow(PluginUpdateNotice.ID));
	}
}
