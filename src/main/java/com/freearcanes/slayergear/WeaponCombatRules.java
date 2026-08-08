package com.freearcanes.slayergear;

import java.util.Set;

/**
 * Weapon-family, attack-style, and monster-affinity rules which RuneLite's raw
 * item stat sheet cannot express on its own.
 */
final class WeaponCombatRules
{
	private WeaponCombatRules() { }

	static boolean supportsAttackType(String itemName, AttackType attackType)
	{
		if (attackType == null || attackType == AttackType.BALANCED) return true;
		String n = NameMatcher.normalize(itemName);
		if (n.isEmpty()) return true;

		if (n.contains("halberd")) return attackType == AttackType.STAB || attackType == AttackType.SLASH;
		if (has(n, "arkan blade", "blade of saeldor", "sulphur blades"))
			return attackType == AttackType.STAB || attackType == AttackType.SLASH;
		if (n.contains("colossal blade")) return attackType == AttackType.SLASH || attackType == AttackType.CRUSH;
		if (has(n, "silverlight", "darklight", "arclight", "emberlight")) return attackType == AttackType.STAB || attackType == AttackType.SLASH;
		if (has(n, "whip", "tentacle")) return attackType == AttackType.SLASH;
		if (n.contains("rapier")) return attackType == AttackType.STAB;
		if (has(n, "scimitar", "dagger", "claw"))
			return attackType == AttackType.STAB || attackType == AttackType.SLASH;
		if (n.contains("fang")) return attackType == AttackType.STAB || attackType == AttackType.SLASH;
		if (n.contains("partisan")) return attackType == AttackType.STAB || attackType == AttackType.CRUSH;
		if (has(n, "spear", "hasta")) return true;
		if (n.contains("scythe")) return attackType == AttackType.SLASH || attackType == AttackType.CRUSH;
		if (has(n, "godsword", "2h sword", "two-handed sword"))
			return attackType == AttackType.SLASH || attackType == AttackType.CRUSH;
		if (has(n, "battleaxe", "battle axe", "soulreaper axe", "zombie axe"))
			return attackType == AttackType.SLASH || attackType == AttackType.CRUSH;
		if (has(n, "warhammer", "granite hammer", "barronite mace", "maul", "bludgeon", "cudgel", "club", "anchor", "gadderhammer"))
			return attackType == AttackType.CRUSH;
		if (n.contains("mace")) return attackType == AttackType.STAB || attackType == AttackType.CRUSH;
		if (n.contains("pickaxe")) return attackType == AttackType.STAB || attackType == AttackType.CRUSH;
		if (has(n, "macuahuitl", "temotli")) return attackType == AttackType.CRUSH;
		if (n.contains("hallowed flail")) return attackType == AttackType.CRUSH || attackType == AttackType.SLASH;
		if (n.contains("flail")) return attackType == AttackType.CRUSH;
		if (n.contains("sickle")) return attackType == AttackType.SLASH;
		if (has(n, "staff", "wand", "sceptre", "trident", "bulwark")) return attackType == AttackType.CRUSH;

		if (n.endsWith(" sword") || n.contains(" longsword") || n.contains(" shortsword"))
			return attackType == AttackType.STAB || attackType == AttackType.SLASH;

		return true;
	}

	/** Multiplier applied to the effective attack-roll proxy while this weapon is used. */
	static double accuracyMultiplier(GearStrategy strategy, String itemName)
	{
		String n = NameMatcher.normalize(itemName);
		Set<TargetTrait> traits = effectiveTraits(strategy);
		double result = 1.0;

		// Summer Sweep-Up gave Granite hammer a 30% Golembane bonus to both
		// accuracy and damage against golem-type creatures, including Gargoyles.
		if (traits.contains(TargetTrait.GOLEM) && n.contains("granite hammer")) result = Math.max(result, 1.30);

		if (traits.contains(TargetTrait.DEMON))
		{
			if (has(n, "emberlight", "arclight")) result = Math.max(result, isDuke(strategy) ? 1.49 : 1.70);
			// Silverlight/Darklight are +60% damage versus demons; current Wiki
			// mechanics do not give their normal attacks a matching accuracy multiplier.
			else if (has(n, "darklight", "silverlight")) result = Math.max(result, 1.00);
			else if (has(n, "burning claws", "bone claws")) result = Math.max(result, 1.05);
			else if (strategy.getCombatStyle() == CombatStyle.RANGED && n.contains("scorching bow")) result = Math.max(result, 1.30);
			else if (strategy.getCombatStyle() == CombatStyle.RANGED && n.contains("holy water")) result = Math.max(result, 1.00);
			else if (strategy.getCombatStyle() == CombatStyle.MAGIC && n.contains("purging staff")
				&& NameMatcher.normalize(strategy.getName()).contains("demonbane")) result = Math.max(result, 1.80);
		}

		if (traits.contains(TargetTrait.WILDERNESS) && isWildernessWeapon(n))
		{
			result = Math.max(result, 1.50);
		}

		if (traits.contains(TargetTrait.DRAGON))
		{
			if (strategy.getCombatStyle() == CombatStyle.MELEE && n.contains("dragon hunter lance")) result = Math.max(result, 1.20);
			else if (strategy.getCombatStyle() == CombatStyle.RANGED && n.contains("dragon hunter crossbow")) result = Math.max(result, 1.30);
			else if (strategy.getCombatStyle() == CombatStyle.MAGIC && n.contains("dragon hunter wand")) result = Math.max(result, 1.75);
		}

		if (traits.contains(TargetTrait.KALPHITE) && n.contains("keris partisan of breaching")) result = Math.max(result, 1.33);

		if (traits.contains(TargetTrait.VAMPYRE))
		{
			if (n.contains("sunspear")) result = Math.max(result, 1.25);
			else if (n.contains("hallowed flail")) result = Math.max(result, 1.25);
			else if (has(n, "blisterwood flail", "blisterwood sickle")) result = Math.max(result, 1.05);
		}

		return result;
	}

