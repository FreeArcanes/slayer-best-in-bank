package com.freearcanes.slayergear;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
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
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.plugins.banktags.BankTagsService;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;

@PluginDescriptor(
	name = "Slayer Best in Bank",
	description = "Builds stable Slayer loadouts from gear and supplies you actually own",
	tags = {"slayer", "gear", "bank", "equipment", "loadout", "overlay"}
)
@PluginDependency(BankTagsPlugin.class)
public class SlayerGearAdvisorPlugin extends Plugin
{
	private static final String SLAYER_CONFIG_GROUP = "slayer";
	private static final String SLAYER_TASK_KEY = "taskName";
	private static final String SLAYER_LOCATION_KEY = "taskLocation";
	private static final String SLAYER_AMOUNT_KEY = "amount";
	private static final Item[] EMPTY_ITEMS = new Item[0];
	private static final Set<Integer> DIZANAS_QUIVER_IDS = dizanasQuiverIds();

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
	private InventoryCapacityPlanner inventoryCapacityPlanner;

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
	private BankTagsService bankTagsService;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientToolbar clientToolbar;

	private NavigationButton navigationButton;
	private GearRecommendations recommendations = GearRecommendations.noTask();
	private Item[] lastBankItems;
	private Item[] lastInventoryItems = EMPTY_ITEMS;
	private Item[] lastWornItems = EMPTY_ITEMS;
	private Item[] bankSessionGearPool;
	private Item[] bankSessionBankItems;
	private String lastTaskName;
	private String lastTaskLocation = "";
	private int lastTaskAmount = -1;
	private volatile boolean highlightsActive;
	private final BankFlowState bankFlow = new BankFlowState();
	private final TripPreparationState tripPreparation = new TripPreparationState();
	private final AtomicBoolean pluginRunning = new AtomicBoolean();
	private final AtomicBoolean strategyCycleRequestQueued = new AtomicBoolean();
	private final AtomicBoolean loadoutRefreshRequestQueued = new AtomicBoolean();
	private String bankSearchTextBeforeFilter = "";

	@Provides
	SlayerGearAdvisorConfig provideConfig(ConfigManager manager)
	{
		return manager.getConfig(SlayerGearAdvisorConfig.class);
	}

	@Override
	protected void startUp()
	{
		pluginRunning.set(true);
		highlightsActive = config.highlightsEnabled();
		panel.setTheme(config.panelTheme());
		panel.setStrategyCycleHandler(this::queueCycleStrategy);
		panel.setSupplyQuantityHandler(this::adjustSupplyQuantity);
		panel.setLoadoutRefreshHandler(this::queueRefreshBankLoadout);
		panel.setAdvisorToggleHandler(this::toggleAdvisor);
		panel.setAdvisorEnabled(config.advisorEnabled());

		AsyncBufferedImage icon = itemManager.getImage(ItemID.SLAYER_HELM);
		// RuneLite snapshots/resizes navigation icons as soon as they are added.
		// Trim the item sprite's transparent inventory padding first so the helm
		// fills more of RuneLite's fixed toolbar icon area.
		icon.onLoaded(() -> SwingUtilities.invokeLater(() ->
		{
			if (pluginRunning.get() && navigationButton == null)
			{
				NavigationButton nav = NavigationButton.builder()
					.tooltip("Slayer Best in Bank")
					.icon(toolbarIcon(icon))
					.priority(6)
					.panel(panel)
					.build();
				navigationButton = nav;
				clientToolbar.addNavigation(nav);
			}
		}));
		overlayManager.add(bankOverlay);
		overlayManager.add(prepReminderOverlay);

		clientThread.invoke(() ->
		{
			lastInventoryItems = snapshotContainer(InventoryID.INV);
			lastWornItems = snapshotContainer(InventoryID.WORN);
			ItemContainer bank = client.getItemContainer(InventoryID.BANK);
			if (bank != null)
			{
				bankFlow.openBank();
				lastBankItems = bank.getItems().clone();
				bankSessionBankItems = lastBankItems.clone();
				bankSessionGearPool = combineGearPool(
					lastBankItems, lastInventoryItems, lastWornItems);
				bankButton.install();
			}
			refreshTask(true);
		});
	}

