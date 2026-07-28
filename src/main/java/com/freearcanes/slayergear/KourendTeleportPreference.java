package com.freearcanes.slayergear;

public enum KourendTeleportPreference
{
	XERICS_TALISMAN_FIRST("Xeric's talisman first"),
	RADAS_BLESSING_FIRST("Rada's blessing first"),
	KOUREND_TELEPORT_FIRST("Kourend teleport first");

	private final String label;

	KourendTeleportPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
