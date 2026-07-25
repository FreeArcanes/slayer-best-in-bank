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
		if ("cave slime".equals(normalized)) return "cave slimes";
		if ("monkey".equals(normalized)) return "monkeys";
		if ("avianise".equals(normalized) || "aviansie".equals(normalized)) return "aviansies";
		if ("minions of scabaras".equals(normalized)) return "scabarites";
		return normalized;
	}
}
