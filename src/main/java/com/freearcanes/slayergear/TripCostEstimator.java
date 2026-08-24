package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemStats;
import net.runelite.client.game.ItemManager;

/** GE-guide estimate for planned supplies and directly observable combat consumables. */
final class TripCostEstimator
{
	private TripCostEstimator() {}

	static TripCostEstimate estimate(List<SupplyRecommendation> supplies,
		ItemManager itemManager, int plannedKills)
	{
		return finish(supplyCosts(supplies, itemManager), plannedKills);
	}

	static TripCostEstimate estimate(GearRecommendations recommendations,
		ItemManager itemManager, int plannedKills)
	{
		List<CategoryCost> costs = supplyCosts(
			recommendations == null ? null : recommendations.getSupplies(), itemManager);
		addCombatConsumables(costs, recommendations, itemManager, plannedKills);
		return finish(costs, plannedKills);
	}

	private static List<CategoryCost> supplyCosts(List<SupplyRecommendation> supplies,
		ItemManager itemManager)
	{
		List<CategoryCost> costs = new ArrayList<>();
		if (supplies == null || itemManager == null) return costs;
		for (SupplyRecommendation supply : supplies)
		{
			if (!supply.isEnabledForTrip() || !supply.hasQuantityTarget()
				|| supply.getItemId() <= 0) continue;
			int price = Math.max(0, itemManager.getItemPrice(supply.getItemId()));
			if (price == 0) continue;
			long count = supply.getRecommendedQuantity();
			if ("doses".equals(supply.getQuantityUnit()))
			{
				int doses = Math.max(1, SmartSupplyAdvisor.doseScore(supply.getItemName()));
				count = (count + doses - 1) / doses;
			}
			long cost = (long) price * count;
			if (cost > 0) costs.add(new CategoryCost(supply.getCategory(), cost));
		}
		return costs;
	}

	private static TripCostEstimate finish(List<CategoryCost> costs, int plannedKills)
	{
		long total = 0;
		for (CategoryCost cost : costs) total += cost.cost;
		costs.sort(Comparator.comparingLong(CategoryCost::getCost).reversed());
		List<String> breakdown = new ArrayList<>();
		for (int index = 0; index < Math.min(3, costs.size()); index++)
		{
			CategoryCost cost = costs.get(index);
			breakdown.add(cost.category + " " + compactGp(cost.cost));
		}
		return new TripCostEstimate(total, plannedKills, breakdown);
	}

	private static void addCombatConsumables(List<CategoryCost> costs,
		GearRecommendations recommendations, ItemManager itemManager, int plannedKills)
	{
		if (recommendations == null || itemManager == null || plannedKills <= 0
			|| recommendations.getState() != GearRecommendations.State.READY
			|| recommendations.getLoadoutTiers().isEmpty()) return;
		LoadoutTier tier = recommendations.getLoadoutTiers().get(0);
		LoadoutOffenseEstimate offense = tier.getOffenseEstimate();
		GearRecommendation weapon = tier.get(EquipmentInventorySlot.WEAPON);
		if (weapon == null || offense == null || !offense.hasKillRate()) return;
		ItemStats weaponStats = itemManager.getItemStats(weapon.getItemId());
		if (weaponStats == null || weaponStats.getEquipment() == null
			|| weaponStats.getEquipment().getAspeed() <= 0) return;
		double secondsPerKill = (offense.getMinimumSecondsPerKill()
			+ offense.getMaximumSecondsPerKill()) / 2.0;
		long attacks = Math.max(1L, (long) Math.ceil(plannedKills * secondsPerKill
			/ (weaponStats.getEquipment().getAspeed() * 0.6)));
		if (recommendations.getStrategy() != null
			&& recommendations.getStrategy().getCombatStyle() == CombatStyle.RANGED)
		{
			addRangedAmmoCost(costs, tier, itemManager, attacks);
		}
		else if (recommendations.getStrategy() != null
			&& recommendations.getStrategy().getCombatStyle() == CombatStyle.MAGIC)
		{
			addMagicCost(costs, tier, itemManager, attacks, offense.getMethodName());
		}
	}

