package com.freearcanes.slayergear;

/** Central compatibility policy for ranged weapons, ammunition, and visible charge states. */
final class RangedAmmoPolicy
{
	private RangedAmmoPolicy() {}

	static boolean isUsableWeapon(String weaponName)
	{
		String weapon = NameMatcher.normalize(weaponName);
		return !(weapon.contains("blowpipe (empty)")
			|| weapon.contains("(inactive)")
			|| weapon.contains("craw's bow (u)")
			|| weapon.contains("webweaver bow (u)")
			|| weapon.contains("venator bow (uncharged)")
			|| weapon.contains("echo venator bow (uncharged)")
			|| weapon.contains("tonalztics of ralos (uncharged)")
			|| weapon.contains("crystal bow (inactive)")
			|| weapon.contains("bow of faerdhinen (inactive)"));
	}

	static boolean usesAmmoSlot(String weaponName)
	{
		String weapon = NameMatcher.normalize(weaponName);
		return !contains(weapon,
			"blowpipe", "crystal bow", "bow of faerdhinen", "chinchompa",
			" dart", "knife", "thrownaxe", "javelin", "toktz-xil-ul",
			"holy water", "blisterwood stake", "craw's bow", "webweaver bow",
			"tonalztics of ralos", "hunter's spear", "mud pie");
	}

	static boolean isCompatible(String weaponName, String ammoName)
	{
		String weapon = NameMatcher.normalize(weaponName);
		String ammo = NameMatcher.normalize(ammoName);
		if (!usesAmmoSlot(weapon) || ammo.isEmpty()) return false;

		if (weapon.contains("hunter's sunlight crossbow") || weapon.contains("hunters' sunlight crossbow"))
			return ammo.contains("sunlight antler bolts") || ammo.contains("moonlight antler bolts");
		if (weapon.contains("hunter's crossbow") || weapon.contains("hunters' crossbow"))
			return ammo.contains("kebbit bolts");
		if (weapon.contains("karil's crossbow")) return ammo.contains("bolt rack");
		if (weapon.contains("dorgeshuun crossbow"))
			return ammo.contains("bone bolts") || contains(ammo, "bronze bolts", "blurite bolts", "iron bolts");
		if (weapon.contains("crossbow"))
		{
			int ammoTier = standardBoltTier(ammo);
			return ammoTier > 0 && ammoTier <= crossbowTier(weapon);
		}

		if (weapon.contains("eclipse atlatl")) return ammo.contains("atlatl dart");
		if (weapon.contains("ballista")) return ammo.contains("javelin");
		if (weapon.contains("salamander") || weapon.contains("swamp lizard"))
			return ammo.equals(requiredTar(weapon));
		if (weapon.contains("training bow")) return ammo.contains("training arrow");
		if (weapon.contains("comp ogre bow")) return isOgreArrow(ammo, true);
		if (weapon.contains("ogre bow")) return isOgreArrow(ammo, false);
		if (weapon.contains("bow"))
		{
			if (weapon.contains("bone shortbow") && ammo.contains("broad arrow")) return true;
			int ammoTier = standardArrowTier(ammo);
			return ammoTier > 0 && ammoTier <= bowTier(weapon);
		}
		return false;
	}

	private static int crossbowTier(String weapon)
	{
		if (contains(weapon, "dragon crossbow", "dragon hunter crossbow", "armadyl crossbow", "zaryte crossbow")) return 8;
		if (weapon.contains("rune crossbow")) return 7;
		if (weapon.contains("adamant crossbow")) return 6;
		if (weapon.contains("mithril crossbow")) return 5;
		if (weapon.contains("steel crossbow")) return 4;
		if (weapon.contains("iron crossbow")) return 3;
		if (weapon.contains("blurite crossbow")) return 2;
		return 1; // Crossbow, Phoenix crossbow, and Bronze crossbow.
	}

	private static int standardBoltTier(String ammo)
	{
		if (!ammo.contains("bolt") || contains(ammo, "bolt rack", "bone bolts", "kebbit bolts", "antler bolts",
			"unfinished", "unf", "tips", "grapple")) return 0;
		if (ammo.contains("dragon ") || ammo.startsWith("dragon bolts")) return 8;
		if (contains(ammo, "broad bolts", "amethyst broad bolts")) return 7;
		if (contains(ammo, "runite bolts", "rune bolts", "dragonstone bolts", "onyx bolts")) return 7;
		if (contains(ammo, "adamant bolts", "ruby bolts", "diamond bolts")) return 6;
		if (contains(ammo, "mithril bolts", "sapphire bolts", "emerald bolts")) return 5;
		if (contains(ammo, "steel bolts", "topaz bolts")) return 4;
		if (contains(ammo, "iron bolts", "pearl bolts", "silver bolts")) return 3;
		if (contains(ammo, "blurite bolts", "jade bolts")) return 2;
		if (contains(ammo, "bronze bolts", "opal bolts", "barbed bolts")) return 1;
		return 0;
	}

	private static int bowTier(String weapon)
	{
		if (contains(weapon, "twisted bow", "dark bow", "3rd age bow", "venator bow", "scorching bow")) return 8;
		if (contains(weapon, "magic shortbow", "magic longbow", "magic comp bow", "seercull")) return 7;
		if (contains(weapon, "yew shortbow", "yew longbow", "yew comp bow", "bone shortbow")) return 6;
		if (contains(weapon, "maple shortbow", "maple longbow", "maple comp bow")) return 5;
		if (contains(weapon, "willow shortbow", "willow longbow", "willow comp bow")) return 4;
		if (contains(weapon, "oak shortbow", "oak longbow", "oak comp bow")) return 3;
		return 2; // Basic, rain, and cursed-goblin bows use arrows through iron.
	}

	private static int standardArrowTier(String ammo)
	{
		if (!ammo.contains("arrow") || contains(ammo, "arrowtips", "headless", "training arrow", "ogre arrow", "brutal",
			"bullet", "field arrow", "blunt arrow", "barbed arrow")) return 0;
		if (ammo.contains("dragon")) return 8;
		if (ammo.contains("amethyst")) return 7;
		if (contains(ammo, "rune", "ice arrow")) return 6;
		if (ammo.contains("adamant")) return 5;
		if (ammo.contains("broad arrow")) return 7;
		if (ammo.contains("mithril")) return 4;
		if (ammo.contains("steel")) return 3;
		if (ammo.contains("iron")) return 2;
		if (ammo.contains("bronze")) return 1;
		return 0;
	}

	private static boolean isOgreArrow(String ammo, boolean composite)
	{
		if (ammo.contains("ogre arrow")) return true;
		if (!ammo.contains("brutal")) return false;
		return composite || contains(ammo, "bronze brutal", "iron brutal", "steel brutal", "black brutal", "mithril brutal");
	}

	private static String requiredTar(String weapon)
	{
		if (weapon.contains("swamp lizard")) return "guam tar";
		if (weapon.contains("orange salamander")) return "marrentill tar";
		if (weapon.contains("red salamander")) return "tarromin tar";
		if (weapon.contains("black salamander")) return "harralander tar";
		if (weapon.contains("tecu salamander")) return "irit tar";
		return "";
	}

	private static boolean contains(String value, String... tokens)
	{
		for (String token : tokens) if (value.contains(token)) return true;
		return false;
	}
}
