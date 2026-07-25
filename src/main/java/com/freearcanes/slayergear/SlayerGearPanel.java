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
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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

/**
 * Beta 2 panel refresh for Slayer Best-in-Bank.
 *
 * The layout is intentionally compact and scan-first: current task, readiness,
 * Tier 1 gear, trip supplies, then optional detail. Backups and long task notes
 * stay collapsed until the player asks for them.
 */
class SlayerGearPanel extends PluginPanel
{
	private static final Color PANEL_BG = new Color(25, 26, 29);
	private static final Color SURFACE = new Color(31, 33, 37);
	private static final Color SURFACE_RAISED = new Color(36, 38, 43);
	private static final Color ROW = new Color(34, 36, 40);
	private static final Color ROW_HOVER = new Color(41, 44, 49);
	private static final Color BORDER = new Color(50, 53, 59);
	private static final Color TEXT = new Color(238, 239, 241);
	private static final Color SOFT_TEXT = new Color(193, 196, 201);
	private static final Color MUTED_TEXT = new Color(132, 136, 144);
	private static final Color FAINT_TEXT = new Color(96, 100, 108);
	private static final Color GOLD = new Color(239, 181, 74);
	private static final Color TEAL = new Color(72, 201, 190);
	private static final Color BLUE = new Color(100, 157, 235);
	private static final Color SUCCESS = new Color(97, 194, 123);
	private static final Color WARNING = new Color(231, 166, 74);
	private static final Color DANGER = new Color(220, 103, 93);
	private static final int CARD_RADIUS = 12;
	private static final int ROW_RADIUS = 9;
	private static final int WRAP_WIDTH = 178;

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
	private final JButton highlightButton = new RoundedButton();
	private Runnable toggleHandler = () -> { };
	private Runnable strategyCycleHandler = () -> { };
	private boolean highlightsActive;
	private boolean showAlternatives;
	private boolean showTaskDetails;
	private GearRecommendations lastRecommendations;

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

	void setToggleHandler(Runnable toggleHandler)
	{
		this.toggleHandler = toggleHandler == null ? () -> { } : toggleHandler;
	}

	void setStrategyCycleHandler(Runnable handler)
	{
		this.strategyCycleHandler = handler == null ? () -> { } : handler;
	}

	void updateHighlights(boolean active)
	{
		highlightsActive = active;
		SwingUtilities.invokeLater(this::refreshHighlightButton);
	}

	void display(GearRecommendations recommendations)
	{
		lastRecommendations = recommendations;
		SwingUtilities.invokeLater(() -> displayOnEdt(recommendations));
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
		identity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));

		JLabel icon = new JLabel();
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		icon.setPreferredSize(new Dimension(34, 34));
		itemManager.getImage(ItemID.SLAYER_HELM).addTo(icon);
		identity.add(icon, BorderLayout.WEST);

		JPanel names = transparentPanel();
		names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
		JLabel title = new JLabel("Slayer Best-in-Bank");
		title.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 15f));
		title.setForeground(TEXT);
		JLabel subtitle = new JLabel("BETA 2  •  owned loadout optimizer");
		subtitle.setFont(FontManager.getRunescapeSmallFont());
		subtitle.setForeground(MUTED_TEXT);
		names.add(title);
		names.add(Box.createVerticalStrut(1));
		names.add(subtitle);
		identity.add(names, BorderLayout.CENTER);
		root.add(identity);
		root.add(Box.createVerticalStrut(6));

		highlightButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		highlightButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		highlightButton.addActionListener(event -> toggleHandler.run());
		refreshHighlightButton();
		root.add(highlightButton);
		return root;
	}

	private void refreshHighlightButton()
	{
		highlightButton.setText(highlightsActive ? "●  Bank highlights on" : "○  Bank highlights off");
		highlightButton.setForeground(highlightsActive ? SUCCESS : MUTED_TEXT);
		highlightButton.setToolTipText(highlightsActive
			? "Hide recommendation markers in the normal bank"
			: "Show recommendation markers in the normal bank");
	}

	private void displayOnEdt(GearRecommendations recommendations)
	{
		content.removeAll();
		lastRecommendations = recommendations;

		switch (recommendations.getState())
		{
			case NO_TASK:
				showEmpty(
					"No Slayer task detected",
					"Best-in-Bank will wake up when RuneLite detects an assignment.");
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
				addLoadout(recommendations);
				addSupplies(recommendations);
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

	private void addLoadout(GearRecommendations recommendations)
	{
		JPanel heading = sectionHeading("LOADOUT", "Tier 1 first");
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
			String suffix = slot == EquipmentInventorySlot.SHIELD && topWeaponIsTwoHanded ? " · 1H setup" : "";
			content.add(buildSlotCard(slotName(slot) + suffix, choices));
			content.add(Box.createVerticalStrut(5));
		}
		content.add(Box.createVerticalStrut(5));
	}

	private JPanel buildSlotCard(String slotName, List<GearRecommendation> choices)
	{
		GearRecommendation best = choices.get(0);
		RoundedPanel card = new RoundedPanel(ROW, ROW_RADIUS, BORDER);
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
		item.setForeground(TEXT);
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
				card.setFill(ROW);
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
			return new StatusPill("PACKED", SUCCESS);
		}
		if (recommendation.isBanked())
		{
			return new StatusPill("BANK", BLUE);
		}
		return new StatusPill("T" + recommendation.getRank(), GOLD);
	}

	private void addSupplies(GearRecommendations recommendations)
	{
		if (recommendations.getSupplies().isEmpty())
		{
			return;
		}

		ReadinessReport ready = recommendations.getReadiness();
		content.add(sectionHeading("TRIP SUPPLIES", ready.getSuppliesPacked() + "/" + ready.getSuppliesTotal() + " packed"));
		content.add(Box.createVerticalStrut(5));

		RoundedPanel card = card(SURFACE);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(new EmptyBorder(5, 7, 5, 7));

		boolean first = true;
		for (SupplyRecommendation supply : recommendations.getSupplies())
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
		RoundedPanel row = new RoundedPanel(ROW, ROW_RADIUS, null);
		row.setLayout(new BorderLayout(7, 0));
		row.setBorder(new EmptyBorder(6, 7, 6, 7));
		row.setToolTipText(supply.getReason());

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
		name.setForeground(TEXT);
		name.setToolTipText(supply.getItemName());
		text.add(name);
		JLabel category = new JLabel(supply.getCategory() + (supply.isRequired() ? "  •  required" : ""));
		category.setFont(FontManager.getRunescapeSmallFont());
		category.setForeground(supply.isRequired() ? WARNING : MUTED_TEXT);
		text.add(category);
		row.add(text, BorderLayout.CENTER);
		row.add(supplyStatus(supply), BorderLayout.EAST);
		return row;
	}

	private StatusPill supplyStatus(SupplyRecommendation supply)
	{
		switch (supply.getStatus())
		{
			case PACKED_BANKED:
				return new StatusPill("PACKED+", SUCCESS);
			case PACKED:
				return new StatusPill("PACKED", SUCCESS);
			case BANKED:
				return new StatusPill("BANK", BLUE);
			case MISSING:
			default:
				return new StatusPill("MISSING", DANGER);
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
			thumbColor = new Color(82, 85, 92);
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
			g.setColor(isDragging ? new Color(118, 122, 132) : new Color(78, 81, 89));
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

	private static Color withAlpha(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
