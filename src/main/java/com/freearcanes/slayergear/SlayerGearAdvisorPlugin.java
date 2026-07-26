package com.freearcanes.slayergear;

import com.google.inject.Provides;
import java.util.Optional;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;

@PluginDescriptor(
	name = "Slayer Best in Bank",
	description = "Builds stable Slayer loadouts from gear and supplies you actually own",
	tags = {"slayer", "gear", "bank", "equipment", "loadout", "overlay"}
)
public class SlayerGearAdvisorPlugin extends Plugin
{
	private static final String SLAYER_CONFIG_GROUP = "slayer";
	private static final String SLAYER_TASK_KEY = "taskName";
	private static final String SLAYER_LOCATION_KEY = "taskLocation";
	private static final String SLAYER_AMOUNT_KEY = "amount";
	private static final Item[] EMPTY_ITEMS = new Item[0];

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private SlayerGearAdvisorConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private GearScorer gearScorer;

	@Inject
	private SlayerGearPanel panel;

	@Inject
	private BankRecommendationOverlay bankOverlay;

	@Inject
	private TaskPrepReminderOverlay prepReminderOverlay;

	@Inject
	private BankAdvisorButton bankButton;

	@Inject
	private TieredBankLayout tieredBankLayout;

	@Inject
	private BankSearch bankSearch;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientToolbar clientToolbar;

	private NavigationButton navigationButton;
	private GearRecommendations recommendations = GearRecommendations.noTask();
	private Item[] lastBankItems;
	private Item[] lastInventoryItems = EMPTY_ITEMS;
	private Item[] lastWornItems = EMPTY_ITEMS;
	private String lastTaskName;
	private String lastTaskLocation = "";
	private int lastTaskAmount = -1;
	private boolean highlightsActive;
	private boolean bankFilterActive;
	private boolean recalculateQueued;
	private boolean bankFilterTransitionQueued;
	private boolean bankViewRefreshQueued;
	private boolean strategyCycleQueued;
	private String bankSearchTextBeforeFilter = "";

	@Provides
	SlayerGearAdvisorConfig provideConfig(ConfigManager manager)
	{
		return manager.getConfig(SlayerGearAdvisorConfig.class);
	}

	@Override
	protected void startUp()
	{
		highlightsActive = config.highlightsEnabled();
		panel.setToggleHandler(this::toggleHighlights);
		panel.setStrategyCycleHandler(this::queueCycleStrategy);
		panel.updateHighlights(highlightsActive);

		AsyncBufferedImage icon = itemManager.getImage(ItemID.SLAYER_HELM);
		final NavigationButton nav = NavigationButton.builder()
			.tooltip("Slayer Best in Bank")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();
		navigationButton = nav;

		// RuneLite snapshots/resizes navigation icons as soon as they are added.
		// Wait for the async item sprite so the toolbar does not cache a blank 16x16 icon.
		icon.onLoaded(() ->
		{
			if (navigationButton == nav)
			{
				clientToolbar.addNavigation(nav);
			}
		});
		overlayManager.add(bankOverlay);
		overlayManager.add(prepReminderOverlay);

		clientThread.invoke(() ->
		{
			lastInventoryItems = snapshotContainer(InventoryID.INV);
			lastWornItems = snapshotContainer(InventoryID.WORN);
			ItemContainer bank = client.getItemContainer(InventoryID.BANK);
			if (bank != null)
			{
				lastBankItems = bank.getItems().clone();
				bankButton.install();
			}
			refreshTask(true);
		});
	}

