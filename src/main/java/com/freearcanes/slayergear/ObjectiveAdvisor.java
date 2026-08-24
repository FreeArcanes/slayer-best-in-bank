package com.freearcanes.slayergear;

/** Advisory objective guidance. It never changes the configured objective. */
final class ObjectiveAdvisor
{
	private ObjectiveAdvisor() {}

	static Suggestion suggest(SlayerTaskProfile profile, GearStrategy strategy)
	{
		String key = NameMatcher.normalize(profile == null ? "" : profile.getKey());
		String summary = NameMatcher.normalize(profile == null ? "" : profile.getSummary());
		String method = NameMatcher.normalize(strategy == null ? ""
			: strategy.getName() + " " + strategy.getLocation());
		if (summary.contains("turael/aya speed") || method.contains("turael/aya"))
		{
			return new Suggestion(GearPriority.VALUE,
				"Low-level speed assignments usually need fewer optional consumables.");
		}
		if (key.contains("boss") || BossSlayerCatalog.contains(
			profile == null ? "" : profile.getDisplayName()))
		{
			return new Suggestion(GearPriority.BALANCED,
				"Boss methods benefit most from target-aware damage and shorter kill times.");
		}
		if (strategy != null && (strategy.isAncientAoe()
			|| method.contains("venator") || method.contains("cannon")))
		{
			return new Suggestion(GearPriority.BALANCED,
				"This multi-target method is primarily selected for damage and task speed.");
		}
		if (strategy != null && (strategy.getMagicDefenceWeight() > 0
			|| strategy.getMinimumEquippedWeightKg() > 0))
		{
			return new Suggestion(GearPriority.DEFENCE_FIRST,
				"This method already emphasizes defensive stats or an equipment threshold.");
		}
		if (strategy != null && strategy.getPrayerWeight() >= 1.7)
		{
			return new Suggestion(GearPriority.PRAYER_FIRST,
				"This method has an explicit Prayer-sustain emphasis.");
		}
		return new Suggestion(GearPriority.PRAYER_FIRST,
			"Regular assignments often gain more convenience from longer Prayer trips.");
	}

	static String policy(GearPriority objective)
	{
		if (objective == GearPriority.PRAYER_FIRST)
		{
			return "Prayer gear · +50% Prayer/restoration target";
		}
		if (objective == GearPriority.DEFENCE_FIRST)
		{
			return "Tank armour · +50% food target";
		}
		if (objective == GearPriority.VALUE)
		{
			return "DPS-valid gear · -50% optional boost/support target";
		}
		return "Target DPS gear · +50% boost/aggression target";
	}

	static String changeFromMaxDps(GearPriority objective)
	{
		if (objective == GearPriority.PRAYER_FIRST)
		{
			return "Vs Max DPS: non-weapon slots shift toward Prayer bonus; Prayer and regeneration targets rise 50%.";
		}
		if (objective == GearPriority.DEFENCE_FIRST)
		{
			return "Vs Max DPS: non-weapon slots shift toward total defence; the food target rises 50%.";
		}
		if (objective == GearPriority.VALUE)
		{
			return "Vs Max DPS: DPS-valid gear remains; optional boost, Goading, and regeneration targets fall 50%.";
		}
		return "Max DPS baseline: target-aware tier ordering with 50% more boost and aggression support.";
	}

	static final class Suggestion
	{
		private final GearPriority objective;
		private final String reason;

		Suggestion(GearPriority objective, String reason)
		{
			this.objective = objective;
			this.reason = reason;
		}

		GearPriority getObjective() { return objective; }
		String getReason() { return reason; }
	}
}
