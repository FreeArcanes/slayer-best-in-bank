package com.freearcanes.slayergear;

/** Conservative item-name policy for charge-dependent and degraded equipment variants. */
final class EquipmentChargePolicy
{
	private EquipmentChargePolicy() {}

	static boolean isUsable(String itemName)
	{
		String name = NameMatcher.normalize(itemName);
		if (name.isEmpty()) return false;
		if (name.contains("(broken)") || name.contains("dormant")
			|| name.contains("depleted") || name.contains("(damaged)"))
		{
			return false;
		}
		if ((name.contains("(uncharged)") || name.startsWith("uncharged ")
			|| name.endsWith("(u)") || name.contains("(inactive)")
			|| name.contains("(empty)")) && chargeStateControlsModeledPerformance(name))
		{
			return false;
		}
		return !isFullyDegradedBarrows(name);
	}

	static String note(String itemName)
	{
		String name = NameMatcher.normalize(itemName);
		if (isCorruptedCrystalWeapon(name)) return "";
		if (has(name, "scythe of vitur", "tumeken's shadow", "sanguinesti staff",
			"trident of the", "uncharged trident", "toxic blowpipe", "toxic staff", "serpentine helm",
			"craw's bow", "webweaver bow", "viggora's chainmace", "ursine chainmace",
			"thammaron's sceptre", "accursed sceptre", "crystal bow",
			"bow of faerdhinen", "blade of saeldor", "crystal helm", "crystal body", "crystal legs",
			"venator bow", "echo venator bow", "tonalztics of ralos",
			"dizana's quiver", "dizana’s quiver")
			|| isBarrowsEquipment(name))
		{
			return "charge-dependent; verify charges before leaving";
		}
		return "";
	}

	private static boolean chargeStateControlsModeledPerformance(String name)
	{
		return has(name, "scythe of vitur", "tumeken's shadow", "sanguinesti staff",
			"trident", "toxic blowpipe", "toxic staff", "serpentine helm",
			"craw's bow", "webweaver bow", "viggora's chainmace", "ursine chainmace",
			"thammaron's sceptre", "accursed sceptre", "crystal bow",
			"bow of faerdhinen", "blade of saeldor", "crystal helm", "crystal body", "crystal legs",
			"venator bow", "echo venator bow", "tonalztics of ralos");
	}

	private static boolean isCorruptedCrystalWeapon(String name)
	{
		return name.contains("(c)")
			&& has(name, "bow of faerdhinen", "blade of saeldor");
	}

	private static boolean isFullyDegradedBarrows(String name)
	{
		return isBarrowsEquipment(name) && name.matches(".*\\s0$");
	}

	private static boolean isBarrowsEquipment(String name)
	{
		return has(name, "ahrim's", "dharok's", "guthan's", "karil's",
			"torag's", "verac's");
	}

	private static boolean has(String name, String... tokens)
	{
		for (String token : tokens) if (name.contains(token)) return true;
		return false;
	}
}
