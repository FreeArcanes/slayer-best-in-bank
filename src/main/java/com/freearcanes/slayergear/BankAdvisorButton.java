package com.freearcanes.slayergear;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetType;

class BankAdvisorButton
{
	private static final int BUTTON_WIDTH = 27;
	private static final int BUTTON_HEIGHT = 24;

	private final Client client;
	private final SlayerGearAdvisorPlugin plugin;
	private Widget button;

	@Inject
	BankAdvisorButton(Client client, SlayerGearAdvisorPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
	}

	void install()
	{
		hide();

		Widget parent = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		if (parent == null)
		{
			return;
		}

		button = parent.createChild(-1, WidgetType.GRAPHIC);
		button.setItemId(ItemID.SLAYER_HELM);
		button.setItemQuantity(1);
		button.setItemQuantityMode(ItemQuantityMode.NEVER);
		button.setOriginalWidth(BUTTON_WIDTH);
		button.setOriginalHeight(BUTTON_HEIGHT);
		button.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
		button.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		button.setOriginalX(98);
		button.setOriginalY(6);
		button.setName("Slayer Best in Bank");
		button.setHasListener(true);
		button.setOnOpListener((JavaScriptCallback) event ->
		{
			// Never rebuild/reset the bank search from inside the widget op callback.
			// Queue the transition so the current bank script/click can finish first.
			plugin.queueToggleBankFilter();
		});
		update();
		button.revalidate();
	}

	void update()
	{
		if (button == null)
		{
			return;
		}

		boolean active = plugin.isBankFilterActive();
		button.setAction(1, active ? "Close Best-in-Bank view" : "Show Slayer Best in Bank");
		button.setBorderType(active ? 2 : 1);
		button.setOpacity(active ? 0 : 35);
	}

	void hide()
	{
		if (button != null)
		{
			button.setHidden(true);
			button = null;
		}
	}
}