	@Override
	protected void shutDown()
	{
		closeBankFilter();
		overlayManager.remove(bankOverlay);
		overlayManager.remove(prepReminderOverlay);
		prepReminderOverlay.hide();
		NavigationButton nav = navigationButton;
		navigationButton = null;
		if (nav != null)
		{
			clientToolbar.removeNavigation(nav);
		}
		clientThread.invoke(bankButton::hide);
		lastBankItems = null;
		lastInventoryItems = EMPTY_ITEMS;
		lastWornItems = EMPTY_ITEMS;
		lastTaskName = null;
		lastTaskLocation = "";
		lastTaskAmount = -1;
		recalculateQueued = false;
		bankFilterTransitionQueued = false;
		bankViewRefreshQueued = false;
		strategyCycleQueued = false;
		recommendations = GearRecommendations.noTask();
		panel.display(recommendations);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.HOPPING || state == GameState.LOGIN_SCREEN)
		{
			// Never carry bank/inventory/task state across accounts or worlds.
			closeBankFilter();
			lastBankItems = null;
			lastInventoryItems = EMPTY_ITEMS;
			lastWornItems = EMPTY_ITEMS;
			lastTaskName = null;
			lastTaskLocation = "";
			lastTaskAmount = -1;
			recalculateQueued = false;
			bankFilterTransitionQueued = false;
			bankViewRefreshQueued = false;
			strategyCycleQueued = false;
			recommendations = GearRecommendations.noTask();
			prepReminderOverlay.hide();
			panel.display(recommendations);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		refreshTask(false);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		switch (event.getContainerId())
		{
			case InventoryID.BANK:
				lastBankItems = event.getItemContainer().getItems().clone();
				break;
			case InventoryID.INV:
				lastInventoryItems = event.getItemContainer().getItems().clone();
				break;
			case InventoryID.WORN:
				lastWornItems = event.getItemContainer().getItems().clone();
				break;
			default:
				return;
		}

		// Bank withdrawals commonly emit BANK + INV/WORN changes in the same client
		// cycle. Defer a single rescore so the withdrawn Tier 1 item remains part of
		// the active loadout pool instead of promoting Tier 2 into its place.
		queueRecalculate();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN)
		{
			clientThread.invokeLater(() ->
			{
				ItemContainer bank = client.getItemContainer(InventoryID.BANK);
				if (bank != null)
				{
					lastBankItems = bank.getItems().clone();
				}
				lastInventoryItems = snapshotContainer(InventoryID.INV);
				lastWornItems = snapshotContainer(InventoryID.WORN);
				recalculate(false);
				bankButton.install();
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() != InterfaceID.BANKMAIN || !event.isUnload())
		{
			return;
		}

		// The interface is already unloading, so do not run the bank reset script.
		if (bankFilterActive)
		{
			bankFilterActive = false;
			tieredBankLayout.clear();
		}

		// Rebuild once from the latest BANK + INV/WORN snapshots before deciding
		// whether anything useful was actually left behind.
		recalculateQueued = false;
		recalculate(false);
		prepReminderOverlay.show(recommendations);
		bankButton.hide();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() != ScriptID.BANKMAIN_FINISHBUILDING
			|| !bankFilterActive
			|| recommendations.getState() != GearRecommendations.State.READY)
		{
			return;
		}

		tieredBankLayout.layout(recommendations);
		Widget title = client.getWidget(InterfaceID.Bankmain.TITLE);
		if (title != null)
		{
			title.setText("Slayer Best in Bank");
		}
	}

