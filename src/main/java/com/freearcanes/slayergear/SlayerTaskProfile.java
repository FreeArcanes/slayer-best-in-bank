package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SlayerTaskProfile
{
	private final String key;
	private final String displayName;
	private final String summary;
	private final String protectionAdvice;
	private final List<GearStrategy> strategies;

	private SlayerTaskProfile(Builder builder)
	{
		key = builder.key;
		displayName = builder.displayName;
		summary = builder.summary;
		protectionAdvice = builder.protectionAdvice;
		strategies = Collections.unmodifiableList(new ArrayList<>(builder.strategies));
	}

	static Builder builder() { return new Builder(); }
	String getKey() { return key; }
	String getDisplayName() { return displayName; }
	String getSummary() { return summary; }
	String getProtectionAdvice() { return protectionAdvice; }
	List<GearStrategy> getStrategies() { return strategies; }

	static final class Builder
	{
		private String key;
		private String displayName;
		private String summary;
		private String protectionAdvice;
		private final List<GearStrategy> strategies = new ArrayList<>();
		Builder key(String value) { key = value; return this; }
		Builder displayName(String value) { displayName = value; return this; }
		Builder summary(String value) { summary = value; return this; }
		Builder protectionAdvice(String value) { protectionAdvice = value; return this; }
		Builder strategy(GearStrategy value) { if (value != null) strategies.add(value); return this; }
		SlayerTaskProfile build() { return new SlayerTaskProfile(this); }
	}
}