	static BufferedImage toolbarIcon(BufferedImage source)
	{
		int minX = source.getWidth();
		int minY = source.getHeight();
		int maxX = -1;
		int maxY = -1;
		for (int y = 0; y < source.getHeight(); y++)
		{
			for (int x = 0; x < source.getWidth(); x++)
			{
				if ((source.getRGB(x, y) >>> 24) != 0)
				{
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				}
			}
		}

		if (maxX < minX || maxY < minY)
		{
			return source;
		}

		int padding = 1;
		minX = Math.max(0, minX - padding);
		minY = Math.max(0, minY - padding);
		maxX = Math.min(source.getWidth() - 1, maxX + padding);
		maxY = Math.min(source.getHeight() - 1, maxY + padding);
		return source.getSubimage(
			minX,
			minY,
			maxX - minX + 1,
			maxY - minY + 1);
	}

	@Override
	protected void shutDown()
	{
		pluginRunning.set(false);
		overlayManager.remove(bankOverlay);
		overlayManager.remove(prepReminderOverlay);
		NavigationButton nav = navigationButton;
		navigationButton = null;
		if (nav != null)
		{
			clientToolbar.removeNavigation(nav);
		}
		strategyCycleRequestQueued.set(false);
		loadoutRefreshRequestQueued.set(false);
		clientThread.invoke(() ->
		{
			closeBankFilter();
			bankButton.hide();
			prepReminderOverlay.hide();
			lastBankItems = null;
			lastInventoryItems = EMPTY_ITEMS;
			lastWornItems = EMPTY_ITEMS;
			bankSessionGearPool = null;
			bankSessionBankItems = null;
			lastTaskName = null;
			lastTaskLocation = "";
			lastTaskAmount = -1;
			bankFlow.resetSession();
			tripPreparation.reset();
			recommendations = GearRecommendations.noTask();
			panel.display(recommendations);
		});
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
			bankSessionGearPool = null;
			bankSessionBankItems = null;
			lastTaskName = null;
			lastTaskLocation = "";
			lastTaskAmount = -1;
			bankFlow.resetSession();
			tripPreparation.reset();
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
		if (bankFlow.isLoadoutLocked() && bankFlow.isLoadoutRefreshPending())
		{
			return;
		}
		queueRecalculate();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO
			|| event.getVarpId() == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT)
		{
			queueRecalculate();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN)
		{
			tripPreparation.reset();
			bankFlow.openBank();
			clientThread.invokeLater(() ->
			{
				ItemContainer bank = client.getItemContainer(InventoryID.BANK);
				if (bank != null)
				{
					lastBankItems = bank.getItems().clone();
				}
				lastInventoryItems = snapshotContainer(InventoryID.INV);
				lastWornItems = snapshotContainer(InventoryID.WORN);
				bankSessionGearPool = bank == null
					? null
					: combineGearPool(lastBankItems, lastInventoryItems, lastWornItems);
				bankSessionBankItems = bank == null ? null : lastBankItems.clone();
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
		if (bankFlow.isFilterActive())
		{
			bankFlow.deactivateFilter();
			tieredBankLayout.clear();
		}

		// Rebuild once from the latest BANK + INV/WORN snapshots before deciding
		// whether anything useful was actually left behind.
		bankFlow.closeBank();
		bankSessionGearPool = null;
		bankSessionBankItems = null;
		recalculate(false);
		tripPreparation.arm(
			recommendations,
			combineGearPool(
				combineGearPool(EMPTY_ITEMS, lastInventoryItems, lastWornItems),
				snapshotLoadedQuiverAmmo(),
				EMPTY_ITEMS),
			itemManager::canonicalize);
		prepReminderOverlay.show(recommendations);
		bankButton.hide();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() != ScriptID.BANKMAIN_FINISHBUILDING
			|| !bankFlow.isFilterActive()
			|| recommendations.getState() != GearRecommendations.State.READY)
		{
			return;
		}

		if (bankTagsService.getActiveTag() != null)
		{
			// Bank Tags and Inventory Setups own their active view. Never rewrite,
			// reopen, or overlay it with Best-in-Bank's widget layout.
			queueToggleBankFilter();
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
		if (!bankFlow.isFilterActive())
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
						"T1 Equip → T1 Supplies → T2 → T3";
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
		}
		else if ("panelTheme".equals(event.getKey()))
		{
			panel.setTheme(config.panelTheme());
		}
		else if ("advisorEnabled".equals(event.getKey()))
		{
			panel.setAdvisorEnabled(config.advisorEnabled());
			clientThread.invokeLater(this::applyAdvisorEnabledState);
		}
		else if (isRecommendationConfigKey(event.getKey()))
		{
			// RuneLite configuration changes can originate outside the client thread.
			// Re-score on the client thread. An open bank keeps its current plan
			// stable and exposes the change through the explicit Refresh action.
			clientThread.invokeLater(this::recalculateOrMarkBankRefresh);
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		// Config proxies follow the active RuneLite profile. Refresh both the
		// cached highlight toggle and every recommendation after a profile switch.
		clientThread.invokeLater(() ->
		{
			highlightsActive = config.highlightsEnabled();
			panel.setTheme(config.panelTheme());
			panel.setAdvisorEnabled(config.advisorEnabled());
			applyAdvisorEnabledState();
		});
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		// Per-task supply overrides are stored in the RuneScape profile, so account
		// and game-mode profile changes must not retain the previous profile's plan.
		clientThread.invokeLater(() ->
		{
			recalculateOrMarkBankRefresh();
		});
	}

	static boolean isRecommendationConfigKey(String key)
	{
		if (key == null) return false;
		return "alternativesPerSlot".equals(key)
			|| "gearPriority".equals(key)
			|| "pinnedItems".equals(key)
			|| "excludedItems".equals(key)
			|| "lowRiskMode".equals(key)
			|| "riskCapThousands".equals(key)
			|| "tripPlan".equals(key)
			|| "customTripKills".equals(key)
			|| "potionEstimatesEnabled".equals(key)
			|| "foodSafety".equals(key)
			|| "prayerSafety".equals(key)
			|| "prayerRestorePreference".equals(key)
			|| "useGoading".equals(key)
			|| "usePrayerRegen".equals(key)
			|| "preferDivineBoosts".equals(key)
			|| "useSlayerBracelet".equals(key)
			|| "slayerBraceletPreference".equals(key)
			|| "travelSuggestionsEnabled".equals(key)
			|| "homeTeleportPreference".equals(key)
			|| "spellTeleportPreference".equals(key)
			|| "slayerRingPreference".equals(key)
			|| "fairyRingPreference".equals(key)
			|| "kourendTeleportPreference".equals(key)
			|| key.startsWith("supply.");
	}

	void queueToggleBankFilter()
	{
		// The button listener runs from a game widget callback. Rebuilding the bank
		// synchronously from that callback can invalidate the widget currently being
		// clicked, especially while a bank-tab rebuild is also in flight.
		if (!pluginRunning.get() || !bankFlow.beginFilterTransition())
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			if (!pluginRunning.get())
			{
				bankFlow.completeFilterTransition();
				return;
			}
			if (bankFlow.isFilterActive())
			{
				closeBankFilter();
				bankFlow.completeFilterTransition();
				bankButton.update();
				return;
			}

			if (recommendations.getState() != GearRecommendations.State.READY
				|| client.getWidget(InterfaceID.Bankmain.ITEMS) == null
				|| bankTagsService.getActiveTag() != null)
			{
				bankFlow.completeFilterTransition();
				bankButton.update();
				return;
			}

			bankSearchTextBeforeFilter = client.getVarcStrValue(VarClientID.MESLAYERINPUT);
			bankFlow.activateFilter();
			tieredBankLayout.activate();
			bankSearch.initSearch();

			// One more client-cycle boundary lets initSearch/reset and any tab script
			// settle before we lay out the custom Best-in-Bank view.
			clientThread.invokeLater(() ->
			{
				try
				{
					if (!pluginRunning.get()
						|| !bankFlow.isFilterActive()
						|| client.getWidget(InterfaceID.Bankmain.ITEMS) == null)
					{
						return;
					}

					client.setVarcStrValue(
						VarClientID.MESLAYERINPUT,
						"T1 Equip → T1 Supplies → T2 → T3");
					bankSearch.layoutBank();
				}
				finally
				{
					bankFlow.completeFilterTransition();
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
		return bankFlow.isFilterActive();
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

		if (TripPreparationState.assignmentChanged(
			lastTaskName,
			lastTaskLocation,
			lastTaskAmount,
			taskName,
			taskLocation,
			taskAmount))
		{
			tripPreparation.reset();
		}
		lastTaskName = taskName;
		lastTaskLocation = taskLocation;
		lastTaskAmount = taskAmount;
		if (bankFlow.isLoadoutLocked())
		{
			markBankRefreshPending();
			return;
		}
		recalculate();
	}

	private void toggleAdvisor()
	{
		configManager.setConfiguration(
			SlayerGearAdvisorConfig.GROUP,
			"advisorEnabled",
			!config.advisorEnabled());
	}

	private void applyAdvisorEnabledState()
	{
		if (config.advisorEnabled())
		{
			recalculateOrMarkBankRefresh();
			return;
		}

		closeBankFilter();
		bankFlow.unlockLoadout();
		tripPreparation.reset();
		prepReminderOverlay.hide();
		bankButton.hide();
		recommendations = GearRecommendations.noTask();
		panel.display(recommendations);
	}

	private void queueRecalculate()
	{
		if (!bankFlow.queueRecalculation())
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			bankFlow.completeRecalculation();
			// The bank's own inventory-transmit script already rebuilds its widgets.
			// Running it again here can invalidate the widget under the mouse during a
			// withdrawal, which is what caused intermittent "click does nothing" behavior.
			recalculate(false);
		});
	}

	private void recalculateOrMarkBankRefresh()
	{
		if (bankFlow.isLoadoutLocked())
		{
			markBankRefreshPending();
			return;
		}
		recalculate();
	}

	private void markBankRefreshPending()
	{
		bankFlow.markLoadoutRefreshPending();
		if (recommendations.getState() == GearRecommendations.State.READY)
		{
			recommendations = recommendations.withBankSessionState(
				bankFlow.isLoadoutLocked(),
				bankFlow.isLoadoutRefreshPending());
			panel.display(recommendations);
		}
	}

	void queueRefreshBankLoadout()
	{
		if (!pluginRunning.get()
			|| !loadoutRefreshRequestQueued.compareAndSet(false, true))
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			try
			{
				if (pluginRunning.get())
				{
					refreshBankLoadoutOnClientThread();
				}
			}
			finally
			{
				loadoutRefreshRequestQueued.set(false);
			}
		});
	}

	private void refreshBankLoadoutOnClientThread()
	{
		if (!bankFlow.isBankOpen())
		{
			tripPreparation.reset();
			recalculate();
			return;
		}
		if (!bankFlow.queueLoadoutRefresh())
		{
			return;
		}

		try
		{
			bankFlow.unlockLoadout();
			bankSessionGearPool = combineGearPool(
				lastBankItems, lastInventoryItems, lastWornItems);
			bankSessionBankItems = lastBankItems == null
				? null
				: lastBankItems.clone();
			recalculate();
		}
		finally
		{
			bankFlow.completeLoadoutRefresh();
		}
	}

	private void recalculate()
	{
		recalculate(true);
	}

	private void recalculate(boolean rebuildBankView)
	{
		if (!config.advisorEnabled()
			|| lastTaskName == null || lastTaskName.isEmpty())
		{
			closeBankFilter();
			bankFlow.unlockLoadout();
			recommendations = GearRecommendations.noTask();
			panel.display(recommendations);
			return;
		}

		Optional<SlayerTaskProfile> profile = TaskProfiles.find(lastTaskName, lastTaskLocation);
		if (!profile.isPresent())
		{
			closeBankFilter();
			bankFlow.unlockLoadout();
			recommendations = GearRecommendations.unsupported(
				lastTaskName, lastTaskAmount);
			panel.display(recommendations);
			return;
		}

		if (lastBankItems == null)
		{
			closeBankFilter();
			bankFlow.unlockLoadout();
			recommendations = GearRecommendations.openBank(
				lastTaskName, lastTaskAmount, profile.get());
			panel.display(recommendations);
			return;
		}

		String strategyOverride = configManager.getConfiguration(
			SlayerGearAdvisorConfig.GROUP, strategyKey(lastTaskName));
		if (bankFlow.isBankOpen() && bankSessionGearPool == null)
		{
			bankSessionGearPool = combineGearPool(
				lastBankItems, lastInventoryItems, lastWornItems);
		}
		if (bankFlow.isBankOpen() && bankSessionBankItems == null)
		{
			bankSessionBankItems = lastBankItems.clone();
		}
		Item[] baseScoringPool = bankFlow.isBankOpen() && bankSessionGearPool != null
			? bankSessionGearPool
			: combineGearPool(lastBankItems, lastInventoryItems, lastWornItems);
		Item[] loadedQuiverAmmo = snapshotLoadedQuiverAmmo();
		Item[] scoringPool = combineGearPool(baseScoringPool, loadedQuiverAmmo, EMPTY_ITEMS);
		Item[] scoringBankItems = bankItemsForScoring(
			bankFlow.isBankOpen(), bankSessionBankItems, lastBankItems);
		Item[] livePackedItems = combineGearPool(
			combineGearPool(EMPTY_ITEMS, lastInventoryItems, lastWornItems),
			loadedQuiverAmmo,
			EMPTY_ITEMS);
		Item[] packedSupplyItems =
			tripPreparation.suppliesForScoring(livePackedItems, itemManager::canonicalize);
		GearRecommendations scored = gearScorer.score(
			lastTaskName,
			lastTaskAmount,
			lastTaskLocation,
			profile.get(),
			scoringPool,
			scoringBankItems,
			livePackedItems,
			packedSupplyItems,
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
			config.riskCapThousands() * 1_000,
			loadedQuiverAmmo.length > 0);
		if (bankFlow.isBankOpen())
		{
			bankFlow.lockLoadout();
		}
		recommendations = inventoryCapacityPlanner.apply(
			scored,
			lastInventoryItems,
			bankFlow.isLoadoutLocked(),
			bankFlow.isLoadoutRefreshPending());
		panel.display(recommendations);
		if (bankFlow.isFilterActive() && rebuildBankView)
		{
			queueBankViewRefresh();
		}
	}

	private void queueBankViewRefresh()
	{
		if (!bankFlow.queueBankViewRefresh())
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			try
			{
				if (!bankFlow.isFilterActive()
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
				bankFlow.completeBankViewRefresh();
				bankButton.update();
			}
		});
	}

