package com.freearcanes.slayergear;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

/**
 * Compact panel for Slayer Best in Bank.
 *
 * The layout is intentionally compact and scan-first: current task, readiness,
 * Tier 1 gear, trip supplies, then optional detail. Backups and long task notes
 * stay collapsed until the player asks for them.
 */
class SlayerGearPanel extends PluginPanel
{
	private static Color PANEL_BG;
	private static Color SURFACE;
	private static Color SURFACE_RAISED;
	private static Color ROW;
	private static Color ROW_HOVER;
	private static Color BORDER;
	private static Color TEXT;
	private static Color SOFT_TEXT;
	private static Color MUTED_TEXT;
	private static Color FAINT_TEXT;
	private static Color GOLD;
	private static Color TEAL;
	private static Color BLUE;
	private static Color SUCCESS;
	private static Color WARNING;
	private static Color DANGER;
	private static Color SCROLL_THUMB;
	private static Color SCROLL_THUMB_ACTIVE;
	private static int CARD_RADIUS;
	private static int ROW_RADIUS;
	private static final int WRAP_WIDTH = 178;

	static
	{
		applyThemeColors(PanelTheme.MIDNIGHT);
	}

	private static final List<EquipmentInventorySlot> SLOT_ORDER = Arrays.asList(
		EquipmentInventorySlot.HEAD,
		EquipmentInventorySlot.CAPE,
		EquipmentInventorySlot.AMULET,
		EquipmentInventorySlot.WEAPON,
		EquipmentInventorySlot.BODY,
		EquipmentInventorySlot.SHIELD,
		EquipmentInventorySlot.LEGS,
		EquipmentInventorySlot.GLOVES,
		EquipmentInventorySlot.BOOTS,
		EquipmentInventorySlot.RING,
		EquipmentInventorySlot.AMMO
	);

	private final ItemManager itemManager;
	private final JPanel content = transparentPanel();
	private Runnable strategyCycleHandler = () -> { };
	private BiConsumer<SupplyRecommendation, SupplyQuantityAction> supplyQuantityHandler =
		(supply, action) -> { };
	private Runnable loadoutRefreshHandler = () -> { };
	private Runnable advisorToggleHandler = () -> { };
	private Runnable turaelAyaSpeedToggleHandler = () -> { };
	private JButton advisorToggleButton;
	private JCheckBox turaelAyaSpeedCheckBox;
	private boolean advisorEnabled = true;
	private boolean turaelAyaSpeedMode;
	private boolean showAlternatives;
	private boolean showTaskDetails;
	private PrepFocusMode prepFocusMode = PrepFocusMode.ALL;
	private GearRecommendations lastRecommendations;
	private PanelTheme panelTheme = PanelTheme.MIDNIGHT;

	@Inject
	SlayerGearPanel(ItemManager itemManager)
	{
		this.itemManager = itemManager;

		setLayout(new BorderLayout(0, 10));
		setBackground(PANEL_BG);
		setBorder(new EmptyBorder(5, 3, 9, 3));
		styleScrollPane();

		add(buildHeader(), BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);
		add(content, BorderLayout.CENTER);

		showEmpty(
			"No Slayer task detected",
			"Best-in-Bank will wake up when RuneLite detects an assignment.");
	}

	void setStrategyCycleHandler(Runnable handler)
	{
		this.strategyCycleHandler = handler == null ? () -> { } : handler;
	}

	void setSupplyQuantityHandler(
		BiConsumer<SupplyRecommendation, SupplyQuantityAction> handler)
	{
		this.supplyQuantityHandler = handler == null ? (supply, action) -> { } : handler;
	}

	void setLoadoutRefreshHandler(Runnable handler)
	{
		this.loadoutRefreshHandler = handler == null ? () -> { } : handler;
	}

	void setAdvisorToggleHandler(Runnable handler)
	{
		this.advisorToggleHandler = handler == null ? () -> { } : handler;
	}

	void setTuraelAyaSpeedToggleHandler(Runnable handler)
	{
		this.turaelAyaSpeedToggleHandler = handler == null ? () -> { } : handler;
	}

	void setAdvisorEnabled(boolean enabled)
	{
		SwingUtilities.invokeLater(() ->
		{
			advisorEnabled = enabled;
			updateAdvisorToggle();
			if (lastRecommendations != null)
			{
				displayOnEdt(lastRecommendations);
			}
		});
	}

	void setTuraelAyaSpeedMode(boolean enabled)
	{
		SwingUtilities.invokeLater(() ->
		{
			turaelAyaSpeedMode = enabled;
			updateTuraelAyaSpeedToggle();
		});
	}

	void setTheme(PanelTheme theme)
	{
		PanelTheme selected = theme == null ? PanelTheme.MIDNIGHT : theme;
		SwingUtilities.invokeLater(() -> setThemeOnEdt(selected));
	}

	void display(GearRecommendations recommendations)
	{
		SwingUtilities.invokeLater(() -> displayOnEdt(recommendations));
	}

	private void setThemeOnEdt(PanelTheme selected)
	{
		if (panelTheme == selected)
		{
			return;
		}

		panelTheme = selected;
		applyThemeColors(selected);
		setBackground(PANEL_BG);
		styleScrollPane();

		removeAll();
		add(buildHeader(), BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
		if (lastRecommendations == null)
		{
			content.removeAll();
			showEmpty(
				"No Slayer task detected",
				"Best-in-Bank will wake up when RuneLite detects an assignment.");
		}
		else
		{
			displayOnEdt(lastRecommendations);
		}
		revalidate();
		repaint();
	}

	private void styleScrollPane()
	{
		JScrollPane scrollPane = getScrollPane();
		if (scrollPane == null)
		{
			return;
		}
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getViewport().setBackground(PANEL_BG);
		JScrollBar bar = scrollPane.getVerticalScrollBar();
		bar.setUI(new ModernScrollBarUI());
		bar.setPreferredSize(new Dimension(7, 0));
		bar.setMinimumSize(new Dimension(7, 0));
		bar.setUnitIncrement(18);
		bar.setBlockIncrement(90);
		bar.setOpaque(false);
	}

	private JPanel buildHeader()
	{
		JPanel root = transparentPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

		JPanel identity = transparentPanel(new BorderLayout(9, 0));
		identity.setAlignmentX(Component.LEFT_ALIGNMENT);
		identity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 49));