	private static void addRangedAmmoCost(List<CategoryCost> costs, LoadoutTier tier,
		ItemManager itemManager, long attacks)
	{
		GearRecommendation weapon = tier.get(EquipmentInventorySlot.WEAPON);
		GearRecommendation ammo = tier.get(EquipmentInventorySlot.AMMO);
		if (weapon == null || ammo == null
			|| !RangedAmmoPolicy.usesAmmoSlot(weapon.getItemName())
			|| !RangedAmmoPolicy.isCompatible(weapon.getItemName(), ammo.getItemName())) return;
		double recovery = recoverableAmmo(ammo.getItemName())
			? ammoRecoveryRate(tier.get(EquipmentInventorySlot.CAPE)) : 0;
		long consumed = Math.max(1L, (long) Math.ceil(attacks * (1.0 - recovery)));
		long cost = consumed * Math.max(0, itemManager.getItemPrice(ammo.getItemId()));
		if (cost > 0) costs.add(new CategoryCost("Worn ammunition", cost));
	}

	private static double ammoRecoveryRate(GearRecommendation cape)
	{
		if (cape == null) return 0;
		String name = NameMatcher.normalize(cape.getItemName());
		if (name.contains("assembler")) return 0.80;
		if (name.contains("accumulator") || name.contains("ranging cape")
			|| (name.contains("max cape") && !name.contains("dizana"))) return 0.72;
		if (name.contains("attractor")) return 0.60;
		// A quiver's Ava upgrade is account-wide and has no visible item variant,
		// so assuming it here could materially understate cost.
		return 0;
	}

	private static boolean recoverableAmmo(String ammoName)
	{
		String name = NameMatcher.normalize(ammoName);
		return !name.contains("bolt rack") && !name.endsWith(" tar")
			&& !name.contains("chinchompa") && !name.contains("holy water");
	}

	private static void addMagicCost(List<CategoryCost> costs, LoadoutTier tier,
		ItemManager itemManager, long attacks, String methodName)
	{
		if (methodName == null || methodName.isEmpty()) return;
		GearRecommendation weapon = tier.get(EquipmentInventorySlot.WEAPON);
		String weaponName = weapon == null ? "" : NameMatcher.normalize(weapon.getItemName());
		RuneRecipe recipe = RuneRecipe.forMethod(methodName);
		if (recipe == null) return;
		long perCast = recipe.coinCost;
		for (RuneUse rune : recipe.runes)
		{
			if (suppliesRune(weaponName, rune.itemId)) continue;
			perCast += (long) rune.quantity * Math.max(0, itemManager.getItemPrice(rune.itemId));
		}
		long cost = attacks * perCast;
		if (cost > 0) costs.add(new CategoryCost("Combat casts", cost));
	}

	private static boolean suppliesRune(String weapon, int runeId)
	{
		if (runeId == ItemID.AIRRUNE)
			return has(weapon, "staff of air", "air battlestaff", "mystic air staff",
				"mist battlestaff", "mist staff", "dust battlestaff", "dust staff",
				"smoke battlestaff", "smoke staff");
		if (runeId == ItemID.WATERRUNE)
			return has(weapon, "staff of water", "water battlestaff", "mystic water staff",
				"mist battlestaff", "mist staff", "mud battlestaff", "mud staff",
				"steam battlestaff", "steam staff", "kodai wand");
		if (runeId == ItemID.EARTHRUNE)
			return has(weapon, "staff of earth", "earth battlestaff", "mystic earth staff",
				"dust battlestaff", "dust staff", "mud battlestaff", "mud staff",
				"lava battlestaff", "lava staff");
		if (runeId == ItemID.FIRERUNE)
			return has(weapon, "staff of fire", "fire battlestaff", "mystic fire staff",
				"smoke battlestaff", "smoke staff", "steam battlestaff", "steam staff",
				"lava battlestaff", "lava staff");
		return false;
	}

	private static boolean has(String value, String... tokens)
	{
		for (String token : tokens) if (value.contains(token)) return true;
		return false;
	}

	static String compactGp(long value)
	{
		if (value >= 1_000_000) return String.format(Locale.ENGLISH, "%.1fm", value / 1_000_000.0);
		if (value >= 1_000) return String.format(Locale.ENGLISH, "%.1fk", value / 1_000.0);
		return value + " gp";
	}