	/** Multiplier applied to the effective damage/max-hit proxy while this weapon is used. */
	static double damageMultiplier(GearStrategy strategy, String itemName)
	{
		String n = NameMatcher.normalize(itemName);
		Set<TargetTrait> traits = effectiveTraits(strategy);
		double result = 1.0;

		if (traits.contains(TargetTrait.GOLEM))
		{
			if (n.contains("granite hammer")) result = Math.max(result, 1.30);
			else if (n.contains("barronite mace")) result = Math.max(result, 1.15);
		}

		if (traits.contains(TargetTrait.DEMON))
		{
			if (has(n, "emberlight", "arclight")) result = Math.max(result, isDuke(strategy) ? 1.49 : 1.70);
			else if (has(n, "darklight", "silverlight")) result = Math.max(result, 1.60);
			else if (has(n, "burning claws", "bone claws")) result = Math.max(result, 1.05);
			else if (strategy.getCombatStyle() == CombatStyle.RANGED && n.contains("scorching bow")) result = Math.max(result, 1.30);
			else if (strategy.getCombatStyle() == CombatStyle.RANGED && n.contains("holy water")) result = Math.max(result, 1.60);
			else if (strategy.getCombatStyle() == CombatStyle.MAGIC && n.contains("purging staff")
				&& NameMatcher.normalize(strategy.getName()).contains("demonbane")) result = Math.max(result, 1.50);
		}

		if (traits.contains(TargetTrait.WILDERNESS) && isWildernessWeapon(n))
		{
			result = Math.max(result, 1.50);
		}

		if (traits.contains(TargetTrait.DRAGON))
		{
			if (strategy.getCombatStyle() == CombatStyle.MELEE && n.contains("dragon hunter lance")) result = Math.max(result, 1.20);
			else if (strategy.getCombatStyle() == CombatStyle.RANGED && n.contains("dragon hunter crossbow")) result = Math.max(result, 1.25);
			else if (strategy.getCombatStyle() == CombatStyle.MAGIC && n.contains("dragon hunter wand")) result = Math.max(result, 1.40);
		}

		if (traits.contains(TargetTrait.KALPHITE) && (n.equals("keris") || n.startsWith("keris ") || n.contains("keris partisan")))
		{
			// Keris-family weapons get their normal target damage multiplier plus
			// a 1/51 chance to deal triple damage. Use the expected-value uplift
			// for ranking rather than pretending every hit procs.
			double base = n.contains("partisan of amascut") ? 1.15 : 1.33;
			result = Math.max(result, base * (53.0 / 51.0));
		}

		if (traits.contains(TargetTrait.LEAFY) && n.contains("leaf-bladed battleaxe")) result = Math.max(result, 1.175);

		if (traits.contains(TargetTrait.SHADE) && n.contains("gadderhammer"))
		{
			// 25% always-on damage plus a 5% chance to double damage. The score uses
			// the expected-value uplift rather than pretending the proc is guaranteed.
			result = Math.max(result, 1.2875);
		}

		if (traits.contains(TargetTrait.VAMPYRE))
		{
			if (n.contains("sunspear")) result = Math.max(result, 1.50);
			else if (n.contains("hallowed flail")) result = Math.max(result, 1.25);
			else if (has(n, "blisterwood flail", "blisterwood sickle")) result = Math.max(result, 1.25);
			else if (has(n, "ivandis flail", "rod of ivandis")) result = Math.max(result, 1.20);
		}

		return result;
	}

	/**
	 * Bounded score for effects that add flat max-hit rather than multiplying the
	 * normal attack. Rat-bone weapons add +10 max hit against rats.
	 */
	static double flatDamageScore(GearStrategy strategy, String itemName)
	{
		String n = NameMatcher.normalize(itemName);
		Set<TargetTrait> traits = effectiveTraits(strategy);
		if (traits.contains(TargetTrait.ARAXXOR) && n.contains("noxious halberd"))
		{
			// Encounter utility: guaranteed max hits on hatched araxytes and the
			// halberd's reach avoids mirrorback recoil when used from one tile away.
			return 175.0;
		}
		if (traits.contains(TargetTrait.RAT)
			&& ((strategy.getCombatStyle() == CombatStyle.MELEE && n.contains("bone mace"))
			|| (strategy.getCombatStyle() == CombatStyle.RANGED && n.contains("bone shortbow"))
			|| (strategy.getCombatStyle() == CombatStyle.MAGIC && n.contains("bone staff"))))
		{
			return 250.0;
		}
		return 0;
	}


