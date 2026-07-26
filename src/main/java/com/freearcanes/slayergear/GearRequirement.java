package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;

final class GearRequirement
{
	private final String label;
	private final List<Option> options;

	private GearRequirement(String label, List<Option> options)
	{
		this.label = label;
		this.options = Collections.unmodifiableList(new ArrayList<>(options));
	}

	static GearRequirement anyOf(String label, Option... options)
	{
		return new GearRequirement(label, Arrays.asList(options));
	}

	static Option option(EquipmentInventorySlot slot, String tokenExpression)
	{
		return new Option(slot, tokenExpression);
	}

	String getLabel() { return label; }
	List<Option> getOptions() { return options; }

	boolean isSatisfied(Map<EquipmentInventorySlot, GearRecommendation> selected)
	{
		for (Option option : options)
		{
			GearRecommendation recommendation = selected.get(option.slot);
			if (recommendation != null && option.matches(recommendation.getItemName()))
			{
				return true;
			}
		}
		return false;
	}

	boolean restricts(EquipmentInventorySlot slot)
	{
		int count = 0;
		for (Option option : options)
		{
			if (option.slot == slot)
			{
				count++;
			}
		}
		return count > 0 && options.stream().allMatch(option -> option.slot == slot);
	}

	boolean matchesForSlot(EquipmentInventorySlot slot, String itemName)
	{
		for (Option option : options)
		{
			if (option.slot == slot && option.matches(itemName))
			{
				return true;
			}
		}
		return false;
	}

	static final class Option
	{
		private final EquipmentInventorySlot slot;
		private final String tokenExpression;

		private Option(EquipmentInventorySlot slot, String tokenExpression)
		{
			this.slot = slot;
			this.tokenExpression = tokenExpression;
		}

		EquipmentInventorySlot getSlot() { return slot; }
		String getTokenExpression() { return tokenExpression; }
		boolean hasSameTokens(Option other)
		{
			return other != null
				&& NameMatcher.normalize(tokenExpression).equals(NameMatcher.normalize(other.tokenExpression));
		}
		boolean matches(String itemName) { return NameMatcher.matchesAnyToken(itemName, tokenExpression); }
	}
}