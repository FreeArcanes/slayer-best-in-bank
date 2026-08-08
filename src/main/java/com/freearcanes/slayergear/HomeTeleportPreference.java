package com.freearcanes.slayergear;

public enum HomeTeleportPreference
{
	TELEPORT_TO_HOUSE("Teleport to house tablet"),
	CONSTRUCTION_CAPE("Construction cape"),
	MAX_CAPE("Max cape"),
	RUNES("Runes"),
	NONE("None");

	private final String label;

	HomeTeleportPreference(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
