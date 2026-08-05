package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Live Slayer-master/task relationship catalog used for UI context.
 *
 * <p>Assignment lists are intentionally kept separate from the task profile
 * engine: a task may use a curated profile or the safe generic fallback while
 * still reporting every master that can assign it.</p>
 */
final class SlayerMasterCatalog
{
	private static final Map<String, Set<String>> TASK_TO_MASTERS = new LinkedHashMap<>();
	private static final Map<String, List<String>> MASTER_TO_TASKS = new LinkedHashMap<>();
	private static final Map<String, List<MasterAssignment>> MASTER_ASSIGNMENT_DETAILS = new LinkedHashMap<>();
	private static final Map<String, MasterRules> MASTER_RULES = new LinkedHashMap<>();

	static
	{
		master("Turael / Aya", "Banshees", "Bats", "Bears", "Birds", "Cave bugs", "Cave crawlers", "Cave slimes", "Cows", "Crawling hands", "Dogs", "Dwarves", "Ghosts", "Goblins", "Icefiends", "Kalphites", "Lizards", "Minotaurs", "Monkeys", "Rats", "Scorpions", "Skeletons", "Spiders", "Wolves", "Zombies");
		master("Spria", "Banshees", "Bats", "Bears", "Birds", "Cave bugs", "Cave crawlers", "Cave slimes", "Cows", "Crawling hands", "Dogs", "Dwarves", "Ghosts", "Goblins", "Icefiends", "Kalphites", "Lizards", "Minotaurs", "Monkeys", "Rats", "Scorpions", "Skeletons", "Sourhogs", "Spiders", "Wolves", "Zombies");

		master("Mazchna / Achtryn",
			"Banshees", "Bats", "Bears", "Catablepon", "Cave bugs", "Cave crawlers", "Cave slimes", "Cockatrice", "Crabs",
			"Crawling hands", "Dogs", "Flesh crawlers", "Ghosts", "Ghouls", "Hill giants", "Hobgoblins", "Ice warriors",
			"Kalphites", "Killerwatts", "Lizards", "Mogres", "Pyrefiends", "Rockslugs", "Scorpions", "Shades", "Skeletons",
			"Vampyres", "Wall beasts", "Wolves", "Zombies");

		master("Vannaka",
			"Aberrant spectres", "Abyssal demons", "Ankou", "Basilisks", "Bloodveld", "Blue dragons", "Brine rats", "Cockatrice",
			"Crabs", "Crocodiles", "Dagannoth", "Dust devils", "Elves", "Fever spiders", "Fire giants", "Gargoyles", "Ghouls",
			"Gryphons", "Harpie bug swarms", "Hellhounds", "Hill giants", "Hobgoblins", "Ice giants", "Ice warriors", "Infernal mages",
			"Jellies", "Jungle horrors", "Kalphites", "Kurask", "Lesser demons", "Mogres", "Molanisks", "Moss giants", "Nechryael",
			"Ogres", "Otherworldly beings", "Pyrefiends", "Sea snakes", "Shades", "Shadow warriors", "Spiritual creatures",
			"Terror dogs", "Trolls", "Turoth", "Vampyres", "Werewolves");

		master("Chaeldar",
			"Aberrant spectres", "Abyssal demons", "Aviansies", "Basilisks", "Black demons", "Bloodveld", "Blue dragons", "Brine rats",
			"Cave horrors", "Cave kraken", "Crabs", "Custodian stalkers", "Dagannoth", "Dust devils", "Elves", "Fever spiders",
			"Fire giants", "Fossil island wyverns", "Gargoyles", "Greater demons", "Gryphons", "Hellhounds", "Jellies", "Jungle horrors",
			"Kalphites", "Kurask", "Lesser demons", "Lesser Nagua", "Lizardmen", "Mutated zygomites", "Nechryael", "Shadow warriors",
			"Skeletal wyverns", "Spiritual creatures", "Trolls", "Turoth", "TzHaar", "Vampyres", "Warped Creatures", "Wyrms");

		master("Konar quo Maten",
			"Aberrant spectres", "Abyssal demons", "Ankou", "Aviansies", "Basilisks", "Black demons", "Black dragons", "Bloodveld",
			"Blue dragons", "Bosses", "Brine rats", "Cave kraken", "Dagannoth", "Dark beasts", "Drakes", "Dust devils", "Fire giants",
			"Fossil island wyverns", "Gargoyles", "Greater demons", "Hellhounds", "Hydras", "Jellies", "Kalphites", "Kurask",
			"Lesser Nagua", "Lizardmen", "Metal dragons", "Mutated zygomites", "Nechryael", "Red dragons", "Skeletal wyverns",
			"Smoke devils", "Trolls", "Turoth", "Vampyres", "Warped Creatures", "Waterfiends", "Wyrms");

		master("Nieve / Steve",
			"Aberrant spectres", "Abyssal demons", "Ankou", "Aquanites", "Araxytes", "Aviansies", "Basilisks", "Black demons",
			"Black dragons", "Bloodveld", "Blue dragons", "Bosses", "Brine rats", "Cave horrors", "Cave kraken", "Custodian stalkers",
			"Dagannoth", "Dark beasts", "Drakes", "Dust devils", "Elves", "Fire giants", "Fossil island wyverns", "Frost dragons",
			"Gargoyles", "Greater demons", "Gryphons", "Hellhounds", "Kalphites", "Kurask", "Lizardmen", "Metal dragons",
			"Minions of Scabaras", "Mutated zygomites", "Nechryael", "Red dragons", "Skeletal wyverns", "Smoke devils",
			"Spiritual creatures", "Suqahs", "Trolls", "Turoth", "TzHaar", "Vampyres", "Warped Creatures", "Wyrms");

		master("Duradel / Kuradal",
			"Aberrant spectres", "Abyssal demons", "Ankou", "Aquanites", "Araxytes", "Aviansies", "Basilisks", "Black demons",
			"Black dragons", "Bloodveld", "Blue dragons", "Bosses", "Cave horrors", "Cave kraken", "Dagannoth", "Dark beasts",
			"Drakes", "Dust devils", "Elves", "Fire giants", "Fossil island wyverns", "Frost dragons", "Gargoyles", "Greater demons",
			"Gryphons", "Hellhounds", "Kalphites", "Kurask", "Lizardmen", "Metal dragons", "Mutated zygomites", "Nechryael",
			"Red dragons", "Skeletal wyverns", "Smoke devils", "Spiritual creatures", "Suqahs", "Trolls", "TzHaar", "Vampyres",
			"Warped Creatures", "Waterfiends", "Wyrms");

		master("Krystilia",
			"Abyssal demons", "Ankou", "Aviansies", "Bandits", "Bears", "Black demons", "Black dragons", "Black knights", "Bloodveld",
			"Bosses", "Chaos druids", "Dark warriors", "Dust devils", "Earth warriors", "Ents", "Fire giants", "Greater demons",
			"Green dragons", "Hellhounds", "Hill giants", "Ice giants", "Ice warriors", "Jellies", "Lava Dragons", "Lesser demons",
			"Magic axes", "Mammoths", "Moss giants", "Nechryael", "Pirates", "Revenants", "Rogues", "Scorpions", "Skeletons",
			"Spiders", "Spiritual creatures", "Zombies");

		// Mortimer launched with Wyrmscraig on 29 July 2026. Unlike ordinary
		// masters, every offered task has a Mortifier and the player chooses
		// between two tasks (three after 50 Mortimer completions). Keep the live
		// level, weight and base-quantity table alongside the ordinary reverse
		// index so future balancing changes are auditable.
		detailedMaster("Mortimer",
			assignment("Crawling hands", 5, 10, 35, 50, false),
			assignment("Cave crawlers", 10, 10, 35, 50, false),
			assignment("Banshees", 15, 10, 35, 50, false),
			assignment("Rockslugs", 20, 10, 35, 50, false),
			assignment("Cockatrice", 25, 10, 35, 50, false),
			assignment("Pyrefiends", 30, 10, 35, 50, false),
			assignment("Infernal mages", 45, 10, 35, 50, false),
			assignment("Bloodveld", 50, 8, 120, 180, true),
			assignment("Gryphons", 51, 10, 80, 120, true),
			assignment("Jellies", 52, 10, 80, 120, false),
			assignment("Custodian stalkers", 54, 8, 80, 120, true),
			assignment("Turoth", 55, 10, 80, 120, false),
			assignment("Warped Creatures", 56, 10, 80, 120, false),
			assignment("Cave horrors", 58, 10, 80, 120, true),
			assignment("Aberrant spectres", 60, 10, 80, 120, true),
			assignment("Basilisks", 60, 10, 40, 60, true),
			assignment("Wyrms", 62, 10, 80, 120, true),
			assignment("Dust devils", 65, 8, 120, 180, true),
			assignment("Kurask", 70, 10, 40, 60, false),
			assignment("Venators", 74, 10, 120, 180, true),
			assignment("Gargoyles", 75, 10, 120, 180, true),
			assignment("Aquanites", 78, 10, 40, 60, true),
			assignment("Nechryael", 80, 8, 150, 200, true),
			assignment("Drakes", 84, 10, 40, 60, false),
			assignment("Abyssal demons", 85, 8, 120, 180, true),
			assignment("Dark beasts", 90, 10, 40, 60, true),
			assignment("Araxytes", 92, 8, 120, 180, true),
			assignment("Smoke devils", 93, 8, 80, 120, false),
			assignment("Hydras", 95, 10, 150, 200, false));
		MASTER_RULES.put("Mortimer", new MasterRules(
			"Wyrmscraig Caverns",
			"62 Sailing and access to the caverns during Fallen From Grace",
			70, 100, true, false,
			2, 3, 50, 15, 25, 40,
			100, 2, 120, false));
	}

