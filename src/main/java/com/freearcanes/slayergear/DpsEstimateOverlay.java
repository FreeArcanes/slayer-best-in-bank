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

/** Optional movable, translucent copy of the current Tier 1 DPS estimate. */
class DpsEstimateOverlay extends Overlay
{
	private static final int PADDING_X = 10;
	private static final int PADDING_Y = 7;
	private static final int LINE_HEIGHT = 15;
	private static final int MIN_WIDTH = 155;
	private static final int MAX_METHOD_LENGTH = 34;
	private static final Color GOLD = new Color(255, 184, 66);
	private static final Color TEAL = new Color(80, 210, 205);
	private static final Color TEXT = new Color(238, 232, 213);

	private final SlayerGearAdvisorPlugin plugin;
	private final SlayerGearAdvisorConfig config;

	@Inject
	DpsEstimateOverlay(SlayerGearAdvisorPlugin plugin, SlayerGearAdvisorConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.dpsOverlayEnabled()) return null;
		GearRecommendations recommendations = plugin.currentRecommendations();
		if (recommendations == null
			|| recommendations.getState() != GearRecommendations.State.READY
			|| recommendations.getLoadoutTiers().isEmpty()) return null;
		LoadoutOffenseEstimate estimate = recommendations.getLoadoutTiers()
			.get(0).getOffenseEstimate();
		if (!estimate.isAvailable()) return null;

		String method = estimate.getMethodName();
		if (method.isEmpty() && recommendations.getStrategy() != null)
			method = recommendations.getStrategy().getName();
		List<String> lines = displayLines(method, estimate, config.dpsOverlayDetailed());
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics metrics = graphics.getFontMetrics();
		int width = MIN_WIDTH;
		for (String line : lines)
			width = Math.max(width, metrics.stringWidth(line) + PADDING_X * 2);
		int height = PADDING_Y * 2 + lines.size() * LINE_HEIGHT;
		int opacity = Math.max(0, Math.min(255, config.dpsOverlayOpacity()));
		graphics.setColor(new Color(18, 20, 24, opacity));
		graphics.fillRoundRect(0, 0, width, height, 8, 8);
		graphics.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(),
			Math.max(90, opacity)));
		graphics.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);

		int baseline = PADDING_Y + metrics.getAscent();
		for (int index = 0; index < lines.size(); index++)
		{
			graphics.setColor(index == 0 ? GOLD : index == 2 ? TEAL : TEXT);
			graphics.drawString(lines.get(index), PADDING_X, baseline + index * LINE_HEIGHT);
		}
		return new Dimension(width, height);
	}

	static List<String> displayLines(
		String methodName, LoadoutOffenseEstimate estimate, boolean detailed)
	{
		List<String> lines = new ArrayList<>();
		lines.add("SBIB DPS ESTIMATE");
		String method = methodName == null ? "" : methodName.trim();
		if (!method.isEmpty()) lines.add(shorten(method, MAX_METHOD_LENGTH));
		lines.add(SlayerGearPanel.formatDps(estimate));
		if (detailed && estimate != null && estimate.hasKillRate())
			lines.add(overlayKillRate(estimate));
		if (detailed && estimate != null && !estimate.getTargetName().isEmpty())
			lines.add("Target: " + shorten(estimate.getTargetName(), MAX_METHOD_LENGTH));
		return lines;
	}

	private static String overlayKillRate(LoadoutOffenseEstimate estimate)
	{
		String ttk = estimate.getMaximumSecondsPerKill()
			> estimate.getMinimumSecondsPerKill() + 0.05
			? String.format(java.util.Locale.ENGLISH, "%.1f–%.1fs TTK",
				estimate.getMinimumSecondsPerKill(), estimate.getMaximumSecondsPerKill())
			: String.format(java.util.Locale.ENGLISH, "%.1fs TTK",
				estimate.getMinimumSecondsPerKill());
		String kills = estimate.getMaximumKillsPerHour()
			> estimate.getMinimumKillsPerHour() + 0.05
			? String.format(java.util.Locale.ENGLISH, "%.0f–%.0f kills/hr",
				estimate.getMinimumKillsPerHour(), estimate.getMaximumKillsPerHour())
			: String.format(java.util.Locale.ENGLISH, "%.0f kills/hr",
				estimate.getMinimumKillsPerHour());
		return ttk + " · " + kills;
	}

	private static String shorten(String value, int length)
	{
		return value.length() <= length ? value : value.substring(0, length - 1) + "…";
	}
}