		JLabel icon = new JLabel();
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		icon.setPreferredSize(new Dimension(46, 42));
		AsyncBufferedImage helmImage = itemManager.getImage(ItemID.SLAYER_HELM);
		helmImage.onLoaded(() -> SwingUtilities.invokeLater(() ->
			icon.setIcon(new ImageIcon(ImageUtil.resizeImage(helmImage, 44, 40)))));
		identity.add(icon, BorderLayout.WEST);

		JPanel names = transparentPanel();
		names.setLayout(new BoxLayout(names, BoxLayout.X_AXIS));
		JLabel title = new JLabel("Slayer Best in Bank");
		title.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 15f));
		title.setForeground(TEXT);
		title.setVerticalAlignment(SwingConstants.CENTER);
		names.add(title);
		names.add(Box.createHorizontalGlue());

		JButton supportButton = new RoundedButton();
		supportButton.setIcon(new ImageIcon(ImageUtil.resizeImage(
			ImageUtil.loadImageResource(
				SlayerGearPanel.class,
				"/net/runelite/client/plugins/info/discord_icon.png"),
			20,
			20)));
		supportButton.setToolTipText("Open Slayer Best in Bank support Discord");
		supportButton.getAccessibleContext().setAccessibleName(
			"Open Slayer Best in Bank support Discord");
		supportButton.setBorder(new EmptyBorder(4, 4, 4, 4));
		supportButton.setPreferredSize(new Dimension(30, 30));
		supportButton.setMinimumSize(new Dimension(30, 30));
		supportButton.setMaximumSize(new Dimension(30, 30));
		supportButton.addActionListener(event -> SupportLinks.openDiscord());
		names.add(supportButton);
		identity.add(names, BorderLayout.CENTER);

		root.add(identity);
		root.add(Box.createVerticalStrut(4));

		advisorToggleButton = new RoundedButton();
		advisorToggleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		advisorToggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		advisorToggleButton.addActionListener(event -> advisorToggleHandler.run());
		updateAdvisorToggle();
		root.add(advisorToggleButton);
		root.add(Box.createVerticalStrut(4));

		turaelAyaSpeedCheckBox = new JCheckBox("Turael/Aya skipping / boosting");
		turaelAyaSpeedCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		turaelAyaSpeedCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		turaelAyaSpeedCheckBox.setOpaque(false);
		turaelAyaSpeedCheckBox.setFocusPainted(false);
		turaelAyaSpeedCheckBox.setFont(FontManager.getRunescapeSmallFont());
		turaelAyaSpeedCheckBox.addActionListener(event -> turaelAyaSpeedToggleHandler.run());
		updateTuraelAyaSpeedToggle();
		root.add(turaelAyaSpeedCheckBox);
		return root;
	}

	private void updateAdvisorToggle()
	{
		if (advisorToggleButton == null)
		{
			return;
		}
		advisorToggleButton.setText(advisorEnabled ? "Advisor enabled" : "Advisor paused");
		advisorToggleButton.setForeground(advisorEnabled ? SUCCESS : MUTED_TEXT);
		advisorToggleButton.setToolTipText(
			advisorEnabled
				? "Pause recommendations, bank helpers, and prep reminders"
				: "Enable recommendations, bank helpers, and prep reminders");
	}

	private void updateTuraelAyaSpeedToggle()
	{
		if (turaelAyaSpeedCheckBox == null)
		{
			return;
		}
		turaelAyaSpeedCheckBox.setSelected(turaelAyaSpeedMode);
		turaelAyaSpeedCheckBox.setForeground(turaelAyaSpeedMode ? TEAL : SOFT_TEXT);
		turaelAyaSpeedCheckBox.setToolTipText(
			"Use low-level Turael/Aya routes, blowpipe/ranged, cannon where supported, and an Expeditious bracelet");
	}

	private void displayOnEdt(GearRecommendations recommendations)
	{
		content.removeAll();
		lastRecommendations = recommendations;

		switch (recommendations.getState())
		{
			case NO_TASK:
				showEmpty(
					advisorEnabled ? "No Slayer task detected" : "Advisor paused",
					advisorEnabled
						? "Best-in-Bank will wake up when RuneLite detects an assignment."
						: "Use the switch above when you want Best-in-Bank to run.");
				break;
			case UNSUPPORTED_TASK:
				showEmpty(
					taskText(recommendations),
					"This assignment has no curated combat profile yet, so the plugin will not invent a setup.");
				break;
			case OPEN_BANK:
				addTaskHero(recommendations);
				addEmptyCard(
					"Bank scan needed",
					"Open your bank once so Best-in-Bank can inspect the gear you actually own.",
					WARNING);
				addTaskDetailsSection(recommendations);
				break;
			case READY:
				addTaskHero(recommendations);
				addReadiness(recommendations);
				addPrepControls(recommendations);
				if (prepFocusMode == PrepFocusMode.MISSING
					&& isPrepComplete(recommendations.getReadiness()))
				{
					addEmptyCard(
						"Nothing missing",
						"All planned gear and supplies are packed.",
						SUCCESS);
				}
				else
				{
					if (prepFocusMode != PrepFocusMode.SUPPLIES)
					{
						addLoadout(recommendations, prepFocusMode == PrepFocusMode.MISSING);
					}
					if (prepFocusMode != PrepFocusMode.GEAR)
					{
						addSupplies(recommendations, prepFocusMode == PrepFocusMode.MISSING);
					}
				}
				addTaskDetailsSection(recommendations);
				break;
			default:
				break;
		}

		content.revalidate();
		content.repaint();
	}

	private void addTaskHero(GearRecommendations recommendations)
	{
		SlayerTaskProfile profile = recommendations.getProfile();
		GearStrategy strategy = recommendations.getStrategy();

		RoundedPanel hero = card(SURFACE);
		hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
		hero.setBorder(new EmptyBorder(10, 11, 10, 11));

		JLabel eyebrow = smallCaps("CURRENT TASK", MUTED_TEXT);
		hero.add(eyebrow);
		hero.add(Box.createVerticalStrut(3));

		JPanel taskRow = transparentPanel(new BorderLayout(5, 0));
		taskRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel task = new JLabel(shorten(taskText(recommendations), 28));
		task.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 16f));
		task.setForeground(TEXT);
		task.setToolTipText(taskText(recommendations));
		taskRow.add(task, BorderLayout.CENTER);
		if (strategy != null)
		{
			taskRow.add(new StatusPill(strategy.getCombatStyle().name(), colorForStyle(strategy.getCombatStyle())), BorderLayout.EAST);
		}
		hero.add(taskRow);

		if (strategy != null)
		{
			hero.add(Box.createVerticalStrut(7));
			JLabel method = new JLabel(strategy.getName());
			method.setFont(FontManager.getRunescapeBoldFont());
			method.setForeground(GOLD);
			method.setAlignmentX(Component.LEFT_ALIGNMENT);
			hero.add(method);

			if (strategy.getLocation() != null && !strategy.getLocation().trim().isEmpty())
			{
				JLabel location = new JLabel(shorten(strategy.getLocation(), 34));
				location.setFont(FontManager.getRunescapeSmallFont());
				location.setForeground(TEAL);
				location.setToolTipText(strategy.getLocation());
				location.setAlignmentX(Component.LEFT_ALIGNMENT);
				hero.add(location);
			}

			if (strategy.getRationale() != null && !strategy.getRationale().trim().isEmpty())
			{
				hero.add(Box.createVerticalStrut(5));
				hero.add(wrappedLabel(shorten(strategy.getRationale(), 115), SOFT_TEXT, WRAP_WIDTH));
			}

			if (!recommendations.getAlternativeStrategies().isEmpty())
			{
				hero.add(Box.createVerticalStrut(8));
				RoundedButton switchMethod = new RoundedButton();
				switchMethod.setText("Change method   ·   " + recommendations.getAlternativeStrategies().size() + " available");
				switchMethod.setForeground(SOFT_TEXT);
				switchMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
				switchMethod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
				switchMethod.addActionListener(event -> strategyCycleHandler.run());
				hero.add(switchMethod);
			}

			if (recommendations.isBankRefreshPending())
			{
				hero.add(Box.createVerticalStrut(8));
				RoundedPanel notice = new RoundedPanel(SURFACE_RAISED, ROW_RADIUS, WARNING);
				notice.setLayout(new BoxLayout(notice, BoxLayout.Y_AXIS));
				notice.setBorder(new EmptyBorder(7, 8, 7, 8));
				notice.setAlignmentX(Component.LEFT_ALIGNMENT);
				notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
				notice.add(smallCaps("BANK PLAN OUT OF DATE", WARNING));
				notice.add(Box.createVerticalStrut(2));
				notice.add(wrappedLabel(
					"Method or settings changed. Click Refresh below, or reopen the bank, to rebuild this loadout.",
					TEXT,
					158));
				hero.add(notice);
			}
		}
		else if (profile != null)
		{
			hero.add(Box.createVerticalStrut(6));
			hero.add(wrappedLabel(profile.getSummary(), SOFT_TEXT, WRAP_WIDTH));
		}

		content.add(hero);
		content.add(Box.createVerticalStrut(8));
	}

	private void addReadiness(GearRecommendations recommendations)
	{
		ReadinessReport ready = recommendations.getReadiness();
		boolean readyToLeave = ready.isReadyToLeave();
		RoundedPanel panel = card(SURFACE);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(new EmptyBorder(9, 10, 9, 10));

		JPanel headline = transparentPanel(new BorderLayout(6, 0));
		headline.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel status = new JLabel(readyToLeave ? "Ready to leave bank" : "Prep incomplete");
		status.setFont(FontManager.getRunescapeBoldFont());
		status.setForeground(readyToLeave ? SUCCESS : WARNING);
		headline.add(status, BorderLayout.WEST);
		JLabel dot = new JLabel(readyToLeave ? "●" : "●");
		dot.setForeground(readyToLeave ? SUCCESS : WARNING);
		headline.add(dot, BorderLayout.EAST);
		panel.add(headline);
		panel.add(Box.createVerticalStrut(7));

		JPanel metrics = transparentPanel(new GridLayout(1, 2, 6, 0));
		metrics.setAlignmentX(Component.LEFT_ALIGNMENT);
		metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		metrics.add(metricTile("GEAR", ready.getGearPacked() + "/" + ready.getGearTotal(),
			ready.getGearPacked() >= ready.getGearTotal() && ready.getGearTotal() > 0 ? SUCCESS : GOLD));
		metrics.add(metricTile("SUPPLIES", ready.getSuppliesPacked() + "/" + ready.getSuppliesTotal(),
			ready.getSuppliesPacked() >= ready.getSuppliesTotal() ? SUCCESS : BLUE));
		panel.add(metrics);
		panel.add(Box.createVerticalStrut(7));

		int readyCount = ready.getGearPacked() + ready.getSuppliesPacked();
		int totalCount = ready.getGearTotal() + ready.getSuppliesTotal();
		InventoryCapacityPlan inventoryPlan = recommendations.getInventoryPlan();
		String inventoryText = inventoryPlan.isAvailable()
			? " | " + inventoryPlan.getPlannedSlots() + "/"
				+ inventoryPlan.getCapacity() + " slots"
			: "";
		JLabel combined = new JLabel(readyCount + "/" + totalCount
			+ " ready" + inventoryText);
		combined.setFont(FontManager.getRunescapeSmallFont());
		combined.setForeground(
			inventoryPlan.isAvailable() && !inventoryPlan.fits() ? DANGER : SOFT_TEXT);
		combined.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(combined);
		panel.add(Box.createVerticalStrut(6));

		JPanel checks = transparentPanel(new GridLayout(0, 2, 5, 4));
		checks.setAlignmentX(Component.LEFT_ALIGNMENT);
		checks.add(checkLabel("Protection", ready.isProtectionReady()));
		checks.add(checkLabel("Ammo", ready.isWeaponAmmoReady()));
		if (!"Not required".equals(ready.getSpellStatus()))
		{
			JLabel spell = new JLabel(shorten(ready.getSpellStatus(), 30));
			spell.setFont(FontManager.getRunescapeSmallFont());
			spell.setForeground(TEAL);
			spell.setToolTipText(ready.getSpellStatus());
			checks.add(spell);
		}
		panel.add(checks);

		if (inventoryPlan.isAvailable()
			&& (inventoryPlan.wasTrimmed() || !inventoryPlan.fits()))
		{
			panel.add(Box.createVerticalStrut(6));
			String capacityMessage;
			Color capacityColor;
			if (!inventoryPlan.fits())
			{
				capacityMessage = "Inventory plan is over by "
					+ inventoryPlan.getOverBy() + " slot"
					+ (inventoryPlan.getOverBy() == 1 ? "" : "s")
					+ ". Required items were kept.";
				capacityColor = DANGER;
			}
			else
			{
				capacityMessage = "Fit to 28 slots by reducing "
					+ String.join(", ", inventoryPlan.getReductions()) + ".";
				capacityColor = WARNING;
			}
			panel.add(wrappedLabel(capacityMessage, capacityColor, WRAP_WIDTH));
		}

		content.add(panel);

		if (!ready.getMissingCritical().isEmpty())
		{
			content.add(Box.createVerticalStrut(6));
			RoundedPanel alert = card(new Color(48, 36, 29));
			alert.setLayout(new BoxLayout(alert, BoxLayout.Y_AXIS));
			alert.setBorder(new EmptyBorder(8, 10, 8, 10));
			JLabel title = smallCaps("DON'T LEAVE YET", WARNING);
			alert.add(title);
			int count = Math.min(3, ready.getMissingCritical().size());
			for (int i = 0; i < count; i++)
			{
				alert.add(Box.createVerticalStrut(2));
				alert.add(wrappedLabel("• " + ready.getMissingCritical().get(i), SOFT_TEXT, WRAP_WIDTH));
			}
			if (ready.getMissingCritical().size() > count)
			{
				alert.add(Box.createVerticalStrut(2));
				JLabel more = new JLabel("+" + (ready.getMissingCritical().size() - count) + " more");
				more.setFont(FontManager.getRunescapeSmallFont());
				more.setForeground(MUTED_TEXT);
				alert.add(more);
			}
			content.add(alert);
		}
		content.add(Box.createVerticalStrut(10));
	}

	private void addPrepControls(GearRecommendations recommendations)
	{
		JPanel controls = transparentPanel(new GridLayout(1, 2, 6, 0));
		controls.setAlignmentX(Component.LEFT_ALIGNMENT);
		controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

		RoundedButton focus = new RoundedButton();
		focus.setText("View: " + prepFocusMode.getDisplayName());
		focus.setForeground(prepFocusMode == PrepFocusMode.ALL ? MUTED_TEXT : TEAL);
		focus.setToolTipText("Show all prep, missing items, gear, or supplies");
		focus.addActionListener(event ->
		{
			prepFocusMode = prepFocusMode.next();
			refreshLastRecommendations();
		});
		controls.add(focus);

		RoundedButton refresh = new RoundedButton();
		refresh.setText(recommendations.isBankRefreshPending() ? "Refresh *" : "Refresh");
		refresh.setForeground(recommendations.isBankRefreshPending() ? WARNING : MUTED_TEXT);
		refresh.setEnabled(recommendations.isBankPlanLocked());
		refresh.setToolTipText(recommendations.isBankRefreshPending()
			? "Apply waiting task or setting changes and rebuild the locked bank plan"
			: recommendations.isBankPlanLocked()
				? "Rebuild the locked bank plan from the latest bank, inventory, and settings"
				: "Open the bank to lock and refresh a preparation plan");
		refresh.addActionListener(event -> loadoutRefreshHandler.run());
		controls.add(refresh);

		content.add(controls);
		if (recommendations.isBankPlanLocked())
		{
			content.add(Box.createVerticalStrut(3));
			JLabel lock = new JLabel(recommendations.isBankRefreshPending()
				? "Bank plan locked | changes waiting"
				: "Bank plan locked | click Refresh to rebuild");
			lock.setFont(FontManager.getRunescapeSmallFont());
			lock.setForeground(recommendations.isBankRefreshPending() ? WARNING : FAINT_TEXT);
			lock.setAlignmentX(Component.LEFT_ALIGNMENT);
			content.add(lock);
		}
		content.add(Box.createVerticalStrut(10));
	}

	private JPanel metricTile(String label, String value, Color valueColor)
	{
		RoundedPanel tile = new RoundedPanel(SURFACE_RAISED, ROW_RADIUS, BORDER);
		tile.setLayout(new BorderLayout(2, 0));
		tile.setBorder(new EmptyBorder(6, 8, 6, 8));
		JLabel name = smallCaps(label, MUTED_TEXT);
		JLabel number = new JLabel(value, SwingConstants.RIGHT);
		number.setFont(FontManager.getRunescapeBoldFont());
		number.setForeground(valueColor);
		tile.add(name, BorderLayout.WEST);
		tile.add(number, BorderLayout.EAST);
		return tile;
	}

	private JLabel checkLabel(String name, boolean ok)
	{
		JLabel label = new JLabel((ok ? "✓ " : "! ") + name);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ok ? SUCCESS : WARNING);
		return label;
	}

	private void addLoadout(GearRecommendations recommendations, boolean missingOnly)
	{
		if (missingOnly)
		{
			boolean missingGear = false;
			for (EquipmentInventorySlot slot : SLOT_ORDER)
			{
				List<GearRecommendation> choices = recommendations.get(slot);
				if (hasTierOneChoice(choices) && !choices.get(0).isPacked())
				{
					missingGear = true;
					break;
				}
			}
			boolean missingCannon = recommendations.getSupplies().stream()
				.anyMatch(supply -> "Cannon setup".equals(supply.getCategory())
					&& !isSupplyReady(supply));
			if (!missingGear && !missingCannon)
			{
				return;
			}
		}

		String loadoutSubtitle = "T2/T3 show swaps only";
		if (!recommendations.getLoadoutTiers().isEmpty())
		{
			LoadoutTier tierOne = recommendations.getLoadoutTiers().get(0);
			if (tierOne.getRiskCapGp() > 0)
			{
				loadoutSubtitle = "Low risk  ~" + compactGp(tierOne.getGuidePrice())
					+ " / " + compactGp(tierOne.getRiskCapGp());
			}
		}
		JPanel heading = sectionHeading("LOADOUT", loadoutSubtitle);
		RoundedButton alternatives = new RoundedButton();
		alternatives.setText(showAlternatives ? "Hide backups" : "Show backups");
		alternatives.setForeground(showAlternatives ? TEAL : MUTED_TEXT);
		alternatives.setPreferredSize(new Dimension(78, 25));
		alternatives.setMargin(new Insets(2, 6, 2, 6));
		alternatives.addActionListener(event ->
		{
			showAlternatives = !showAlternatives;
			refreshLastRecommendations();
		});
		heading.add(alternatives, BorderLayout.EAST);
		content.add(heading);
		content.add(Box.createVerticalStrut(5));

		List<GearRecommendation> weapons = recommendations.get(EquipmentInventorySlot.WEAPON);
		boolean topWeaponIsTwoHanded = !weapons.isEmpty() && weapons.get(0).isTwoHanded();
		for (EquipmentInventorySlot slot : SLOT_ORDER)
		{
			List<GearRecommendation> choices = recommendations.get(slot);
			if (choices.isEmpty())
			{
				continue;
			}
			boolean tierOneChoice = hasTierOneChoice(choices);
			if (missingOnly && (!tierOneChoice || choices.get(0).isPacked()))
			{
				continue;
			}
			if (!showAlternatives && !tierOneChoice)
			{
				continue;
			}
			String suffix = slot == EquipmentInventorySlot.SHIELD && topWeaponIsTwoHanded ? " · 1H setup" : "";
			if (!tierOneChoice)
			{
				suffix += " · T" + choices.get(0).getRank() + " swap";
			}
			content.add(buildSlotCard(slotName(slot) + suffix, choices));
			content.add(Box.createVerticalStrut(5));
		}

		// A dwarf multicannon is ground equipment rather than wearable equipment, but
		// when the selected method calls for one it is still part of the Tier 1
		// loadout. Keep all four required components beside the recommended gear
		// instead of burying them among optional consumables.
		List<SupplyRecommendation> cannonSet = new ArrayList<>();
		for (SupplyRecommendation supply : recommendations.getSupplies())
		{
			if ("Cannon setup".equals(supply.getCategory())
				&& (!missingOnly || !isSupplyReady(supply)))
			{
				cannonSet.add(supply);
			}
		}
		if (!cannonSet.isEmpty())
		{
			content.add(Box.createVerticalStrut(2));
			content.add(sectionHeading("CANNON SET", "Tier 1 utility · all 4 required"));
			content.add(Box.createVerticalStrut(4));
			for (SupplyRecommendation part : cannonSet)
			{
				content.add(buildSupplyRow(part));
				content.add(Box.createVerticalStrut(4));
			}
		}
		content.add(Box.createVerticalStrut(5));
	}

	static boolean hasTierOneChoice(List<GearRecommendation> choices)
	{
		return choices != null
			&& !choices.isEmpty()
			&& choices.get(0).getRank() == 1;
	}

	private JPanel buildSlotCard(String slotName, List<GearRecommendation> choices)
	{
		GearRecommendation best = choices.get(0);
		Color restingFill = best.isPacked() ? SURFACE : ROW;
		RoundedPanel card = new RoundedPanel(restingFill, ROW_RADIUS, BORDER);
		card.setLayout(new BorderLayout(8, 0));
		card.setBorder(new EmptyBorder(7, 8, 7, 8));
		card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		card.setToolTipText("Click for why this item was selected");

		JLabel icon = new JLabel();
		icon.setPreferredSize(new Dimension(34, 34));
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		if (best.getItemId() > 0)
		{
			itemManager.getImage(best.getItemId()).addTo(icon);
		}
		card.add(icon, BorderLayout.WEST);

		JPanel center = transparentPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		JLabel slot = smallCaps(slotName.toUpperCase(), MUTED_TEXT);
		center.add(slot);
		JLabel item = new JLabel(shorten(best.getItemName(), 27));
		item.setFont(FontManager.getRunescapeBoldFont());
		item.setForeground(best.isPacked() ? SOFT_TEXT : TEXT);
		item.setToolTipText(best.getItemName());
		center.add(item);

		if (showAlternatives && choices.size() > 1)
		{
			JPanel backups = transparentPanel();
			backups.setLayout(new BoxLayout(backups, BoxLayout.Y_AXIS));
			backups.add(Box.createVerticalStrut(4));
			for (int index = 1; index < choices.size(); index++)
			{
				GearRecommendation choice = choices.get(index);
				JLabel fallback = new JLabel("T" + choice.getRank() + "  " + shorten(choice.getItemName(), 24));
				fallback.setFont(FontManager.getRunescapeSmallFont());
				fallback.setForeground(choice.getRank() == 2 ? TEAL : MUTED_TEXT);
				fallback.setToolTipText(choice.getItemName() + " — " + choice.getReason());
				backups.add(fallback);
			}
			center.add(backups);
		}

		JPanel detail = transparentPanel();
		detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
		detail.setVisible(false);
		detail.add(Box.createVerticalStrut(4));
		detail.add(wrappedLabel(best.getReason(), MUTED_TEXT, 128));
		center.add(detail);
		card.add(center, BorderLayout.CENTER);

		JPanel right = transparentPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		StatusPill status = gearStatus(best);
		status.setAlignmentX(Component.RIGHT_ALIGNMENT);
		right.add(status);
		right.add(Box.createVerticalGlue());
		JLabel chevron = new JLabel("›");
		chevron.setFont(FontManager.getRunescapeBoldFont().deriveFont(16f));
		chevron.setForeground(FAINT_TEXT);
		chevron.setAlignmentX(Component.RIGHT_ALIGNMENT);
		right.add(chevron);
		card.add(right, BorderLayout.EAST);

		card.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent event)
			{
				card.setFill(ROW_HOVER);
			}

			@Override
			public void mouseExited(MouseEvent event)
			{
				card.setFill(restingFill);
			}

			@Override
			public void mouseClicked(MouseEvent event)
			{
				detail.setVisible(!detail.isVisible());
				chevron.setText(detail.isVisible() ? "⌄" : "›");
				card.revalidate();
				card.repaint();
			}
		});
		return card;
	}

	private StatusPill gearStatus(GearRecommendation recommendation)
	{
		if (recommendation.isPacked())
		{
			return new StatusPill("R", SUCCESS);
		}
		if (recommendation.isBanked())
		{
			return new StatusPill("B", BLUE);
		}
		return new StatusPill("T" + recommendation.getRank(), GOLD);
	}

	private void addSupplies(GearRecommendations recommendations, boolean missingOnly)
	{
		List<SupplyRecommendation> tripSupplies = new ArrayList<>();
		int packed = 0;
		int enabled = 0;
		for (SupplyRecommendation supply : recommendations.getSupplies())
		{
			if ("Cannon setup".equals(supply.getCategory())) continue;
			if (missingOnly
				&& (!supply.isEnabledForTrip() || isSupplyReady(supply)))
			{
				continue;
			}
			tripSupplies.add(supply);
			if (supply.isEnabledForTrip())
			{
				enabled++;
				if (supply.getStatus().isPacked() && supply.hasRecommendedQuantityPacked()) packed++;
			}
		}
		if (tripSupplies.isEmpty())
		{
			return;
		}

		content.add(sectionHeading("TRIP SUPPLIES", packed + "/" + enabled + " packed"));
		content.add(Box.createVerticalStrut(5));

		RoundedPanel card = card(SURFACE);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(new EmptyBorder(5, 7, 5, 7));

		boolean first = true;
		for (SupplyRecommendation supply : tripSupplies)
		{
			if (!first)
			{
				card.add(Box.createVerticalStrut(3));
			}
			first = false;
			card.add(buildSupplyRow(supply));
		}
		content.add(card);
		content.add(Box.createVerticalStrut(10));
	}

	private JPanel buildSupplyRow(SupplyRecommendation supply)
	{
		boolean ready = isSupplyReady(supply);
		RoundedPanel row = new RoundedPanel(ready ? SURFACE : ROW, ROW_RADIUS, null);
		row.setLayout(new BorderLayout(7, 0));
		row.setBorder(new EmptyBorder(6, 7, 6, 7));
		String quantityDetails = supply.hasQuantityTarget()
			? " Packed: " + supply.getPackedQuantity()
				+ "; bank: " + supply.getBankQuantity()
				+ "; estimated target: " + supply.getRecommendedQuantity()
				+ " " + supply.getQuantityUnit() + "."
				+ (supply.isCapacityAdjusted()
					? " Capacity guard reduced the requested target from "
						+ supply.getRequestedQuantity() + "."
					: "")
			: "";
		row.setToolTipText(supply.getReason() + quantityDetails);

		JLabel icon = new JLabel();
		icon.setPreferredSize(new Dimension(30, 30));
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		if (supply.getItemId() > 0)
		{
			itemManager.getImage(supply.getItemId()).addTo(icon);
		}
		row.add(icon, BorderLayout.WEST);

		JPanel text = transparentPanel();
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		JLabel name = new JLabel(shorten(supply.getItemName(), 25));
		name.setFont(FontManager.getRunescapeBoldFont());
		name.setForeground(ready ? SOFT_TEXT : TEXT);
		name.setToolTipText(supply.getItemName());
		text.add(name);
		String quantity = supply.hasQuantityTarget()
			? "  •  " + supply.getPackedQuantity() + "/" + supply.getRecommendedQuantity()
				+ " " + supply.getQuantityUnit()
			: "";
		JLabel category = new JLabel(supply.getCategory()
			+ (supply.isRequired() ? "  •  required" : "")
			+ (supply.isCapacityAdjusted() ? "  •  capacity fit" : "")
			+ quantity);
		category.setFont(FontManager.getRunescapeSmallFont());
		category.setForeground(supply.isRequired() ? WARNING : MUTED_TEXT);
		text.add(category);
		row.add(text, BorderLayout.CENTER);
		JPanel right = transparentPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		StatusPill status = supplyStatus(supply);
		status.setAlignmentX(Component.RIGHT_ALIGNMENT);
		right.add(status);
		if (supply.isQuantityAdjustable())
		{
			right.add(Box.createVerticalStrut(3));
			JPanel controls = transparentPanel(new GridLayout(1, 3, 2, 0));
			controls.setAlignmentX(Component.RIGHT_ALIGNMENT);
			controls.setMaximumSize(new Dimension(69, 18));
			controls.add(quantityButton("−", supply, SupplyQuantityAction.DECREASE));
			controls.add(quantityButton("A", supply, SupplyQuantityAction.AUTO));
			controls.add(quantityButton("+", supply, SupplyQuantityAction.INCREASE));
			right.add(controls);
		}
		row.add(right, BorderLayout.EAST);
		return row;
	}

	private static boolean isSupplyReady(SupplyRecommendation supply)
	{
		return !supply.isEnabledForTrip()
			|| (supply.getStatus().isPacked()
				&& supply.hasRecommendedQuantityPacked());
	}

	private static boolean isPrepComplete(ReadinessReport readiness)
	{
		return readiness.getGearTotal() > 0
			&& readiness.getGearPacked() >= readiness.getGearTotal()
			&& readiness.getSuppliesPacked() >= readiness.getSuppliesTotal();
	}

	private JButton quantityButton(
		String text,
		SupplyRecommendation supply,
		SupplyQuantityAction action)
	{
		JButton button = new JButton(text);
		button.setFont(FontManager.getRunescapeSmallFont());
		button.setForeground(SOFT_TEXT);
		button.setBackground(SURFACE_RAISED);
		button.setBorder(new EmptyBorder(1, 3, 1, 3));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);
		button.setToolTipText(action == SupplyQuantityAction.AUTO
			? "Reset this task supply to Auto"
			: action == SupplyQuantityAction.INCREASE
				? "Increase this task supply"
				: "Decrease this task supply");
		button.addActionListener(event -> supplyQuantityHandler.accept(supply, action));
		return button;
	}

	private StatusPill supplyStatus(SupplyRecommendation supply)
	{
		if (!supply.isEnabledForTrip())
		{
			return new StatusPill("OFF", MUTED_TEXT);
		}
		if (supply.hasQuantityTarget() && supply.hasRecommendedQuantityPacked())
		{
			return new StatusPill("R", SUCCESS);
		}
		if (supply.hasQuantityTarget() && supply.getStatus() != SupplyStatus.MISSING)
		{
			boolean countUnits = "shots".equals(supply.getQuantityUnit());
			int needed = countUnits
				? supply.getQuantityStillNeeded()
				: supply.getWithdrawalsStillNeeded();
			StatusPill shortage = new StatusPill("+" + needed, WARNING);
			shortage.setToolTipText(countUnits
				? "Need " + needed + " more " + supply.getQuantityUnit()
				: "Need " + needed + " more withdrawal"
					+ (needed == 1 ? "" : "s"));
			return shortage;
		}
		switch (supply.getStatus())
		{
			case PACKED_BANKED:
				return new StatusPill("R", SUCCESS);
			case PACKED:
				return new StatusPill("R", SUCCESS);
			case BANKED:
				return new StatusPill("B", BLUE);
			case MISSING:
			default:
				return new StatusPill("X", DANGER);
		}
	}

	private void addTaskDetailsSection(GearRecommendations recommendations)
	{
		if (recommendations.getProfile() == null)
		{
			return;
		}

		RoundedButton toggle = new RoundedButton();
		toggle.setText(showTaskDetails ? "Hide task notes & safety" : "Task notes & safety");
		toggle.setForeground(showTaskDetails ? TEAL : SOFT_TEXT);
		toggle.setAlignmentX(Component.LEFT_ALIGNMENT);
		toggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		toggle.addActionListener(event ->
		{
			showTaskDetails = !showTaskDetails;
			refreshLastRecommendations();
		});
		content.add(toggle);

		if (!showTaskDetails)
		{
			return;
		}
		content.add(Box.createVerticalStrut(5));

		RoundedPanel notes = card(SURFACE);
		notes.setLayout(new BoxLayout(notes, BoxLayout.Y_AXIS));
		notes.setBorder(new EmptyBorder(9, 10, 9, 10));

		notes.add(smallCaps("TASK NOTES", MUTED_TEXT));
		notes.add(Box.createVerticalStrut(3));
		notes.add(wrappedLabel(recommendations.getProfile().getSummary(), SOFT_TEXT, WRAP_WIDTH));
		notes.add(Box.createVerticalStrut(8));
		notes.add(smallCaps("SAFETY", WARNING));
		notes.add(Box.createVerticalStrut(3));
		notes.add(wrappedLabel(recommendations.getProfile().getProtectionAdvice(), SOFT_TEXT, WRAP_WIDTH));

		if (!recommendations.getAssignableMasters().isEmpty())
		{
			notes.add(Box.createVerticalStrut(8));
			notes.add(smallCaps("ASSIGNED BY", TEAL));
			notes.add(Box.createVerticalStrut(3));
			notes.add(wrappedLabel(String.join("  •  ", recommendations.getAssignableMasters()), MUTED_TEXT, WRAP_WIDTH));
		}

		if (!recommendations.getAlternativeStrategies().isEmpty())
		{
			notes.add(Box.createVerticalStrut(8));
			notes.add(smallCaps("OTHER OWNED METHODS", MUTED_TEXT));
			notes.add(Box.createVerticalStrut(3));
			notes.add(wrappedLabel(joinStrategyNames(recommendations.getAlternativeStrategies()), MUTED_TEXT, WRAP_WIDTH));
		}
		content.add(notes);
		content.add(Box.createVerticalStrut(6));

		JLabel disclaimer = wrappedLabel(
			"Recommendations are bank-aware and safety-aware. Verify unusual boss mechanics, charges, and account-specific unlocks.",
			FAINT_TEXT,
			184);
		disclaimer.setBorder(new EmptyBorder(0, 3, 0, 3));
		content.add(disclaimer);
	}

	private JPanel sectionHeading(String title, String subtitle)
	{
		JPanel heading = transparentPanel(new BorderLayout(6, 0));
		heading.setAlignmentX(Component.LEFT_ALIGNMENT);
		heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 31));
		JPanel text = transparentPanel();
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		JLabel titleLabel = smallCaps(title, ColorScheme.LIGHT_GRAY_COLOR);
		text.add(titleLabel);
		if (subtitle != null && !subtitle.isEmpty())
		{
			JLabel subtitleLabel = new JLabel(subtitle);
			subtitleLabel.setFont(FontManager.getRunescapeSmallFont());
			subtitleLabel.setForeground(FAINT_TEXT);
			text.add(subtitleLabel);
		}
		heading.add(text, BorderLayout.CENTER);
		return heading;
	}

	private void showEmpty(String title, String message)
	{
		content.removeAll();
		addEmptyCard(title, message, MUTED_TEXT);
	}

	private void addEmptyCard(String title, String message, Color accent)
	{
		RoundedPanel empty = card(SURFACE);
		empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
		empty.setBorder(new EmptyBorder(14, 12, 14, 12));
		JLabel heading = new JLabel(title);
		heading.setFont(FontManager.getRunescapeBoldFont());
		heading.setForeground(accent);
		heading.setAlignmentX(Component.LEFT_ALIGNMENT);
		empty.add(heading);
		empty.add(Box.createVerticalStrut(5));
		empty.add(wrappedLabel(message, SOFT_TEXT, WRAP_WIDTH));
		content.add(empty);
	}

	private void refreshLastRecommendations()
	{
		if (lastRecommendations != null)
		{
			displayOnEdt(lastRecommendations);
		}
	}

	private static RoundedPanel card(Color fill)
	{
		RoundedPanel panel = new RoundedPanel(fill, CARD_RADIUS, BORDER);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		return panel;
	}

	private static JLabel smallCaps(String text, Color color)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
		label.setForeground(color);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private static JPanel transparentPanel()
	{
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		return panel;
	}

	private static JPanel transparentPanel(java.awt.LayoutManager layout)
	{
		JPanel panel = new JPanel(layout);
		panel.setOpaque(false);
		return panel;
	}

	private static JLabel wrappedLabel(String text, Color color, int width)
	{
		JLabel label = new JLabel("<html><body style='width:" + width + "px'>" + escape(text) + "</body></html>");
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(color);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private static String taskText(GearRecommendations recommendations)
	{
		String amount = recommendations.getTaskAmount() > 0 ? "  × " + recommendations.getTaskAmount() : "";
		return recommendations.getTaskName() + amount;
	}

	private static String slotName(EquipmentInventorySlot slot)
	{
		switch (slot)
		{
			case AMULET:
				return "Neck";
			case SHIELD:
				return "Off-hand";
			default:
				String lower = slot.name().toLowerCase();
				return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
		}
	}

	private static Color colorForStyle(CombatStyle style)
	{
		switch (style)
		{
			case MAGIC:
				return new Color(118, 157, 245);
			case RANGED:
				return new Color(99, 199, 109);
			case MELEE:
			default:
				return new Color(229, 110, 99);
		}
	}

	private static String joinStrategyNames(List<GearStrategy> strategies)
	{
		StringBuilder result = new StringBuilder();
		for (GearStrategy strategy : strategies)
		{
			if (result.length() > 0)
			{
				result.append("  •  ");
			}
			result.append(strategy.getName());
		}
		return result.toString();
	}

	private static String shorten(String value, int max)
	{
		if (value == null || value.length() <= max)
		{
			return value == null ? "" : value;
		}
		return value.substring(0, Math.max(1, max - 1)) + "…";
	}

	private static String compactGp(int value)
	{
		if (value >= 1_000_000)
		{
			double millions = value / 1_000_000.0;
			return (millions >= 10 ? Integer.toString((int) millions)
				: String.format(Locale.ENGLISH, "%.1f", millions)) + "m";
		}
		if (value >= 1_000)
		{
			return (value / 1_000) + "k";
		}
		return Integer.toString(Math.max(0, value));
	}

	private static String escape(String value)
	{
		if (value == null)
		{
			return "";
		}
		return value
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&#39;");
	}

	private static final class StatusPill extends JLabel
	{
		private final Color accent;

		private StatusPill(String text, Color accent)
		{
			super(text);
			this.accent = accent;
			setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
			setForeground(accent);
			setHorizontalAlignment(SwingConstants.CENTER);
			setOpaque(false);
			setBorder(new EmptyBorder(3, 6, 3, 6));
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(withAlpha(accent, 28));
			g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
			g.dispose();
			super.paintComponent(graphics);
		}
	}

	private static final class RoundedButton extends JButton
	{
		private boolean hovered;

		private RoundedButton()
		{
			setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
			setFocusPainted(false);
			setBorderPainted(false);
			setContentAreaFilled(false);
			setOpaque(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setBorder(new EmptyBorder(5, 9, 5, 9));
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseEntered(MouseEvent event)
				{
					hovered = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent event)
				{
					hovered = false;
					repaint();
				}
			});
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(hovered ? SURFACE_RAISED : SURFACE);
			g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
			g.setColor(BORDER);
			g.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), 10, 10);
			g.dispose();
			super.paintComponent(graphics);
		}
	}

	private static final class RoundedPanel extends JPanel
	{
		private Color fill;
		private final int radius;
		private final Color stroke;

		private RoundedPanel(Color fill, int radius, Color stroke)
		{
			this.fill = fill;
			this.radius = radius;
			this.stroke = stroke;
			setOpaque(false);
			setAlignmentX(Component.LEFT_ALIGNMENT);
			setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		}

		private void setFill(Color fill)
		{
			this.fill = fill;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(fill);
			g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
			if (stroke != null)
			{
				g.setColor(stroke);
				g.setStroke(new BasicStroke(1f));
				g.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), radius, radius);
			}
			g.dispose();
			super.paintComponent(graphics);
		}
	}

	private static final class ModernScrollBarUI extends BasicScrollBarUI
	{
		@Override
		protected void configureScrollBarColors()
		{
			trackColor = PANEL_BG;
			thumbColor = SCROLL_THUMB;
		}

		@Override
		protected JButton createDecreaseButton(int orientation)
		{
			return zeroButton();
		}

		@Override
		protected JButton createIncreaseButton(int orientation)
		{
			return zeroButton();
		}

		@Override
		protected void paintTrack(Graphics graphics, JComponent component, java.awt.Rectangle bounds)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setColor(PANEL_BG);
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g.dispose();
		}

		@Override
		protected void paintThumb(Graphics graphics, JComponent component, java.awt.Rectangle bounds)
		{
			if (!component.isEnabled() || bounds.width <= 0 || bounds.height <= 0)
			{
				return;
			}
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(isDragging ? SCROLL_THUMB_ACTIVE : SCROLL_THUMB);
			int x = bounds.x + 1;
			int width = Math.max(3, bounds.width - 2);
			g.fillRoundRect(x, bounds.y + 1, width, Math.max(4, bounds.height - 2), 7, 7);
			g.dispose();
		}

		private static JButton zeroButton()
		{
			JButton button = new JButton();
			button.setPreferredSize(new Dimension(0, 0));
			button.setMinimumSize(new Dimension(0, 0));
			button.setMaximumSize(new Dimension(0, 0));
			return button;
		}
	}

	private static void applyThemeColors(PanelTheme theme)
	{
		PANEL_BG = theme.panelBackground;
		SURFACE = theme.surface;
		SURFACE_RAISED = theme.raisedSurface;
		ROW = theme.row;
		ROW_HOVER = theme.rowHover;
		BORDER = theme.border;
		TEXT = theme.text;
		SOFT_TEXT = theme.softText;
		MUTED_TEXT = theme.mutedText;
		FAINT_TEXT = theme.faintText;
		GOLD = theme.gold;
		TEAL = theme.teal;
		BLUE = theme.blue;
		SUCCESS = theme.success;
		WARNING = theme.warning;
		DANGER = theme.danger;
		SCROLL_THUMB = theme.scrollThumb;
		SCROLL_THUMB_ACTIVE = theme.activeScrollThumb;
		CARD_RADIUS = theme.cardRadius;
		ROW_RADIUS = theme.rowRadius;
	}

	private static Color withAlpha(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
