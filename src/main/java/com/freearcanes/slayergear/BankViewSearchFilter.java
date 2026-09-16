package com.freearcanes.slayergear;

/**
 * Combines the native RuneLite bank search with Slayer Best in Bank's
 * recommendation-only view.
 */
final class BankViewSearchFilter
{
	private BankViewSearchFilter() {}

	static boolean shouldShow(
		int itemId,
		boolean placeholder,
		boolean recommended,
		boolean nativeSearchMatch)
	{
		// The bank uses negative item ids for non-item layout/drag targets.
		if (itemId < 0)
		{
			return true;
		}
		return !placeholder && recommended && nativeSearchMatch;
	}
}
