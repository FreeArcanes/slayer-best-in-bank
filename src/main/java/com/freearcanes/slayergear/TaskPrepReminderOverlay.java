package com.freearcanes.slayergear;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class TaskPrepReminderOverlay extends Overlay
{
	private static final long DISPLAY_MILLIS = 6_500L;
	private static final long FADE_MILLIS = 1_200L;
	private static final int HEIGHT = 52;
	private static final int MIN_WIDTH = 285;
	private static final int MAX_WIDTH = 440;
	private static final Color GOLD = new Color(224, 174, 73);
	private static final Color TEXT = new Color(238, 232, 213);
	private static final Color BACKGROUND = new Color(24, 20, 14);

	private final SlayerGearAdvisorConfig config;
	private String title;
	private String message;
	private long expiresAt;

	@Inject
	TaskPrepReminderOverlay(SlayerGearAdvisorPlugin plugin, SlayerGearAdvisorConfig config)
	{
		super(plugin);
		this.config = config;
		setPosition(OverlayPosition.TOP_CENTER);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_HIGH);
		setMovable(false);
	}

	void show(GearRecommendations recommendations)
	{
		hide();
		if (!config.prepReminderEnabled() || recommendations == null
			|| recommendations.getState() != GearRecommendations.State.READY) return;

		ReadinessReport readiness = recommendations.getReadiness();
		List<String> reminders = new ArrayList<>();
		for (String critical : readiness.getMissingCritical())
		{
			reminders.add(critical);
			if (reminders.size() == 2) break;
		}
		if (reminders.isEmpty())
		{
			for (SupplyRecommendation supply : recommendations.getSupplies())
			{
				if (shouldRemindSupply(supply))
				{
					String quantity = "";
					if (supply.hasQuantityTarget())
					{
						quantity = "shots".equals(supply.getQuantityUnit())
							? supply.getQuantityStillNeeded() + " "
							: supply.getWithdrawalsStillNeeded() + "× ";
					}
					reminders.add(quantity + supply.getItemName());
					if (reminders.size() == 2) break;
				}
			}
		}
		if (reminders.isEmpty() && readiness.getGearPacked() < readiness.getGearTotal())
			reminders.add("Gear " + readiness.getGearPacked() + "/" + readiness.getGearTotal() + " packed");
		if (reminders.isEmpty()) return;

		title = readiness.isReadyToLeave() ? "Slayer prep suggestion" : "Slayer prep incomplete";
		message = (readiness.isReadyToLeave() ? "Consider: " : "Missing: ") + String.join("  •  ", reminders);
		expiresAt = System.currentTimeMillis() + DISPLAY_MILLIS;
	}

	void hide()
	{
		expiresAt = 0L;
		title = null;
		message = null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long remaining = expiresAt - System.currentTimeMillis();
		if (remaining <= 0 || title == null || message == null)
		{
			return null;
		}

		float fade = remaining < FADE_MILLIS
			? Math.max(0f, remaining / (float) FADE_MILLIS)
			: 1f;

		graphics.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics bodyMetrics = graphics.getFontMetrics();
		graphics.setFont(FontManager.getRunescapeBoldFont());
		FontMetrics titleMetrics = graphics.getFontMetrics();
		int width = Math.max(
			MIN_WIDTH,
			Math.max(titleMetrics.stringWidth(title), bodyMetrics.stringWidth(message)) + 28);
		width = Math.min(MAX_WIDTH, width);

		graphics.setColor(withAlpha(BACKGROUND, (int) (222 * fade)));
		graphics.fillRoundRect(0, 0, width, HEIGHT, 8, 8);
		graphics.setColor(withAlpha(Color.BLACK, (int) (190 * fade)));
		graphics.drawRoundRect(1, 1, width - 3, HEIGHT - 3, 8, 8);
		graphics.setColor(withAlpha(GOLD, (int) (235 * fade)));
		graphics.drawRoundRect(0, 0, width - 1, HEIGHT - 1, 8, 8);
		graphics.fillRect(9, 7, 3, HEIGHT - 14);

		graphics.setFont(FontManager.getRunescapeBoldFont());
		graphics.setColor(withAlpha(Color.BLACK, (int) (230 * fade)));
		graphics.drawString(title, 19, 20);
		graphics.setColor(withAlpha(GOLD, (int) (255 * fade)));
		graphics.drawString(title, 18, 19);

		graphics.setFont(FontManager.getRunescapeSmallFont());
		graphics.setColor(withAlpha(Color.BLACK, (int) (230 * fade)));
		graphics.drawString(message, 19, 40);
		graphics.setColor(withAlpha(TEXT, (int) (255 * fade)));
		graphics.drawString(message, 18, 39);

		return new Dimension(width, HEIGHT);
	}

	static boolean shouldRemindSupply(SupplyRecommendation supply)
	{
		return supply != null
			&& supply.isEnabledForTrip()
			&& supply.getStatus().isBanked()
			&& (!supply.getStatus().isPacked() || !supply.hasRecommendedQuantityPacked())
			&& (supply.isRequired() || isPrepReminderSupply(supply));
	}

	private static boolean isPrepReminderSupply(SupplyRecommendation supply)
	{
		return "Goading".equals(supply.getCategory())
			|| "Prayer regen".equals(supply.getCategory());
	}

	private static Color withAlpha(Color color, int alpha)
	{
		return new Color(
			color.getRed(),
			color.getGreen(),
			color.getBlue(),
			Math.max(0, Math.min(255, alpha)));
	}
}
