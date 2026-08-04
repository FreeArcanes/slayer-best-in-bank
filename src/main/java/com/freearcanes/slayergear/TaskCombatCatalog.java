package com.freearcanes.slayergear;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Task-wide combat mechanics that ordinary item stat sheets cannot express.
 *
 * <p>The catalog deliberately separates three ideas:</p>
 * <ul>
 *   <li>the melee attack type a monster materially favours,</li>
 *   <li>monster attributes which activate target-specific equipment effects,</li>
 *   <li>standard-spellbook elemental weaknesses.</li>
 * </ul>
 *
 * <p>Every Slayer-master assignment is registered, even when the correct rule
 * is "no task-wide special modifier". That makes coverage auditable and keeps
 * future game changes from silently falling through an unclassified task.</p>
 */
final class TaskCombatCatalog
{
	private static final AliasCatalog<Rule> RULES = new AliasCatalog<>(TaskCombatCatalog::normalize);

	static
	{
		// First classify every assignment. Specific mechanics below replace these
		// defaults. This is intentionally sourced from SlayerMasterCatalog so a new
		// assignment added there must be explicitly visible to coverage tests/docs.
		for (java.util.List<String> assignments : SlayerMasterCatalog.allAssignments().values())
		{
			for (String task : assignments)
			{
				RULES.register(Rule.generic(), AliasCatalog.CollisionPolicy.KEEP_FIRST, task);
			}
		}

		// Undead / spectral targets. Salve interactions are not forced because the
		// Slayer helmet and Salve bonuses do not simply stack; the trait is retained
		// for task context and future loadout-level comparison logic.
		register(rule(WeaponRule.ANY, AttackType.BALANCED,
			traits(TargetTrait.UNDEAD, TargetTrait.SPECTRAL), ElementalWeakness.AIR, 50,
			"Undead/spectral target; Air spells receive the listed elemental weakness."),
			"Aberrant spectres");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.UNDEAD), ElementalWeakness.NONE, 0,
			"Undead target."), "Ankou", "Zombies");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(), ElementalWeakness.NONE, 0,
			"Current Slayer task data does not classify Ghouls with a special target attribute."), "Ghouls");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.UNDEAD, TargetTrait.WILDERNESS), ElementalWeakness.AIR, 30,
			"Undead Wilderness target: charged Wilderness weapons gain +50% accuracy/damage, and Air Magic has a 30% elemental weakness."), "Revenants");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.UNDEAD), ElementalWeakness.AIR, 50,
			"Undead target with an Air elemental weakness."), "Ghosts");
		register(rule(WeaponRule.ANY, AttackType.CRUSH, traits(TargetTrait.UNDEAD), ElementalWeakness.NONE, 0,
			"Skeleton variants generally favour Crush, but elemental weakness varies by variant/location; no single task-wide element is forced."), "Skeletons");
		register(rule(WeaponRule.SHADE, AttackType.BALANCED,
			traits(TargetTrait.SHADE, TargetTrait.UNDEAD, TargetTrait.SPECTRAL), ElementalWeakness.AIR, 40,
			"Shades are undead/spectral; Gadderhammer has a Shade-specific damage effect and Air Magic is effective."),
			"Shades", "Shade");

		// Demonbane. Element values are only encoded where the current task target
		// has a stable task-wide weakness; Bloodveld/Nechryael deliberately do not
		// inherit one just because they are demons.
		register(demon(ElementalWeakness.NONE, 0), "Abyssal demons");
		register(demon(ElementalWeakness.WATER, 40), "Black demons", "Greater demons", "Lesser demons");
		register(demon(ElementalWeakness.NONE, 0), "Bloodveld", "Nechryael");
		register(demon(ElementalWeakness.WATER, 50), "Hellhounds");
		register(rule(WeaponRule.DEMONBANE, AttackType.BALANCED, traits(TargetTrait.DEMON, TargetTrait.FIERY), ElementalWeakness.WATER, 100,
			"Demonic/fiery target: Demonbane applies and Water Magic receives a 100% elemental weakness."), "Pyrefiends");

		// Dragonbane. FIERY is used only for fire-breathing families where Pearl
		// bolt effects are relevant; icy wyverns/frost dragons are not marked fiery.
		register(dragon(ElementalWeakness.WATER, 50, true),
			"Black dragons", "Green dragons", "Red dragons", "Lava Dragons");
		register(dragon(ElementalWeakness.WATER, 50, true), "Blue dragons");
		register(dragon(ElementalWeakness.WATER, 50, true), "Drakes");
		register(rule(WeaponRule.DRAGONBANE, AttackType.STAB, traits(TargetTrait.DRAGON, TargetTrait.FIERY), ElementalWeakness.EARTH, 50,
			"Draconic metal target: Dragonbane applies; stab is the melee route and Earth Magic receives a 50% weakness."),
			"Metal dragons");
		register(rule(WeaponRule.DRAGONBANE, AttackType.BALANCED, traits(TargetTrait.DRAGON), ElementalWeakness.FIRE, 100,
			"Draconic icy target: Dragonbane applies and Fire Magic receives a 100% elemental weakness."), "Frost dragons");
		register(dragon(ElementalWeakness.NONE, 0, false), "Fossil island wyverns", "Hydras");
		register(dragon(ElementalWeakness.FIRE, 25, false), "Skeletal wyverns");
		register(dragon(ElementalWeakness.EARTH, 50, false), "Wyrms");

		// Golembane / physical-style weaknesses.
		register(rule(WeaponRule.GOLEMBANE, AttackType.CRUSH, traits(TargetTrait.GOLEM), ElementalWeakness.EARTH, 40,
			"Gargoyles are golems: Granite hammer gains Golembane, Crush is favoured, and Earth Magic is an alternative."),
			"Gargoyles", "Gargoyle");
		register(rule(WeaponRule.DEMONBANE, AttackType.CRUSH, traits(TargetTrait.DEMON), ElementalWeakness.EARTH, 100,
			"Waterfiends are demons: Demonbane passives apply. Crush is the normal melee affinity, while Earth Magic has a 100% elemental weakness."),
			"Waterfiends", "Waterfiend");
		register(rule(WeaponRule.ANY, AttackType.CRUSH, traits(), ElementalWeakness.FIRE, 100,
			"Ice warriors favour Crush and have a 100% Fire elemental weakness."), "Ice warriors");
		register(rule(WeaponRule.ANY, AttackType.CRUSH, traits(), ElementalWeakness.NONE, 0,
			"Crush is the preferred melee attack type for this target."), "Earth warriors", "Dark warriors", "Shadow warriors");
		register(rule(WeaponRule.ANY, AttackType.STAB, traits(), ElementalWeakness.NONE, 0,
			"Stab is the preferred melee attack type for this target."), "Crocodiles");
		register(rule(WeaponRule.ANY, AttackType.SLASH, traits(), ElementalWeakness.NONE, 0,
			"Slash is the preferred melee attack type for this target."), "Scorpions");
		register(rule(WeaponRule.ANY, AttackType.CRUSH, traits(), ElementalWeakness.EARTH, 40,
			"Basilisks favour Crush and have a 40% Earth elemental weakness."), "Basilisks");
		register(rule(WeaponRule.ANY, AttackType.CRUSH, traits(), ElementalWeakness.EARTH, 60,
			"Molanisks favour Crush and have a 60% Earth elemental weakness."), "Molanisks");

		// Kalphite / leafy / rat / vampyre families with mechanical weapon effects.
		register(rule(WeaponRule.KALPHITE, AttackType.BALANCED, traits(TargetTrait.KALPHITE), ElementalWeakness.NONE, 0,
			"Keris weapons gain Kalphite/Scabarite damage effects; Breaching also gains target accuracy."),
			"Kalphites", "Minions of Scabaras", "Scabarites");
		register(rule(WeaponRule.LEAF_BLADED, AttackType.BALANCED, traits(TargetTrait.LEAFY), ElementalWeakness.NONE, 0,
			"Only valid leaf-bane/Slayer weapons can damage this target; Leaf-bladed battleaxe gains a target damage bonus."),
			"Kurask", "Turoth");
		register(rule(WeaponRule.RATBANE, AttackType.BALANCED, traits(TargetTrait.RAT), ElementalWeakness.NONE, 0,
			"Rat-bone weapons receive their rat-specific +10 max-hit effect."), "Rats", "Brine rats");
		register(rule(WeaponRule.VAMPYRE, AttackType.BALANCED, traits(TargetTrait.VAMPYRE, TargetTrait.UNDEAD), ElementalWeakness.NONE, 0,
			"Vampyre-specific weapons are required/strongly favoured; modern Sunspear/Hallowed/Blisterwood effects are scored."),
			"Vampyres", "Venators");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(), ElementalWeakness.NONE, 0,
			"Werewolf task: do not prefer Wolfbane in Canifis because it prevents the human citizen from transforming, and human-form kills do not count for the Werewolf Slayer assignment."),
			"Werewolves");

		// Elemental weaknesses that do not imply a special weapon family.
		register(element(ElementalWeakness.FIRE, 50, "Araxytes have a strong Fire elemental weakness."), "Araxytes");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.FLYING), ElementalWeakness.AIR, 45,
			"Flying target with a strong Air elemental weakness."), "Aviansies");
		register(element(ElementalWeakness.AIR, 30, "Banshees have a 30% Air elemental weakness."), "Banshees");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.FLYING), ElementalWeakness.NONE, 0,
			"Bat assignments include variants with different Air weakness values; no single task-wide element is forced."), "Bats");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(), ElementalWeakness.NONE, 0,
			"Bear assignments include variants with different Fire weakness values; no single task-wide element is forced."), "Bears");
		register(element(ElementalWeakness.FIRE, 50, "Cave bugs have a 50% Fire elemental weakness."), "Cave bugs");
		register(element(ElementalWeakness.FIRE, 30, "Cave horrors have a 30% Fire elemental weakness."), "Cave horrors");
		register(element(ElementalWeakness.EARTH, 50, "Cave kraken have a 50% Earth elemental weakness."), "Cave kraken");
		register(element(ElementalWeakness.EARTH, 50, "Cave slimes have a 50% Earth elemental weakness."), "Cave slimes");
		register(element(ElementalWeakness.EARTH, 60, "Dark beasts have a 60% Earth elemental weakness."), "Dark beasts");
		register(element(ElementalWeakness.FIRE, 40, "Ents have a 40% Fire elemental weakness."), "Ents");
		register(element(ElementalWeakness.FIRE, 25, "Fever spiders have a 25% Fire elemental weakness."), "Fever spiders");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.FLYING), ElementalWeakness.NONE, 0,
			"Bird assignments span many variants with different or absent elemental weaknesses; no single task-wide element is forced."), "Birds");
		register(element(ElementalWeakness.FIRE, 30, "Mature Custodian stalkers have a Fire elemental weakness."), "Custodian stalkers");
		register(element(ElementalWeakness.AIR, 35, "Dust devils have an Air elemental weakness."), "Dust devils");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.FIERY), ElementalWeakness.WATER, 100,
			"Fiery target with a 100% Water elemental weakness."), "Fire giants");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.FLYING), ElementalWeakness.AIR, 50,
			"Flying target with a 50% Air elemental weakness."), "Gryphons");
		register(element(ElementalWeakness.FIRE, 50, "Harpie bug swarms have a Fire elemental weakness."), "Harpie bug swarms");
		register(element(ElementalWeakness.FIRE, 100, "Ice giants have a 100% Fire elemental weakness."), "Ice giants");
		register(rule(WeaponRule.DEMONBANE, AttackType.BALANCED, traits(TargetTrait.DEMON), ElementalWeakness.FIRE, 100,
			"Icefiends are demons: Demonbane passives apply and Fire Magic has a 100% elemental weakness."), "Icefiends");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.FLYING), ElementalWeakness.AIR, 60,
			"Flying target with a 60% Air elemental weakness."), "Killerwatts");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(TargetTrait.SPECTRAL), ElementalWeakness.NONE, 0,
			"Spectral target; no stable task-wide weapon passive is forced by the current optimizer."), "Lesser Nagua");
		register(element(ElementalWeakness.FIRE, 50, "Moss giants have a Fire elemental weakness."), "Moss giants");
		register(element(ElementalWeakness.EARTH, 25, "Rockslugs have a 25% Earth elemental weakness."), "Rockslugs");
		register(element(ElementalWeakness.AIR, 30, "Smoke devils have an Air elemental weakness; Burst/Barrage remains the primary multi-target XP method."), "Smoke devils");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(), ElementalWeakness.NONE, 0,
			"Spider assignments can be completed on substantially different spider variants; no single task-wide element is forced."), "Spiders");
		register(element(ElementalWeakness.EARTH, 20, "Suqahs have a 20% Earth elemental weakness."), "Suqahs");
		register(element(ElementalWeakness.FIRE, 50, "Mountain-troll routes have a Fire elemental weakness."), "Trolls");
		register(rule(WeaponRule.ANY, AttackType.BALANCED, traits(), ElementalWeakness.NONE, 0,
			"Elemental weakness is faction-dependent: non-Zaros spiritual creatures use Air weakness while Zarosian variants use a much larger Fire weakness; no single task-wide element is forced."),
			"Spiritual creatures");
		register(rule(WeaponRule.DRAGONBANE, AttackType.BALANCED, traits(TargetTrait.DRAGON), ElementalWeakness.NONE, 0,
			"Fossil Island wyvern variants are draconic but their elemental values vary by variant; Dragonbane is scored without forcing one task-wide element."),
			"Fossil island wyverns");

		// Aliases used by RuneLite/boss task names but not stored as master category
		// labels. These inherit the same combat mechanic as their task family.
		alias("gargoyle", "Gargoyles");
		alias("the grotesque guardians", "Gargoyles");
		alias("grotesque guardians", "Gargoyles");
		alias("waterfiend", "Waterfiends");
		alias("vampyre", "Vampyres");
		alias("vyrewatch", "Vampyres");
		alias("venator", "Venators");
		alias("scabarites", "Minions of Scabaras");
		alias("smoke devil", "Smoke devils");
		alias("dust devil", "Dust devils");
		alias("black dragon", "Black dragons");
		alias("green dragon", "Green dragons");
		alias("red dragon", "Red dragons");
		alias("blue dragon", "Blue dragons");
	}

	private TaskCombatCatalog() { }

	static SlayerTaskProfile enrich(SlayerTaskProfile profile, String taskName)
	{
		return enrich(profile, taskName, null);
	}

	static SlayerTaskProfile enrich(SlayerTaskProfile profile, String taskName, String assignedLocation)
	{
		Rule rule = contextualRule(taskName, assignedLocation);

		String summary = profile.getSummary();
		if (!rule.note.isEmpty() && !rule.isGeneric()
			&& !NameMatcher.normalize(summary).contains(NameMatcher.normalize(rule.note)))
		{
			summary = summary + " " + rule.note;
		}

		SlayerTaskProfile.Builder builder = SlayerTaskProfile.builder()
			.key(profile.getKey())
			.displayName(profile.getDisplayName())
			.summary(summary)
			.protectionAdvice(profile.getProtectionAdvice());

		boolean hasMelee = false;
		boolean hasRanged = false;
		boolean hasMagic = false;
		boolean alreadyHasElementMethod = false;
		String elementToken = rule.elementalWeakness.displayName().toLowerCase(Locale.ENGLISH);
		for (GearStrategy source : profile.getStrategies())
		{
			GearStrategy enriched = enrich(source, rule);
			builder.strategy(enriched);
			hasMelee |= source.getCombatStyle() == CombatStyle.MELEE;
			hasRanged |= source.getCombatStyle() == CombatStyle.RANGED;
			hasMagic |= source.getCombatStyle() == CombatStyle.MAGIC;
			if (!elementToken.isEmpty() && source.getCombatStyle() == CombatStyle.MAGIC
				&& NameMatcher.normalize(source.getName()).contains(elementToken)) alreadyHasElementMethod = true;
		}

		// Target-family methods only appear when their signature weapon is owned;
		// requiredWeapon is enforced by the existing eligibility engine.
		if (rule.weaponRule == WeaponRule.DEMONBANE)
		{
			if (!hasRanged)
			{
				builder.strategy(signatureMethod("Demonbane Ranged — Scorching bow", CombatStyle.RANGED, rule, "scorching bow"));
				builder.strategy(signatureMethod("Demonbane Ranged — Holy water", CombatStyle.RANGED, rule, "holy water"));
			}
			if (!hasMagic) builder.strategy(signatureMethod("Demonbane Magic + Mark of Darkness", CombatStyle.MAGIC, rule, "purging staff"));
		}
		else if (rule.weaponRule == WeaponRule.DRAGONBANE)
		{
			if (!hasRanged) builder.strategy(signatureMethod("Dragonbane Ranged", CombatStyle.RANGED, rule, "dragon hunter crossbow"));
			if (!hasMagic) builder.strategy(signatureMethod("Dragonbane Magic", CombatStyle.MAGIC, rule, "dragon hunter wand"));
		}
		else if (rule.weaponRule == WeaponRule.RATBANE)
		{
			if (!hasMelee) builder.strategy(signatureMethod("Ratbane melee", CombatStyle.MELEE, rule, "bone mace"));
			if (!hasRanged) builder.strategy(signatureMethod("Ratbane Ranged", CombatStyle.RANGED, rule, "bone shortbow"));
			if (!hasMagic) builder.strategy(signatureMethod("Ratbane Magic", CombatStyle.MAGIC, rule, "bone staff"));
		}

		// Elemental weakness applies only to Standard-spellbook elemental spells.
		// Add it as an alternate method instead of displacing curated Barrage,
		// Venator, cannon, or other task methods that are intentionally first.
		if (rule.elementalWeakness != ElementalWeakness.NONE && rule.elementalWeaknessPercent >= 30
			&& !alreadyHasElementMethod)
		{
			builder.strategy(elementalMethod(rule));
		}
		return builder.build();
	}

	private static Rule contextualRule(String taskName, String assignedLocation)
	{
		Rule base = RULES.get(taskName);
		if (base == null) base = Rule.generic();

		String task = normalize(taskName);
		String location = normalize(assignedLocation);

		// The Ruins of Tapoyauik blue-dragon variant uses Fire rather than the
		// ordinary blue-dragon Water weakness.
		if (task.equals("blue dragons") || task.equals("blue dragon"))
		{
			if (location.contains("tapoyauik"))
			{
				return dragon(ElementalWeakness.FIRE, 50, true);
			}
		}

		// Trollweiss/Ice Path ice-troll assignments are substantially more
		// vulnerable to Fire than ordinary mountain-troll routes.
		if (task.equals("trolls") || task.equals("troll"))
		{
			if (location.contains("trollweiss") || location.contains("ice path"))
			{
				return element(ElementalWeakness.FIRE, 100,
					"Ice-troll route with a 100% Fire elemental weakness.");
			}
		}

		return base;
	}

	static boolean isAudited(String taskName)
	{
		return RULES.contains(taskName);
	}

	static WeaponRule ruleFor(String taskName)
	{
		Rule rule = RULES.get(taskName);
		return rule == null ? WeaponRule.ANY : rule.weaponRule;
	}

	static AttackType meleeAttackTypeFor(String taskName)
	{
		Rule rule = RULES.get(taskName);
		return rule == null ? AttackType.BALANCED : rule.attackType;
	}

	static Set<TargetTrait> traitsFor(String taskName)
	{
		Rule rule = RULES.get(taskName);
		return rule == null ? Collections.emptySet() : rule.targetTraits;
	}

	static ElementalWeakness elementalWeaknessFor(String taskName)
	{
		Rule rule = RULES.get(taskName);
		return rule == null ? ElementalWeakness.NONE : rule.elementalWeakness;
	}

	static int elementalWeaknessPercentFor(String taskName)
	{
		Rule rule = RULES.get(taskName);
		return rule == null ? 0 : rule.elementalWeaknessPercent;
	}

	static String noteFor(String taskName)
	{
		Rule rule = RULES.get(taskName);
		return rule == null ? Rule.GENERIC_NOTE : rule.note;
	}

	static Set<String> auditedTasks()
	{
		return Collections.unmodifiableSet(new LinkedHashSet<>(RULES.snapshot().keySet()));
	}

	private static GearStrategy signatureMethod(String name, CombatStyle style, Rule rule, String weapon)
	{
		GearStrategy.Builder builder = GearStrategy.builder()
			.name(name)
			.location("Task-dependent")
			.rationale(rule.note)
			.combatStyle(style)
			.weaponRule(rule.weaponRule)
			.targetTraits(rule.targetTraits)
			.elementalWeakness(rule.elementalWeakness, rule.elementalWeaknessPercent)
			.requiredWeapon(weapon);
		if (style == CombatStyle.MELEE) builder.attackType(rule.attackType);
		return builder.build();
	}

	private static GearStrategy elementalMethod(Rule rule)
	{
		String element = rule.elementalWeakness.displayName();
		return GearStrategy.builder()
			.name(element + " Magic")
			.location("Task-dependent")
			.rationale("Standard-spellbook " + element + " spells gain " + rule.elementalWeaknessPercent
				+ "% accuracy and damage from this monster's elemental weakness.")
			.combatStyle(CombatStyle.MAGIC)
			.targetTraits(rule.targetTraits)
			.elementalWeakness(rule.elementalWeakness, rule.elementalWeaknessPercent)
			.build();
	}

	private static GearStrategy enrich(GearStrategy source, Rule rule)
	{
		WeaponRule weaponRule = source.getWeaponRule() == WeaponRule.ANY ? rule.weaponRule : source.getWeaponRule();
		AttackType attackType = source.getAttackType();
		if (source.getCombatStyle() == CombatStyle.MELEE && attackType == AttackType.BALANCED
			&& rule.attackType != AttackType.BALANCED)
		{
			attackType = rule.attackType;
		}

		EnumSet<TargetTrait> traits = EnumSet.noneOf(TargetTrait.class);
		traits.addAll(rule.targetTraits);
		traits.addAll(source.getTargetTraits());
		ElementalWeakness element = source.getElementalWeakness() == ElementalWeakness.NONE
			? rule.elementalWeakness : source.getElementalWeakness();
		int elementPercent = source.getElementalWeakness() == ElementalWeakness.NONE
			? rule.elementalWeaknessPercent : source.getElementalWeaknessPercent();

		GearStrategy.Builder copy = GearStrategy.builder()
			.name(source.getName())
			.location(source.getLocation())
			.rationale(source.getRationale())
			.combatStyle(source.getCombatStyle())
			.attackType(attackType)
			.weaponRule(weaponRule)
			.targetTraits(traits)
			.elementalWeakness(element, elementPercent)
			.minimumMagic(source.getMinimumMagic())
			.minimumRanged(source.getMinimumRanged())
			.magicDefenceWeight(source.getMagicDefenceWeight())
			.prayerWeight(source.getPrayerWeight())
			.ancientAoe(source.isAncientAoe());
		if (source.getRequiredWeapon() != null) copy.requiredWeapon(source.getRequiredWeapon());
		if (source.getRequiredOffhand() != null) copy.requiredOffhand(source.getRequiredOffhand());

		// Only explicit method-specific preferences survive. Target-family weapons
		// are scored dynamically by WeaponCombatRules instead of receiving a hard
		// +1000 override that can make a weaker weapon win incorrectly.
		for (String preferred : source.getPreferredItems()) copy.preferredItem(preferred);
		return copy.build();
	}

	private static Rule demon(ElementalWeakness element, int percent)
	{
		return rule(WeaponRule.DEMONBANE, AttackType.BALANCED, traits(TargetTrait.DEMON), element, percent,
			"Demonic target: Demonbane weapon/spell effects are included in scoring."
				+ (element == ElementalWeakness.NONE ? "" : " " + element.displayName() + " Magic receives a " + percent + "% weakness."));
	}

	private static Rule dragon(ElementalWeakness element, int percent, boolean fiery)
	{
		EnumSet<TargetTrait> targetTraits = traits(TargetTrait.DRAGON);
		if (fiery) targetTraits.add(TargetTrait.FIERY);
		return rule(WeaponRule.DRAGONBANE, AttackType.BALANCED, targetTraits, element, percent,
			"Draconic target: Dragonbane effects are included in scoring."
				+ (element == ElementalWeakness.NONE ? "" : " " + element.displayName() + " Magic receives a " + percent + "% weakness."));
	}

	private static Rule element(ElementalWeakness element, int percent, String note)
	{
		return rule(WeaponRule.ANY, AttackType.BALANCED, traits(), element, percent, note);
	}

	private static Rule rule(WeaponRule weaponRule, AttackType attackType, EnumSet<TargetTrait> traits,
		ElementalWeakness element, int percent, String note)
	{
		return new Rule(weaponRule, attackType, traits, element, percent, note);
	}

	private static EnumSet<TargetTrait> traits(TargetTrait... traits)
	{
		EnumSet<TargetTrait> set = EnumSet.noneOf(TargetTrait.class);
		if (traits != null) Collections.addAll(set, traits);
		return set;
	}

	private static void register(Rule rule, String... taskNames)
	{
		RULES.register(rule, AliasCatalog.CollisionPolicy.REPLACE, taskNames);
	}

	private static void alias(String alias, String sourceTask)
	{
		Rule source = RULES.get(sourceTask);
		if (source != null)
		{
			RULES.register(source, AliasCatalog.CollisionPolicy.REPLACE, alias);
		}
	}

	private static String normalize(String value)
	{
		if (value == null) return "";
		String normalized = value.trim().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+", " ").trim();
		return normalized.startsWith("the ") ? normalized.substring(4) : normalized;
	}

	static Map<String, WeaponRule> weaponRulesSnapshot()
	{
		Map<String, WeaponRule> snapshot = new LinkedHashMap<>();
		for (Map.Entry<String, Rule> entry : RULES.snapshot().entrySet())
		{
			snapshot.put(entry.getKey(), entry.getValue().weaponRule);
		}
		return Collections.unmodifiableMap(snapshot);
	}

	private static final class Rule
	{
		private static final String GENERIC_NOTE = "Generic / no task-wide special weapon modifier encoded.";
		private final WeaponRule weaponRule;
		private final AttackType attackType;
		private final Set<TargetTrait> targetTraits;
		private final ElementalWeakness elementalWeakness;
		private final int elementalWeaknessPercent;
		private final String note;

		private Rule(WeaponRule weaponRule, AttackType attackType, Set<TargetTrait> targetTraits,
			ElementalWeakness elementalWeakness, int elementalWeaknessPercent, String note)
		{
			this.weaponRule = weaponRule;
			this.attackType = attackType;
			this.targetTraits = Collections.unmodifiableSet(targetTraits.isEmpty()
				? EnumSet.noneOf(TargetTrait.class) : EnumSet.copyOf(targetTraits));
			this.elementalWeakness = elementalWeakness == null ? ElementalWeakness.NONE : elementalWeakness;
			this.elementalWeaknessPercent = Math.max(0, elementalWeaknessPercent);
			this.note = note == null ? "" : note;
		}

		private static Rule generic()
		{
			return new Rule(WeaponRule.ANY, AttackType.BALANCED, EnumSet.noneOf(TargetTrait.class),
				ElementalWeakness.NONE, 0, GENERIC_NOTE);
		}

		private boolean isGeneric()
		{
			return GENERIC_NOTE.equals(note);
		}
	}
}