	@Subscribe(priority = -1)
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!bankFilterActive)
		{
			return;
		}

		switch (event.getEventName())
		{
			case "bankSearchFilter":
			{
				int[] intStack = client.getIntStack();
				int size = client.getIntStackSize();
				if (intStack == null || size < 2)
				{
					break;
				}
				int itemId = intStack[size - 1];
				int canonicalItemId = itemId < 0 ? itemId : itemManager.canonicalize(itemId);
				boolean placeholder = itemId >= 0
					&& itemManager.getItemComposition(itemId).getPlaceholderTemplateId() != -1;
				intStack[size - 2] = !placeholder
					&& (itemId < 0 || recommendations.isBankViewItem(canonicalItemId))
					? 1
					: 0;
				break;
			}
			case "bankBuildTab":
			{
				int[] intStack = client.getIntStack();
				int size = client.getIntStackSize();
				if (intStack != null && size >= 1)
				{
					intStack[size - 1] = 1;
				}
				break;
			}
			case "setSearchBankInputText":
			{
				Object[] objectStack = client.getObjectStack();
				int size = client.getObjectStackSize();
				if (objectStack != null && size >= 1)
				{
					objectStack[size - 1] =
						"Best-in-Bank: " + recommendations.getTaskName();
				}
				break;
			}
			case "setSearchBankInputTextFound":
			{
				Object[] objectStack = client.getObjectStack();
				int size = client.getObjectStackSize();
				if (objectStack != null && size >= 1)
				{
					objectStack[size - 1] =
						"Tier 1 + supplies | Tier 2 | Tier 3";
				}
				break;
			}
			default:
				break;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!SlayerGearAdvisorConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		if ("highlightsEnabled".equals(event.getKey()))
		{
			highlightsActive = config.highlightsEnabled();
			panel.updateHighlights(highlightsActive);
		}
		else if ("alternativesPerSlot".equals(event.getKey())
			|| "gearPriority".equals(event.getKey())
			|| "pinnedItems".equals(event.getKey())
			|| "excludedItems".equals(event.getKey())
			|| "lowRiskMode".equals(event.getKey())
			|| "riskCapThousands".equals(event.getKey()))
		{
			// RuneLite configuration changes can originate outside the client thread.
			// Re-score on the client thread, then queue the bank rebuild after the
			// current bank script has finished. This makes Balanced <-> Prayer First
			// update immediately without closing/reopening the bank.
			clientThread.invokeLater(() -> recalculate());
		}
	}

	void toggleHighlights()
	{
		highlightsActive = !highlightsActive;
		configManager.setConfiguration(
			SlayerGearAdvisorConfig.GROUP,
			"highlightsEnabled",
			highlightsActive);
		panel.updateHighlights(highlightsActive);
	}

	void queueToggleBankFilter()
	{
		// The button listener runs from a game widget callback. Rebuilding the bank
		// synchronously from that callback can invalidate the widget currently being
		// clicked, especially while a bank-tab rebuild is also in flight.
		if (bankFilterTransitionQueued)
		{
			return;
		}

		bankFilterTransitionQueued = true;
		clientThread.invokeLater(() ->
		{
			if (bankFilterActive)
			{
				closeBankFilter();
				bankFilterTransitionQueued = false;
				bankButton.update();
				return;
			}

			if (recommendations.getState() != GearRecommendations.State.READY
				|| client.getWidget(InterfaceID.Bankmain.ITEMS) == null)
			{
				bankFilterTransitionQueued = false;
				bankButton.update();
				return;
			}

			bankSearchTextBeforeFilter = client.getVarcStrValue(VarClientID.MESLAYERINPUT);
			bankFilterActive = true;
			tieredBankLayout.activate();
			bankSearch.initSearch();

			// One more client-cycle boundary lets initSearch/reset and any tab script
			// settle before we lay out the custom Best-in-Bank view.
			clientThread.invokeLater(() ->
			{
				try
				{
					if (!bankFilterActive
						|| client.getWidget(InterfaceID.Bankmain.ITEMS) == null)
					{
						return;
					}

					client.setVarcStrValue(
						VarClientID.MESLAYERINPUT,
						"Tier 1 + supplies | Tier 2 | Tier 3");
					bankSearch.layoutBank();
				}
				finally
				{
					bankFilterTransitionQueued = false;
					bankButton.update();
				}
			});
		});
	}

	boolean isHighlightsActive()
	{
		return highlightsActive;
	}

	boolean isBankFilterActive()
	{
		return bankFilterActive;
	}

	GearRecommendations getRecommendations()
	{
		return recommendations;
	}

	private void refreshTask(boolean force)
	{
		String taskName = configManager.getRSProfileConfiguration(
			SLAYER_CONFIG_GROUP, SLAYER_TASK_KEY);
		taskName = taskName == null ? "" : taskName.trim();
		String taskLocation = configManager.getRSProfileConfiguration(
			SLAYER_CONFIG_GROUP, SLAYER_LOCATION_KEY);
		taskLocation = taskLocation == null ? "" : taskLocation.trim();
		int taskAmount = parseAmount(configManager.getRSProfileConfiguration(
			SLAYER_CONFIG_GROUP, SLAYER_AMOUNT_KEY));

		if (!force
			&& taskName.equals(lastTaskName)
			&& taskLocation.equals(lastTaskLocation)
			&& taskAmount == lastTaskAmount)
		{
			return;
		}

		lastTaskName = taskName;
		lastTaskLocation = taskLocation;
		lastTaskAmount = taskAmount;
		recalculate();
	}

	private void queueRecalculate()
	{
		if (recalculateQueued)
		{
			return;
		}

		recalculateQueued = true;
		clientThread.invokeLater(() ->
		{
			recalculateQueued = false;
			// The bank's own inventory-transmit script already rebuilds its widgets.
			// Running it again here can invalidate the widget under the mouse during a
			// withdrawal, which is what caused intermittent "click does nothing" behavior.
			recalculate(false);
		});
	}

	private void recalculate()
	{
		recalculate(true);
	}

	private void recalculate(boolean rebuildBankView)
	{
		if (lastTaskName == null || lastTaskName.isEmpty())
		{
			closeBankFilter();
			recommendations = GearRecommendations.noTask();
			panel.display(recommendations);
			return;
		}

		Optional<SlayerTaskProfile> profile = TaskProfiles.find(lastTaskName, lastTaskLocation);
		if (!profile.isPresent())
		{
			closeBankFilter();
			recommendations = GearRecommendations.unsupported(
				lastTaskName, lastTaskAmount);
			panel.display(recommendations);
			return;
		}

		if (lastBankItems == null)
		{
			closeBankFilter();
			recommendations = GearRecommendations.openBank(
				lastTaskName, lastTaskAmount, profile.get());
			panel.display(recommendations);
			return;
		}

		String strategyOverride = configManager.getConfiguration(
			SlayerGearAdvisorConfig.GROUP, strategyKey(lastTaskName));
		recommendations = gearScorer.score(
			lastTaskName,
			lastTaskAmount,
			profile.get(),
			combineGearPool(lastBankItems, lastInventoryItems, lastWornItems),
			lastBankItems,
			combineGearPool(EMPTY_ITEMS, lastInventoryItems, lastWornItems),
			config.alternativesPerSlot(),
			client.getRealSkillLevel(Skill.MAGIC),
			client.getRealSkillLevel(Skill.RANGED),
			client.getVarbitValue(VarbitID.KOUREND_DIARY_ELITE_COMPLETE) == 1,
			client.getVarbitValue(VarbitID.SPELLBOOK) == 1,
			strategyOverride,
			config.gearPriority(),
			config.pinnedItems(),
			config.excludedItems(),
			config.lowRiskMode(),
			config.riskCapThousands() * 1_000);
		panel.display(recommendations);
		if (bankFilterActive && rebuildBankView)
		{
			queueBankViewRefresh();
		}
	}

	private void queueBankViewRefresh()
	{
		if (!bankFilterActive || bankViewRefreshQueued)
		{
			return;
		}

		bankViewRefreshQueued = true;
		clientThread.invokeLater(() ->
		{
			try
			{
				if (!bankFilterActive
					|| recommendations.getState() != GearRecommendations.State.READY
					|| client.getWidget(InterfaceID.Bankmain.ITEMS) == null)
				{
					return;
				}

				// Re-run the bank container script only after the triggering config/UI
				// event has completed. ScriptPostFired will apply TieredBankLayout
				// using the newly-scored recommendations.
				bankSearch.layoutBank();
			}
			finally
			{
				bankViewRefreshQueued = false;
				bankButton.update();
			}
		});
	}

	private Item[] snapshotContainer(int containerId)
	{
		ItemContainer container = client.getItemContainer(containerId);
		return container == null ? EMPTY_ITEMS : container.getItems().clone();
	}

	static Item[] combineGearPool(Item[] bank, Item[] inventory, Item[] worn)
	{
		int bankLength = bank == null ? 0 : bank.length;
		int inventoryLength = inventory == null ? 0 : inventory.length;
		int wornLength = worn == null ? 0 : worn.length;
		Item[] combined = new Item[bankLength + inventoryLength + wornLength];
		int offset = 0;
		if (bankLength > 0)
		{
			System.arraycopy(bank, 0, combined, offset, bankLength);
			offset += bankLength;
		}
		if (inventoryLength > 0)
		{
			System.arraycopy(inventory, 0, combined, offset, inventoryLength);
			offset += inventoryLength;
		}
		if (wornLength > 0)
		{
			System.arraycopy(worn, 0, combined, offset, wornLength);
		}
		return combined;
	}

	private void closeBankFilter()
	{
		if (!bankFilterActive)
		{
			return;
		}

		bankFilterActive = false;
		bankViewRefreshQueued = false;
		tieredBankLayout.clear();
		if (client.getWidget(InterfaceID.Bankmain.ITEMS) != null)
		{
			bankSearch.reset(true);
			if (bankSearchTextBeforeFilter != null && !bankSearchTextBeforeFilter.isEmpty())
			{
				client.setVarcStrValue(VarClientID.MESLAYERINPUT, bankSearchTextBeforeFilter);
			}
			bankSearchTextBeforeFilter = "";
			bankButton.update();
		}
	}

	void queueCycleStrategy()
	{
		/*
		 * The sidebar Change method button is a Swing JButton, so its action
		 * listener runs on the Swing EDT. Recommendation scoring reads RuneLite
		 * client state and an active Best-in-Bank view may need to rebuild bank
		 * widgets, so the entire strategy transition belongs on the client thread.
		 */
		if (strategyCycleQueued)
		{
			return;
		}

		strategyCycleQueued = true;
		clientThread.invokeLater(() ->
		{
			try
			{
				cycleStrategyOnClientThread();
			}
			finally
			{
				strategyCycleQueued = false;
			}
		});
	}

	private void cycleStrategyOnClientThread()
	{
		if (recommendations.getState() != GearRecommendations.State.READY
			|| recommendations.getStrategy() == null)
		{
			return;
		}

		List<GearStrategy> eligible = new java.util.ArrayList<>();
		SlayerTaskProfile profile = recommendations.getProfile();
		if (profile != null)
		{
			for (GearStrategy strategy : profile.getStrategies())
			{
				if (strategy.getName().equals(recommendations.getStrategy().getName())
					|| recommendations.getAlternativeStrategies().stream()
						.anyMatch(candidate -> candidate.getName().equals(strategy.getName())))
				{
					eligible.add(strategy);
				}
			}
		}

		if (eligible.size() < 2)
		{
			return;
		}

		int current = 0;
		for (int i = 0; i < eligible.size(); i++)
		{
			if (eligible.get(i).getName().equals(recommendations.getStrategy().getName()))
			{
				current = i;
				break;
			}
		}

		if (current == eligible.size() - 1)
		{
			// One more click after the final explicit method returns the task to Auto.
			configManager.setConfiguration(
				SlayerGearAdvisorConfig.GROUP,
				strategyKey(lastTaskName),
				"");
		}
		else
		{
			GearStrategy next = eligible.get(current + 1);
			configManager.setConfiguration(
				SlayerGearAdvisorConfig.GROUP,
				strategyKey(lastTaskName),
				next.getName());
		}

		/*
		 * Re-score immediately from the cached bank/inventory/equipment snapshot.
		 * panel.display() then schedules the new method on the Swing EDT.
		 *
		 * If Best-in-Bank is active, recalculate() also calls
		 * queueBankViewRefresh(), so the currently-open bank changes in place
		 * instead of waiting for a close/reopen.
		 */
		recalculate();
	}

	private static String strategyKey(String taskName)
	{
		return "strategy." + (taskName == null ? "" : taskName.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
	}

	private static int parseAmount(String value)
	{
		if (value == null || value.trim().isEmpty())
		{
			return 0;
		}

		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ignored)
		{
			return 0;
		}
	}
}