	private SlayerMasterCatalog() { }

	private static void master(String name, String... tasks)
	{
		List<String> list = Collections.unmodifiableList(Arrays.asList(tasks));
		MASTER_TO_TASKS.put(name, list);
		for (String task : tasks)
		{
			index(name, task);
			String normalized = normalize(task);
			if ("bosses".equals(normalized))
			{
				index(name, "Boss");
			}
			else if ("metal dragons".equals(normalized))
			{
				for (String dragon : Arrays.asList("Bronze dragons", "Iron dragons", "Steel dragons",
					"Mithril dragons", "Adamant dragons", "Rune dragons"))
				{
					index(name, dragon);
				}
			}
			else if ("minions of scabaras".equals(normalized))
			{
				index(name, "Scabarites");
			}
		}
	}

	private static MasterAssignment assignment(
		String task, int slayerLevel, int weight, int minimum, int maximum, boolean extendable)
	{
		return new MasterAssignment(task, slayerLevel, weight, minimum, maximum, extendable);
	}

	private static void detailedMaster(String name, MasterAssignment... assignments)
	{
		List<MasterAssignment> details = Collections.unmodifiableList(Arrays.asList(assignments));
		MASTER_ASSIGNMENT_DETAILS.put(name, details);
		String[] tasks = new String[assignments.length];
		for (int i = 0; i < assignments.length; i++)
		{
			tasks[i] = assignments[i].getTask();
		}
		master(name, tasks);
	}