	private Item[] snapshotContainer(int containerId)
	{
		ItemContainer container = client.getItemContainer(containerId);
		return container == null ? EMPTY_ITEMS : container.getItems().clone();
	}

	private Item[] snapshotLoadedQuiverAmmo()
	{
		return loadedQuiverAmmo(
			lastWornItems,
			client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO),
			client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT));
	}

	static Item[] loadedQuiverAmmo(Item[] worn, int ammoId, int ammoCount)
	{
		int capeSlot = EquipmentInventorySlot.CAPE.getSlotIdx();
		if (worn == null || capeSlot >= worn.length || worn[capeSlot] == null
			|| !DIZANAS_QUIVER_IDS.contains(worn[capeSlot].getId())
			|| ammoId < 0 || ammoCount <= 0)
		{
			return EMPTY_ITEMS;
		}
		return new Item[] {new Item(ammoId, ammoCount)};
	}

	private static Set<Integer> dizanasQuiverIds()
	{
		Set<Integer> ids = new HashSet<>();
		ids.addAll(ItemVariationMapping.getVariations(
			ItemVariationMapping.map(ItemID.DIZANAS_QUIVER_CHARGED)));
		ids.addAll(ItemVariationMapping.getVariations(
			ItemVariationMapping.map(ItemID.DIZANAS_QUIVER_INFINITE)));
		ids.addAll(ItemVariationMapping.getVariations(
			ItemVariationMapping.map(ItemID.SKILLCAPE_MAX_DIZANAS)));
		return ids;
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

	static Item[] bankItemsForScoring(
		boolean bankOpen,
		Item[] bankSessionItems,
		Item[] liveBankItems)
	{
		return bankOpen && bankSessionItems != null
			? bankSessionItems
			: liveBankItems;
	}

	private void closeBankFilter()
	{
		if (!bankFlow.isFilterActive())
		{
			return;
		}

		boolean bankTagActive = bankTagsService.getActiveTag() != null;
		bankFlow.deactivateFilter();
		tieredBankLayout.clear();
		if (client.getWidget(InterfaceID.Bankmain.ITEMS) != null)
		{
			// Selecting a tag/setup already rebuilt the bank view. Resetting search
			// here could close or replace the newly selected external tag.
			if (!bankTagActive)
			{
				bankSearch.reset(true);
				if (bankSearchTextBeforeFilter != null
					&& !bankSearchTextBeforeFilter.isEmpty())
				{
					client.setVarcStrValue(
						VarClientID.MESLAYERINPUT, bankSearchTextBeforeFilter);
				}
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
		if (!pluginRunning.get()
			|| !strategyCycleRequestQueued.compareAndSet(false, true))
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			boolean strategyCycleStarted = false;
			try
			{
				if (!pluginRunning.get()
					|| !bankFlow.queueStrategyCycle())
				{
					return;
				}
				strategyCycleStarted = true;
				cycleStrategyOnClientThread();
			}
			finally
			{
				if (strategyCycleStarted)
				{
					bankFlow.completeStrategyCycle();
				}
				strategyCycleRequestQueued.set(false);
			}
		});
	}

	private void adjustSupplyQuantity(
		SupplyRecommendation supply,
		SupplyQuantityAction action)
	{
		if (supply == null || action == null) return;
		clientThread.invokeLater(() ->
		{
			if (!pluginRunning.get()
				|| !recommendations.getSupplies().contains(supply)
				|| recommendations.getProfile() == null)
			{
				return;
			}

			String key = SmartSupplyAdvisor.quantityOverrideKey(
				recommendations.getProfile().getKey(), supply.getCategory());
			if (action == SupplyQuantityAction.AUTO)
			{
				configManager.unsetRSProfileConfiguration(
					SlayerGearAdvisorConfig.GROUP, key);
			}
			else
			{
				String stored = configManager.getRSProfileConfiguration(
					SlayerGearAdvisorConfig.GROUP, key);
				int current = savedSupplyQuantity(stored, supply.getAutomaticQuantity());
				int unit = supply.getAdjustmentUnit();
				int next = adjustedSupplyQuantity(current, unit, supply.isRequired(), action);
				configManager.setRSProfileConfiguration(
					SlayerGearAdvisorConfig.GROUP, key, next);
			}
			recalculateOrMarkBankRefresh();
		});
	}

	static int savedSupplyQuantity(String stored, int automaticQuantity)
	{
		if (stored == null || stored.trim().isEmpty()) return Math.max(0, automaticQuantity);
		try
		{
			return Math.max(0, Integer.parseInt(stored.trim()));
		}
		catch (NumberFormatException ignored)
		{
			return Math.max(0, automaticQuantity);
		}
	}

	static int adjustedSupplyQuantity(
		int current,
		int unit,
		boolean required,
		SupplyQuantityAction action)
	{
		int safeUnit = Math.max(1, unit);
		int minimum = required ? safeUnit : 0;
		if (action == SupplyQuantityAction.INCREASE)
		{
			return Math.max(minimum, current + safeUnit);
		}
		return Math.max(minimum, current - safeUnit);
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
		 * Outside the bank, re-score immediately from the cached snapshots. During
		 * a locked bank session, keep the active withdrawal plan in place and mark
		 * the selected method as waiting for the explicit Refresh action.
		 */
		recalculateOrMarkBankRefresh();
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
