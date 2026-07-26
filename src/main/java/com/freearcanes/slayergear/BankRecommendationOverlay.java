package com.freearcanes.slayergear;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

class BankRecommendationOverlay extends WidgetItemOverlay
{
	private final SlayerGearAdvisorPlugin plugin;
	private final SlayerGearAdvisorConfig config;
	private final ItemManager itemManager;
	private final TieredBankLayout tieredBankLayout;

	@Inject
	BankRecommendationOverlay(
		SlayerGearAdvisorPlugin plugin,
		SlayerGearAdvisorConfig config,
		ItemManager itemManager,
		TieredBankLayout tieredBankLayout)
	{
		this.plugin = plugin;
		this.config = config;
		this.itemManager = itemManager;
		this.tieredBankLayout = tieredBankLayout;
		showOnBank();
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (itemId < 0 || itemManager.getItemComposition(itemId).getPlaceholderTemplateId() != -1)
		{
			return;
		}

		Rectangle bounds = widgetItem.getCanvasBounds();
		if (plugin.isBankFilterActive())
		{
			int tier = tieredBankLayout.tierFor(widgetItem.getWidget());
			String heading = tieredBankLayout.headingFor(widgetItem.getWidget());
			if (heading != null)
			{
				Color color = tier > 0 ? colorFor(tier) : new Color(255, 152, 31);
				renderHeading(graphics, bounds, heading, color, tier == 1 ? 4 : 8);
			}
			if (tier > 0)
			{
				renderTierHighlight(graphics, bounds, tier, colorFor(tier), false);
			}
			SupplyRecommendation supply = tieredBankLayout.supplyFor(widgetItem.getWidget());
			if (supply != null && supply.getWithdrawalsStillNeeded() > 0)
			{
				renderWithdrawalBadge(graphics, bounds, supply.getWithdrawalsStillNeeded());
			}
			return;
		}

		if (!plugin.isHighlightsActive())
		{
			return;
		}

		GearRecommendations recommendations = plugin.getRecommendations();
		if (recommendations.getState() != GearRecommendations.State.READY)
		{
			return;
		}

		int canonicalItemId = itemManager.canonicalize(itemId);
		GearRecommendation recommendation = recommendations.find(canonicalItemId);
		if (recommendation == null)
		{
			return;
		}

		renderTierHighlight(
			graphics,
			bounds,
			recommendation.getRank(),
			colorFor(recommendation.getRank()),
			true);
	}

	private static void renderTierHighlight(
		Graphics2D graphics,
		Rectangle bounds,
		int tier,
		Color color,
		boolean showBadge)
	{
		if (bounds == null || bounds.width <= 2 || bounds.height <= 2)
		{
			return;
		}

		Stroke oldStroke = graphics.getStroke();
		graphics.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);

		Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), 34);
		Color border = new Color(color.getRed(), color.getGreen(), color.getBlue(), 215);
		graphics.setColor(fill);
		graphics.fillRoundRect(
			bounds.x + 1,
			bounds.y + 1,
			bounds.width - 2,
			bounds.height - 2,
			6,
			6);

		graphics.setStroke(new BasicStroke(tier == 1 ? 2f : 1.35f));
		graphics.setColor(border);
		graphics.drawRoundRect(
			bounds.x + 1,
			bounds.y + 1,
			bounds.width - 3,
			bounds.height - 3,
			6,
			6);

		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 235));
		graphics.fillRect(
			bounds.x + 3,
			bounds.y + bounds.height - 4,
			Math.max(1, bounds.width - 6),
			2);

		if (showBadge)
		{
			graphics.setFont(FontManager.getRunescapeSmallFont());
			String text = Integer.toString(tier);
			int x = bounds.x + 4;
			int y = bounds.y + 11;
			graphics.setColor(Color.BLACK);
			graphics.drawString(text, x + 1, y + 1);
			graphics.setColor(color);
			graphics.drawString(text, x, y);
		}

		graphics.setStroke(oldStroke);
	}

	private Color colorFor(int rank)
	{
		switch (rank)
		{
			case 1:
				return config.bestColor();
			case 2:
				return config.alternativeColor();
			default:
				return config.tierThreeColor();
		}
	}

	private static void renderWithdrawalBadge(Graphics2D graphics, Rectangle bounds, int withdrawals)
	{
		if (bounds == null || withdrawals <= 0) return;
		String text = "×" + withdrawals;
		graphics.setFont(FontManager.getRunescapeBoldFont());
		int width = graphics.getFontMetrics().stringWidth(text) + 6;
		int height = 14;
		int x = bounds.x + bounds.width - width;
		int y = bounds.y + 1;
		graphics.setColor(new Color(20, 17, 12, 225));
		graphics.fillRoundRect(x, y, width, height, 5, 5);
		graphics.setColor(new Color(255, 193, 7));
		graphics.drawRoundRect(x, y, width - 1, height - 1, 5, 5);
		graphics.drawString(text, x + 3, y + 11);
	}

	private static void renderHeading(
		Graphics2D graphics,
		Rectangle firstItemBounds,
		String text,
		Color color,
		int columns)
	{
		graphics.setFont(FontManager.getRunescapeSmallFont());
		graphics.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int x = firstItemBounds.x;
		int baseline = firstItemBounds.y - 6;
		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, baseline + 1);
		graphics.setColor(color);
		graphics.drawString(text, x, baseline);
		graphics.drawLine(
			x,
			firstItemBounds.y - 3,
			x + Math.max(1, columns) * (36 + 12) - 12,
			firstItemBounds.y - 3);
	}
}