	private static void index(String master, String task)
	{
		TASK_TO_MASTERS.computeIfAbsent(normalize(task), ignored -> new LinkedHashSet<>()).add(master);
	}

	static List<String> mastersFor(String taskName)
	{
		Set<String> masters = TASK_TO_MASTERS.get(normalize(taskName));
		return masters == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(masters));
	}

	static Map<String, List<String>> allAssignments()
	{
		return Collections.unmodifiableMap(MASTER_TO_TASKS);
	}

	static List<MasterAssignment> detailedAssignmentsFor(String masterName)
	{
		List<MasterAssignment> assignments = MASTER_ASSIGNMENT_DETAILS.get(masterName);
		return assignments == null ? Collections.emptyList() : assignments;
	}

	static MasterRules rulesFor(String masterName)
	{
		return MASTER_RULES.get(masterName);
	}

	static boolean isKnownMasterTask(String taskName)
	{
		return TASK_TO_MASTERS.containsKey(normalize(taskName));
	}

	private static String normalize(String value)
	{
		if (value == null) return "";
		String normalized = value.toLowerCase(Locale.ENGLISH).trim();
		if (normalized.startsWith("the ")) normalized = normalized.substring(4);
		// Master tables sometimes use singular category labels while RuneLite's
		// Slayer task tracker uses plurals. Normalize the few live mismatches.
		normalized = normalized.replaceAll("[^a-z0-9]+", " ").trim();
		if ("boss".equals(normalized)) return "bosses";
		if ("waterfiend".equals(normalized)) return "waterfiends";
		if ("venator".equals(normalized)) return "venators";
		if ("cave slime".equals(normalized)) return "cave slimes";
		if ("monkey".equals(normalized)) return "monkeys";
		if ("avianise".equals(normalized) || "aviansie".equals(normalized)) return "aviansies";
		if ("minions of scabaras".equals(normalized)) return "scabarites";
		return normalized;
	}

	static final class MasterAssignment
	{
		private final String task;
		private final int slayerLevel;
		private final int weight;
		private final int minimum;
		private final int maximum;
		private final boolean extendable;

		private MasterAssignment(
			String task, int slayerLevel, int weight, int minimum, int maximum, boolean extendable)
		{
			this.task = task;
			this.slayerLevel = slayerLevel;
			this.weight = weight;
			this.minimum = minimum;
			this.maximum = maximum;
			this.extendable = extendable;
		}

		String getTask() { return task; }
		int getSlayerLevel() { return slayerLevel; }
		int getWeight() { return weight; }
		int getMinimum() { return minimum; }
		int getMaximum() { return maximum; }
		boolean isExtendable() { return extendable; }
	}

	static final class MasterRules
	{
		private final String location;
		private final String accessRequirement;
		private final int minimumSlayer;
		private final int minimumCombat;
		private final boolean slayerCapeBypass;
		private final boolean awardsBasePoints;
		private final int initialChoices;
		private final int unlockedChoices;
		private final int choicesUnlockAt;
		private final int clueModifierUnlockAt;
		private final int superiorUniqueModifierUnlockAt;
		private final int xpModifierUnlockAt;
		private final int cancelCost;
		private final int blockSlots;
		private final int blockCost;
		private final boolean turaelResetAllowed;

		private MasterRules(String location, String accessRequirement,
			int minimumSlayer, int minimumCombat, boolean slayerCapeBypass,
			boolean awardsBasePoints, int initialChoices, int unlockedChoices,
			int choicesUnlockAt, int clueModifierUnlockAt,
			int superiorUniqueModifierUnlockAt, int xpModifierUnlockAt,
			int cancelCost, int blockSlots, int blockCost, boolean turaelResetAllowed)
		{
			this.location = location;
			this.accessRequirement = accessRequirement;
			this.minimumSlayer = minimumSlayer;
			this.minimumCombat = minimumCombat;
			this.slayerCapeBypass = slayerCapeBypass;
			this.awardsBasePoints = awardsBasePoints;
			this.initialChoices = initialChoices;
			this.unlockedChoices = unlockedChoices;
			this.choicesUnlockAt = choicesUnlockAt;
			this.clueModifierUnlockAt = clueModifierUnlockAt;
			this.superiorUniqueModifierUnlockAt = superiorUniqueModifierUnlockAt;
			this.xpModifierUnlockAt = xpModifierUnlockAt;
			this.cancelCost = cancelCost;
			this.blockSlots = blockSlots;
			this.blockCost = blockCost;
			this.turaelResetAllowed = turaelResetAllowed;
		}

		String getLocation() { return location; }
		String getAccessRequirement() { return accessRequirement; }
		int getMinimumSlayer() { return minimumSlayer; }
		int getMinimumCombat() { return minimumCombat; }
		boolean isSlayerCapeBypass() { return slayerCapeBypass; }
		boolean isAwardsBasePoints() { return awardsBasePoints; }
		int getInitialChoices() { return initialChoices; }
		int getUnlockedChoices() { return unlockedChoices; }
		int getChoicesUnlockAt() { return choicesUnlockAt; }
		int getClueModifierUnlockAt() { return clueModifierUnlockAt; }
		int getSuperiorUniqueModifierUnlockAt() { return superiorUniqueModifierUnlockAt; }
		int getXpModifierUnlockAt() { return xpModifierUnlockAt; }
		int getCancelCost() { return cancelCost; }
		int getBlockSlots() { return blockSlots; }
		int getBlockCost() { return blockCost; }
		boolean isTuraelResetAllowed() { return turaelResetAllowed; }
	}
}
