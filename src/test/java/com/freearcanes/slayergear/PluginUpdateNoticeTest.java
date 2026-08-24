package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PluginUpdateNoticeTest
{
	@Test
	public void noticeIsBriefAndOnlyNewForDifferentStoredId()
	{
		assertTrue(PluginUpdateNotice.LINES.size() >= 1);
		assertTrue(PluginUpdateNotice.LINES.size() <= 3);
		assertTrue(PluginUpdateNotice.shouldShow(null));
		assertTrue(PluginUpdateNotice.shouldShow("older-update"));
		assertFalse(PluginUpdateNotice.shouldShow(PluginUpdateNotice.ID));
	}
}