	/**
	 * Some target-family weapons are mechanically unusable outside their target
	 * family. Reject them before raw stats can accidentally make them look good.
	 */
	static boolean usableOnTarget(GearStrategy strategy, String itemName)
	{
		String n = NameMatcher.normalize(itemName);
		Set<TargetTrait> traits = effectiveTraits(strategy);

		if (n.contains("holy water")) return traits.contains(TargetTrait.DEMON);

		if (n.contains("bone mace") || n.contains("bone shortbow") || n.contains("bone staff"))
		{
			return traits.contains(TargetTrait.RAT);
		}

		return true;
	}



	/**
	 * Returns true when the item has a real monster-family passive for the
	 * current task. This is also used as an escape hatch from a generic melee
	 * attack-type preference: e.g. Waterfiends normally favour Crush, but
	 * Emberlight's Demonbane passive can still make it a legitimate candidate.
	 */
	static boolean hasTargetSpecificEffect(GearStrategy strategy, String itemName)
	{
		return accuracyMultiplier(strategy, itemName) > 1.0
			|| damageMultiplier(strategy, itemName) > 1.0
			|| flatDamageScore(strategy, itemName) > 0;
	}

	static String affinityReason(GearStrategy strategy, String itemName)
	{
		double accuracy = accuracyMultiplier(strategy, itemName);
		double damage = damageMultiplier(strategy, itemName);
		double flat = flatDamageScore(strategy, itemName);
		if (accuracy <= 1.0 && damage <= 1.0 && flat <= 0) return null;
		String n = NameMatcher.normalize(itemName);
		Set<TargetTrait> traits = effectiveTraits(strategy);
		if (traits.contains(TargetTrait.GOLEM) && has(n, "granite hammer", "barronite mace")) return "Golembane target effect";
		if (traits.contains(TargetTrait.ARAXXOR) && n.contains("noxious halberd")) return "Araxxor minion and reach utility";
		if (traits.contains(TargetTrait.DEMON)) return "Demonbane target effect";
		if (traits.contains(TargetTrait.DRAGON)) return "Dragonbane target effect";
		if (traits.contains(TargetTrait.WILDERNESS) && isWildernessWeapon(n)) return "Wilderness weapon +50% accuracy/damage";
		if (traits.contains(TargetTrait.KALPHITE)) return "Kalphite/Scabarite target effect";
		if (traits.contains(TargetTrait.RAT)) return "Ratbane +10 max-hit effect";
		if (traits.contains(TargetTrait.SHADE)) return "Shade-specific damage effect";
		if (traits.contains(TargetTrait.LEAFY)) return "Leafbane target effect";
		if (traits.contains(TargetTrait.VAMPYRE)) return "Vampyre-specific weapon effect";
		return "Target-specific weapon effect";
	}

	static boolean isFieryPearlAmmo(GearStrategy strategy, String itemName)
	{
		String n = NameMatcher.normalize(itemName);
		return effectiveTraits(strategy).contains(TargetTrait.FIERY)
			&& (n.contains("pearl bolts (e)") || n.contains("pearl dragon bolts (e)"));
	}

	private static Set<TargetTrait> effectiveTraits(GearStrategy strategy)
	{
		if (strategy != null && !strategy.getTargetTraits().isEmpty()) return strategy.getTargetTraits();
		java.util.EnumSet<TargetTrait> fallback = java.util.EnumSet.noneOf(TargetTrait.class);
		if (strategy == null) return fallback;
		switch (strategy.getWeaponRule())
		{
			case DEMONBANE: fallback.add(TargetTrait.DEMON); break;
			case DRAGONBANE: fallback.add(TargetTrait.DRAGON); break;
			case GOLEMBANE: fallback.add(TargetTrait.GOLEM); break;
			case KALPHITE: fallback.add(TargetTrait.KALPHITE); break;
			case RATBANE: fallback.add(TargetTrait.RAT); break;
			case SHADE: fallback.add(TargetTrait.SHADE); break;
			case LEAF_BLADED: fallback.add(TargetTrait.LEAFY); break;
			case VAMPYRE: fallback.add(TargetTrait.VAMPYRE); break;
			default: break;
		}
		return fallback;
	}


	private static boolean isWildernessWeapon(String n)
	{
		if (n.contains("uncharged") || n.endsWith("(u)")) return false;
		return has(n, "craw's bow", "webweaver bow", "viggora's chainmace", "ursine chainmace",
			"thammaron's sceptre", "accursed sceptre");
	}

	private static boolean isDuke(GearStrategy strategy)
	{
		return strategy != null && (NameMatcher.normalize(strategy.getName()).contains("duke sucellus")
			|| NameMatcher.normalize(strategy.getLocation()).contains("duke sucellus"));
	}

	private static boolean has(String value, String... tokens)
	{
		for (String token : tokens) if (value.contains(token)) return true;
		return false;
	}
}
