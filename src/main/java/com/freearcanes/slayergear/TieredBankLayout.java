package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;

class TieredBankLayout
{
	private static final Logger log = LoggerFactory.getLogger(TieredBankLayout.class);
	private static final int ITEMS_PER_ROW = 8;
	private static final int ITEM_WIDTH = 36;
	private static final int ITEM_HEIGHT = 32;
	private static final int ITEM_X_PADDING = 12;
	private static final int ITEM_Y_PADDING = 4;
	private static final int ITEM_START_X = 51;
	private static final int HEADER_HEIGHT = 21;
	private static final int SECTION_PADDING = 8;
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

	private final Client client;
	private final ItemManager itemManager;
	private final Map<Widget, Integer> tiersByWidget = new IdentityHashMap<>();
	private final Map<Widget, String> headingsByWidget = new IdentityHashMap<>();
	private boolean resetScrollOnNextLayout;

	@Inject
	TieredBankLayout(Client client, ItemManager itemManager)
	{
		this.client = client;
		this.itemManager = itemManager;
	}

	void activate()
	{
		resetScrollOnNextLayout = true;
		clearMappings();
	}

	void clear()
	{
		resetScrollOnNextLayout = false;
		clearMappings();
	}

	void layout(GearRecommendations recommendations)
	{
		clearMappings();
		Widget itemContainer = client.getWidget(InterfaceID.Bankmain.ITEMS);
		if (itemContainer == null || itemContainer.getChildren() == null)
		{
			return;
		}

		Map<Integer, Widget> widgetsByItemId = new HashMap<>();
		for (Widget child : itemContainer.getChildren())
		{
			if (child == null || child.getItemId() < 0)
			{
				continue;
			}

			int itemId = child.getItemId();
			ItemComposition composition = itemManager.getItemComposition(itemId);
			boolean realItem = child.getItemQuantity() > 0
				&& composition != null
				&& composition.getPlaceholderTemplateId() == -1;
			if (realItem)
			{
				widgetsByItemId.putIfAbsent(itemManager.canonicalize(itemId), child);
			}
		}

		for (Widget child : itemContainer.getChildren())
		{
			if (child != null)
			{
				child.setHidden(true);
			}
		}

		Map<Integer, List<TierWidget>> byTier = new HashMap<>();
		Set<Widget> representedGear = new HashSet<>();
		for (Map.Entry<EquipmentInventorySlot, List<GearRecommendation>> entry : recommendations.getBySlot().entrySet())
		{
			for (GearRecommendation recommendation : entry.getValue())
			{
				Widget widget = widgetsByItemId.get(recommendation.getCanonicalItemId());
				if (widget == null || !representedGear.add(widget))
				{
					// The item may already be in inventory/equipment. Keep its rank in the
					// recommendation model, but never fake it back into the bank UI.
					continue;
				}
				byTier.computeIfAbsent(recommendation.getRank(), ignored -> new ArrayList<>())
					.add(new TierWidget(widget, recommendation));
			}
		}

		List<SupplyWidget> supplies = new ArrayList<>();
		Set<Integer> representedSupplies = new HashSet<>();
		for (SupplyRecommendation supply : recommendations.getSupplies())
		{
			// PACKED_BANKED is intentionally still visible. Consumables should not
			// vanish from the Best-in-Bank view after the first potion/food withdrawal.
			if (!supply.getStatus().isBanked()) continue;
			Widget widget = widgetsByItemId.get(supply.getCanonicalItemId());
			if (widget != null
				&& !representedGear.contains(widget)
				&& representedSupplies.add(supply.getCanonicalItemId()))
			{
				supplies.add(new SupplyWidget(widget, supply));
			}
		}
		supplies.sort(Comparator.comparingInt(value ->
			recommendations.getSupplies().indexOf(value.recommendation)));

		Comparator<TierWidget> slotOrder = Comparator
			.comparingInt(value -> slotIndex(value.recommendation.getSlot()));
		int cursorY = 0;
		int gearPlaced = 0;

		// Tier 1 and task supplies share the primary section. This keeps the things
		// the player should actually withdraw together at the top of the bank view.
		List<TierWidget> tierOne = byTier.getOrDefault(1, Collections.emptyList());
		tierOne.sort(slotOrder);
		int primaryCount = tierOne.size() + supplies.size();
		boolean cannonSetupIncluded = supplies.stream().anyMatch(value ->
			"Cannon setup".equals(value.recommendation.getCategory()) || "Cannon ammo".equals(value.recommendation.getCategory()));
		if (primaryCount > 0)
		{
			int itemStartY = cursorY + HEADER_HEIGHT;
			int index = 0;
			Widget firstWidget = null;
			for (TierWidget tierWidget : tierOne)
			{
				Widget widget = tierWidget.widget;
				if (firstWidget == null) firstWidget = widget;
				positionWidget(widget,
					ITEM_START_X + (index % ITEMS_PER_ROW) * (ITEM_WIDTH + ITEM_X_PADDING),
					itemStartY + (index / ITEMS_PER_ROW) * (ITEM_HEIGHT + ITEM_Y_PADDING));
				tiersByWidget.put(widget, 1);
				gearPlaced++;
				index++;
			}
			for (SupplyWidget supplyWidget : supplies)
			{
				Widget widget = supplyWidget.widget;
				if (firstWidget == null) firstWidget = widget;
				positionWidget(widget,
					ITEM_START_X + (index % ITEMS_PER_ROW) * (ITEM_WIDTH + ITEM_X_PADDING),
					itemStartY + (index / ITEMS_PER_ROW) * (ITEM_HEIGHT + ITEM_Y_PADDING));
				// Cannon parts/ammo are part of the actual Tier 1 trip setup, not
				// merely optional consumables. Give them the same Tier 1 bank treatment.
				String category = supplyWidget.recommendation.getCategory();
				if ("Cannon setup".equals(category) || "Cannon ammo".equals(category))
				{
					tiersByWidget.put(widget, 1);
				}
				index++;
			}
			if (firstWidget != null)
			{
				headingsByWidget.put(firstWidget, cannonSetupIncluded
					? "Tier 1 - Best equipment + cannon + task supplies"
					: "Tier 1 - Best equipment + task supplies");
			}
			int rows = (primaryCount + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW;
			cursorY = itemStartY
				+ rows * (ITEM_HEIGHT + ITEM_Y_PADDING)
				+ SECTION_PADDING;
		}

		for (int tier = 2; tier <= 3; tier++)
		{
			List<TierWidget> widgets = byTier.getOrDefault(tier, Collections.emptyList());
			if (widgets.isEmpty())
			{
				continue;
			}

			widgets.sort(slotOrder);
			int itemStartY = cursorY + HEADER_HEIGHT;
			for (int index = 0; index < widgets.size(); index++)
			{
				TierWidget tierWidget = widgets.get(index);
				Widget widget = tierWidget.widget;
				int x = ITEM_START_X
					+ (index % ITEMS_PER_ROW) * (ITEM_WIDTH + ITEM_X_PADDING);
				int y = itemStartY
					+ (index / ITEMS_PER_ROW) * (ITEM_HEIGHT + ITEM_Y_PADDING);
				positionWidget(widget, x, y);
				tiersByWidget.put(widget, tier);
				gearPlaced++;
			}

			headingsByWidget.put(widgets.get(0).widget, tierHeading(tier));
			int rows = (widgets.size() + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW;
			cursorY = itemStartY
				+ rows * (ITEM_HEIGHT + ITEM_Y_PADDING)
				+ SECTION_PADDING;
		}

		int scrollHeight = Math.max(cursorY, itemContainer.getHeight());
		itemContainer.setScrollHeight(scrollHeight);
		if (resetScrollOnNextLayout)
		{
			itemContainer.setScrollY(0);
			client.setVarcIntValue(VarClientID.BANK_SCROLLPOS, 0);
			resetScrollOnNextLayout = false;
		}
		else if (itemContainer.getScrollY() > scrollHeight - itemContainer.getHeight())
		{
			itemContainer.setScrollY(Math.max(0, scrollHeight - itemContainer.getHeight()));
		}
		itemContainer.revalidateScroll();
		log.debug(
			"Placed {} currently banked ranked gear items and {} owned supplies into Best-in-Bank layout",
			gearPlaced,
			supplies.size());
	}

	int tierFor(Widget widget)
	{
		return tiersByWidget.getOrDefault(widget, 0);
	}

	String headingFor(Widget widget)
	{
		return headingsByWidget.get(widget);
	}

	private void clearMappings()
	{
		tiersByWidget.clear();
		headingsByWidget.clear();
	}

	private static void positionWidget(Widget widget, int x, int y)
	{
		widget.setHidden(false);
		widget.setOriginalWidth(ITEM_WIDTH);
		widget.setOriginalHeight(ITEM_HEIGHT);
		widget.setOriginalX(x);
		widget.setOriginalY(y);
		widget.revalidate();
	}

	private static String tierHeading(int tier)
	{
		String suffix = tier == 1
			? "Best available equipment"
			: tier == 2 ? "Next-best equipment" : "Fallback equipment";
		return "Tier " + tier + " - " + suffix;
	}


	private static int slotIndex(EquipmentInventorySlot slot)
	{
		int index = SLOT_ORDER.indexOf(slot);
		return index < 0 ? Integer.MAX_VALUE : index;
	}

	private static final class TierWidget
	{
		private final Widget widget;
		private final GearRecommendation recommendation;

		private TierWidget(Widget widget, GearRecommendation recommendation)
		{
			this.widget = widget;
			this.recommendation = recommendation;
		}
	}

	private static final class SupplyWidget
	{
		private final Widget widget;
		private final SupplyRecommendation recommendation;

		private SupplyWidget(Widget widget, SupplyRecommendation recommendation)
		{
			this.widget = widget;
			this.recommendation = recommendation;
		}
	}
}