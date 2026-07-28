package com.freearcanes.slayergear;

import net.runelite.client.plugins.banktags.BankTag;
import net.runelite.client.plugins.banktags.BankTagsService;
import net.runelite.client.plugins.banktags.tabs.Layout;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InventorySetupBankTagStateTest
{
	@Test
	public void neutralizesAndRestoresOriginalLayout()
	{
		FakeBankTagsService service = new FakeBankTagsService();
		service.openBankTag("_invsetup_alpha", 0);
		InventorySetupBankTagState state = new InventorySetupBankTagState();

		assertTrue(state.captureActive(service));
		service.closeBankTag(); // Native search initialization closes the tag.
		state.reopenNeutralized(service);

		assertEquals("_invsetup_alpha", service.activeTag);
		assertNull(service.activeLayout);
		assertTrue((service.options & BankTagsService.OPTION_NO_LAYOUT) != 0);

		boolean restore = state.shouldRestore(service);
		service.closeBankTag(); // Best-in-Bank search reset closes the lease.
		state.restore(service, restore);

		assertEquals("_invsetup_alpha", service.activeTag);
		assertTrue(service.activeLayout != null);
		assertFalse((service.options & BankTagsService.OPTION_NO_LAYOUT) != 0);
	}

	@Test
	public void followsSetupSwitchWhileBestInBankIsOpen()
	{
		FakeBankTagsService service = new FakeBankTagsService();
		InventorySetupBankTagState state = new InventorySetupBankTagState();
		service.openBankTag("_invsetup_alpha", 0);
		state.captureActive(service);
		state.neutralizeActive(service);

		service.openBankTag("_invsetup_beta", 0);
		assertTrue(state.activeNeedsNeutralizing(service));
		state.neutralizeActive(service);

		assertEquals("_invsetup_beta", service.activeTag);
		assertNull(service.activeLayout);
		boolean restore = state.shouldRestore(service);
		service.closeBankTag();
		state.restore(service, restore);
		assertEquals("_invsetup_beta", service.activeTag);
		assertTrue(service.activeLayout != null);
	}

	@Test
	public void doesNotResurrectDeselectedSetup()
	{
		FakeBankTagsService service = new FakeBankTagsService();
		InventorySetupBankTagState state = new InventorySetupBankTagState();
		service.openBankTag("_invsetup_alpha", 0);
		state.captureActive(service);
		state.neutralizeActive(service);

		service.closeBankTag();
		boolean restore = state.shouldRestore(service);
		state.restore(service, restore);

		assertFalse(restore);
		assertNull(service.activeTag);
	}

	@Test
	public void preservesClassicFilteringPreference()
	{
		FakeBankTagsService service = new FakeBankTagsService();
		InventorySetupBankTagState state = new InventorySetupBankTagState();
		service.openBankTag(
			"_invsetup_alpha", BankTagsService.OPTION_NO_LAYOUT);
		state.captureActive(service);

		boolean restore = state.shouldRestore(service);
		service.closeBankTag();
		state.restore(service, restore);

		assertNull(service.activeLayout);
		assertTrue((service.options & BankTagsService.OPTION_NO_LAYOUT) != 0);
	}

	@Test
	public void followsSameSetupLayoutPreferenceChange()
	{
		FakeBankTagsService service = new FakeBankTagsService();
		InventorySetupBankTagState state = new InventorySetupBankTagState();
		service.openBankTag("_invsetup_alpha", 0);
		state.captureActive(service);
		state.neutralizeActive(service);
		state.updateLayoutPreference(false);

		boolean restore = state.shouldRestore(service);
		service.closeBankTag();
		state.restore(service, restore);

		assertNull(service.activeLayout);
		assertTrue((service.options & BankTagsService.OPTION_NO_LAYOUT) != 0);
	}

	@Test
	public void doesNotReplaceNormalBankTag()
	{
		FakeBankTagsService service = new FakeBankTagsService();
		InventorySetupBankTagState state = new InventorySetupBankTagState();
		service.openBankTag("_invsetup_alpha", 0);
		state.captureActive(service);
		state.neutralizeActive(service);
		service.openBankTag("normal-tag", 0);

		boolean restore = state.shouldRestore(service);
		state.restore(service, restore);

		assertFalse(restore);
		assertEquals("normal-tag", service.activeTag);
	}

	private static final class FakeBankTagsService implements BankTagsService
	{
		private String activeTag;
		private Layout activeLayout;
		private int options;

		@Override
		public void openBankTag(String tag, int options)
		{
			this.activeTag = tag;
			this.options = options;
			this.activeLayout = (options & OPTION_NO_LAYOUT) == 0
				? new Layout(tag) : null;
		}

		@Override
		public void closeBankTag()
		{
			activeTag = null;
			activeLayout = null;
			options = 0;
		}

		@Override
		public String getActiveTag()
		{
			return activeTag;
		}

		@Override
		public BankTag getActiveBankTag()
		{
			return null;
		}

		@Override
		public Layout getActiveLayout()
		{
			return activeLayout;
		}
	}
}
