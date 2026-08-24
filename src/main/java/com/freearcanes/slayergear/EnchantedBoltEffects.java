package com.freearcanes.slayergear;

/** Expected sustained damage from unambiguous PvM enchanted-bolt procs. */
final class EnchantedBoltEffects
{
	private EnchantedBoltEffects() {}

	static String effectName(String ammoName)
	{
		String ammo = NameMatcher.normalize(ammoName);
		if (!ammo.contains("(e)")) return "";
		if (ammo.contains("ruby")) return "Blood Forfeit EV";
		if (ammo.contains("diamond")) return "Armour Piercing EV";
		if (ammo.contains("opal")) return "Lucky Lightning EV";
		if (ammo.contains("dragonstone")) return "Dragon's Breath EV";
		if (ammo.contains("onyx")) return "Life Leech EV";
		if (ammo.contains("pearl")) return "Sea Curse";
		if (ammo.contains("emerald")) return "Poison utility";
		if (ammo.contains("jade")) return "Knockdown utility";
		if (ammo.contains("sapphire")) return "Prayer utility";
		if (ammo.contains("topaz")) return "PvP-only effect";
		return "";
	}

	static Result apply(String ammoName, String weaponName, double normalDamage,
		double accuracy, int maxHit, TargetDefence target, CombatLevelContext levels)
	{
		String ammo = NameMatcher.normalize(ammoName);
		if (!ammo.contains("(e)")) return Result.none(normalDamage);
		String weapon = NameMatcher.normalize(weaponName);
		boolean zaryte = weapon.contains("zaryte crossbow");
		double diary = levels.hasKandarinHardDiary() ? 1.10 : 1.0;
		double successfulAverage = maxHit / 2.0 + (maxHit > 0 ? 1.0 / (maxHit + 1.0) : 0);

		if (ammo.contains("ruby"))
		{
			double chance = 0.06 * diary;
			if (target.getHitpoints() <= 0) return Result.named(normalDamage, "Ruby proc unavailable (HP unknown)");
			double proc = sustainedRubyProc(target.getHitpoints(), zaryte);
			return Result.named((1 - chance) * normalDamage + chance * proc,
				"Ruby Blood Forfeit sustained EV");
		}
		if (ammo.contains("diamond"))
		{
			double chance = 0.10 * diary;
			int procMax = (int) Math.floor(maxHit * (zaryte ? 1.26 : 1.15));
			double procAverage = procMax / 2.0 + (procMax > 0 ? 1.0 / (procMax + 1.0) : 0);
			return Result.named((1 - chance) * normalDamage + chance * procAverage,
				"Diamond Armour Piercing EV");
		}
		if (ammo.contains("opal"))
		{
			double chance = 0.05 * diary;
			double extra = Math.floor(levels.getBoostedRanged() * 0.10) * (zaryte ? 1.10 : 1.0);
			return Result.named((1 - chance) * normalDamage
				+ chance * (successfulAverage + extra), "Opal Lucky Lightning EV");
		}
		if (ammo.contains("dragonstone") && !target.hasAttribute("dragon")
			&& !target.hasAttribute("fiery"))
		{
			double chance = 0.06 * diary;
			double extra = Math.floor(levels.getBoostedRanged() * 0.20) * (zaryte ? 1.10 : 1.0);
			return Result.named(normalDamage + accuracy * chance * extra, "Dragonstone Dragon's Breath EV");
		}
		if (ammo.contains("onyx") && !target.hasAttribute("undead"))
		{
			double chance = 0.11 * diary;
			double bonus = zaryte ? 0.32 : 0.20;
			return Result.named(normalDamage * (1.0 + chance * bonus), "Onyx Life Leech EV");
		}
		if (ammo.contains("pearl")) return Result.named(normalDamage,
			target.hasAttribute("fiery") ? "Pearl Sea Curse (fiery target)" : "Pearl Sea Curse");
		if (ammo.contains("emerald")) return Result.named(normalDamage, "Emerald poison utility");
		if (ammo.contains("jade")) return Result.named(normalDamage, "Jade knockdown utility");
		if (ammo.contains("sapphire")) return Result.named(normalDamage, "Sapphire prayer utility");
		if (ammo.contains("topaz")) return Result.named(normalDamage, "Topaz PvP-only effect");
		return Result.none(normalDamage);
	}

	/** Average Blood Forfeit damage while the target falls from full HP to zero. */
	static double sustainedRubyProc(int hitpoints, boolean zaryte)
	{
		if (hitpoints <= 0) return 0;
		double percent = zaryte ? 0.22 : 0.20;
		double cap = zaryte ? 110.0 : 100.0;
		double capThreshold = cap / percent;
		if (hitpoints <= capThreshold) return percent * hitpoints / 2.0;
		return (percent * capThreshold * capThreshold / 2.0
			+ cap * (hitpoints - capThreshold)) / hitpoints;
	}

	static final class Result
	{
		private final double damage;
		private final String name;
		private Result(double damage, String name) { this.damage = damage; this.name = name; }
		static Result none(double damage) { return new Result(damage, ""); }
		static Result named(double damage, String name) { return new Result(damage, name); }
		double getDamage() { return damage; }
		String getName() { return name; }
	}
}
