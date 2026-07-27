package com.freearcanes.slayergear;

enum PrepFocusMode
{
	ALL("All"),
	MISSING("Missing"),
	GEAR("Gear"),
	SUPPLIES("Supplies");

	private final String displayName;

	PrepFocusMode(String displayName)
	{
		this.displayName = displayName;
	}

	String getDisplayName()
	{
		return displayName;
	}

	PrepFocusMode next()
	{
		PrepFocusMode[] values = values();
		return values[(ordinal() + 1) % values.length];
	}
}
