package com.freearcanes.slayergear;

public enum TripPlan
{
	FULL_ASSIGNMENT("Full assignment"),
	SHORT_TRIP("Short trip"),
	CUSTOM_KILLS("Custom kills");

	private final String label;

	TripPlan(String label) { this.label = label; }

	@Override
	public String toString() { return label; }
}
