package com.freearcanes.slayergear;

import net.runelite.client.plugins.banktags.BankTagsService;

/**
 * Keeps an Inventory Setups bank tag active while Best-in-Bank owns the item
 * layout. The tag is reopened without its custom layout so Inventory Setups can
 * still observe setup switches and deselection without intercepting withdrawals.
 */
final class InventorySetupBankTagState
{
	private static final String INVENTORY_SETUP_TAG_PREFIX = "_invsetup_";

	private String leasedTag;
	private boolean restoreWithLayout;

	boolean hasActiveInventorySetup(BankTagsService bankTagsService)
	{
		String activeTag = bankTagsService.getActiveTag();
		return activeTag != null && activeTag.startsWith(INVENTORY_SETUP_TAG_PREFIX);
	}

	boolean captureActive(BankTagsService bankTagsService)
	{
		if (!hasActiveInventorySetup(bankTagsService))
		{
			return false;
		}

		String activeTag = bankTagsService.getActiveTag();
		boolean activeHasLayout = bankTagsService.getActiveLayout() != null;
		if (!activeTag.equals(leasedTag) || activeHasLayout)
		{
			// A different setup, or the same setup reopened by Inventory Setups,
			// supersedes the lease currently held by Best-in-Bank.
			leasedTag = activeTag;
			restoreWithLayout = activeHasLayout;
		}
		return true;
	}

	boolean activeNeedsNeutralizing(BankTagsService bankTagsService)
	{
		return captureActive(bankTagsService)
			&& bankTagsService.getActiveLayout() != null;
	}

	void neutralizeActive(BankTagsService bankTagsService)
	{
		if (!captureActive(bankTagsService))
		{
			return;
		}
		openWithoutLayout(bankTagsService);
	}

	void reopenNeutralized(BankTagsService bankTagsService)
	{
		if (leasedTag != null)
		{
			openWithoutLayout(bankTagsService);
		}
	}

	void updateLayoutPreference(boolean useLayouts)
	{
		if (leasedTag != null)
		{
			restoreWithLayout = useLayouts;
		}
	}

	boolean shouldRestore(BankTagsService bankTagsService)
	{
		captureActive(bankTagsService);
		return leasedTag != null
			&& leasedTag.equals(bankTagsService.getActiveTag());
	}

	void restore(BankTagsService bankTagsService, boolean restore)
	{
		if (leasedTag == null || !restore)
		{
			clear();
			return;
		}

		int options = BankTagsService.OPTION_ALLOW_MODIFICATIONS
			| BankTagsService.OPTION_HIDE_TAG_NAME;
		if (!restoreWithLayout)
		{
			options |= BankTagsService.OPTION_NO_LAYOUT;
		}

		String tag = leasedTag;
		clear();
		bankTagsService.openBankTag(tag, options);
	}

	private void openWithoutLayout(BankTagsService bankTagsService)
	{
		bankTagsService.openBankTag(
			leasedTag,
			BankTagsService.OPTION_ALLOW_MODIFICATIONS
				| BankTagsService.OPTION_HIDE_TAG_NAME
				| BankTagsService.OPTION_NO_LAYOUT);
	}

	void clear()
	{
		leasedTag = null;
		restoreWithLayout = false;
	}
}
