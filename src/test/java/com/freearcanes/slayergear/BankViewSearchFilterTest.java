package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BankViewSearchFilterTest
{
	@Test
	public void nativeSearchNarrowsRecommendedItems()
	{
		assertTrue(BankViewSearchFilter.shouldShow(100, false, true, true));
		assertFalse(BankViewSearchFilter.shouldShow(100, false, true, false));
		assertFalse(BankViewSearchFilter.shouldShow(100, false, false, true));
	}

	@Test
	public void placeholdersStayHiddenAndLayoutTargetsRemainUsable()
	{
		assertFalse(BankViewSearchFilter.shouldShow(100, true, true, true));
		assertTrue(BankViewSearchFilter.shouldShow(-1, false, false, false));
	}
}