	private static final class CategoryCost
	{
		private final String category;
		private final long cost;
		private CategoryCost(String category, long cost) { this.category = category; this.cost = cost; }
		private long getCost() { return cost; }
	}

	private static final class RuneUse
	{
		private final int itemId;
		private final int quantity;
		private RuneUse(int itemId, int quantity) { this.itemId = itemId; this.quantity = quantity; }
	}

	private static final class RuneRecipe
	{
		private final List<RuneUse> runes = new ArrayList<>();
		private long coinCost;

		private RuneRecipe rune(int itemId, int quantity)
		{
			runes.add(new RuneUse(itemId, quantity));
			return this;
		}

		private RuneRecipe coins(long coins) { coinCost = coins; return this; }

		private static RuneRecipe forMethod(String methodName)
		{
			String method = NameMatcher.normalize(methodName);
			if (method.contains("warped sceptre"))
				return new RuneRecipe().rune(ItemID.CHAOSRUNE, 2).rune(ItemID.EARTHRUNE, 5);
			if (method.contains("trident of the swamp"))
				return new RuneRecipe().rune(ItemID.DEATHRUNE, 1).rune(ItemID.CHAOSRUNE, 1)
					.rune(ItemID.FIRERUNE, 5).rune(ItemID.SNAKEBOSS_SCALE, 1);
			if (method.contains("trident of the seas"))
				return new RuneRecipe().rune(ItemID.DEATHRUNE, 1).rune(ItemID.CHAOSRUNE, 1)
					.rune(ItemID.FIRERUNE, 5).coins(10);
			if (method.contains("sanguinesti staff"))
				return new RuneRecipe().rune(ItemID.BLOODRUNE, 3);
			if (method.contains("tumeken") && method.contains("shadow"))
				return new RuneRecipe().rune(ItemID.SOULRUNE, 2).rune(ItemID.CHAOSRUNE, 5);
			if (method.contains("ice barrage"))
				return new RuneRecipe().rune(ItemID.WATERRUNE, 6).rune(ItemID.DEATHRUNE, 4)
					.rune(ItemID.BLOODRUNE, 2);
			if (method.contains("ice burst"))
				return new RuneRecipe().rune(ItemID.WATERRUNE, 4).rune(ItemID.CHAOSRUNE, 2)
					.rune(ItemID.DEATHRUNE, 4);
			return elemental(method);
		}

		private static RuneRecipe elemental(String method)
		{
			int elementRune = method.contains("water") ? ItemID.WATERRUNE
				: method.contains("earth") ? ItemID.EARTHRUNE
				: method.contains("fire") ? ItemID.FIRERUNE : 0;
			int elementQuantity;
			RuneRecipe recipe = new RuneRecipe();
			if (method.contains("surge"))
			{
				recipe.rune(ItemID.AIRRUNE, 7).rune(ItemID.WRATHRUNE, 1);
				elementQuantity = 10;
			}
			else if (method.contains("wave"))
			{
				recipe.rune(ItemID.AIRRUNE, 5).rune(ItemID.BLOODRUNE, 1);
				elementQuantity = 7;
			}
			else if (method.contains("blast"))
			{
				recipe.rune(ItemID.AIRRUNE, 3).rune(ItemID.DEATHRUNE, 1);
				elementQuantity = method.contains("water") ? 3 : method.contains("earth") ? 4 : 5;
			}
			else if (method.contains("bolt"))
			{
				recipe.rune(ItemID.AIRRUNE, 2).rune(ItemID.CHAOSRUNE, 1);
				elementQuantity = method.contains("water") ? 2 : method.contains("earth") ? 3 : 4;
			}
			else if (method.contains("strike"))
			{
				recipe.rune(ItemID.AIRRUNE, 1).rune(ItemID.MINDRUNE, 1);
				elementQuantity = method.contains("water") ? 1 : method.contains("earth") ? 2 : 3;
			}
			else return null;
			if (elementRune != 0) recipe.rune(elementRune, elementQuantity);
			return recipe;
		}
	}
}
