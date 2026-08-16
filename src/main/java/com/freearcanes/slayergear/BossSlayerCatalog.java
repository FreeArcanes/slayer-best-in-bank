package com.freearcanes.slayergear;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BossSlayerCatalog
{
	/*
	 * Names shown by the manual Boss Slayer selector. Encounter-specific gear
	 * rules live with scoring/profile data; this list is presentation only.
	 */
	private static final List<String> BOSSES = Collections.unmodifiableList(Arrays.asList(
		"Abyssal Sire", "Alchemical Hydra", "Amoxliatl", "Araxxor", "Barrows Brothers",
		"Branda the Fire Queen", "Brutus", "Bryophyta", "Callisto", "Cerberus",
		"Chaos Elemental", "Chaos Fanatic", "Commander Zilyana", "Crazy Archaeologist",
		"Dagannoth Kings", "Demonic Gorillas", "Deranged Archaeologist", "Duke Sucellus",
		"Eldric the Ice King", "General Graardor", "Giant Mole", "Grotesque Guardians",
		"K'ril Tsutsaroth", "Kalphite Queen", "King Black Dragon", "Kraken", "Kree'arra",
		"Maggot King", "Obor", "Phantom Muspah", "Sarachnis", "Scorpia", "Scurrius",
		"Shellbane Gryphon", "Skotizo", "The Leviathan", "The Whisperer",
		"Thermonuclear Smoke Devil", "Tormented Demons", "TzKal-Zuk", "TzTok-Jad",
		"Vardorvis", "Venenatis", "Vet'ion", "Vorkath", "Zulrah"));
	private static final Map<String, List<String>> STANDARD_TASK_BOSSES = buildTaskBosses();

	private BossSlayerCatalog() { }

	static List<String> names() { return BOSSES; }

	static List<String> forTask(String taskName)
	{
		List<String> bosses = STANDARD_TASK_BOSSES.get(normalizeTask(taskName));
		return bosses == null ? Collections.emptyList() : bosses;
	}

	static boolean contains(String value)
	{
		String normalized = NameMatcher.normalize(value);
		for (String boss : BOSSES)
		{
			String candidate = NameMatcher.normalize(boss);
			if (normalized.equals(candidate) || normalized.equals("the " + candidate)) return true;
		}
		return false;
	}

	private static Map<String, List<String>> buildTaskBosses()
	{
		Map<String, List<String>> map = new LinkedHashMap<>();
		map(map, "abyssal demon", "Abyssal Sire");
		map(map, "hydra", "Alchemical Hydra");
		map(map, "lesser nagua", "Amoxliatl");
		map(map, "araxyte", "Araxxor");
		map(map, "spider", "Araxxor", "Sarachnis", "Venenatis");
		map(map, "fire giant", "Branda the Fire Queen");
		map(map, "cow", "Brutus");
		map(map, "moss giant", "Bryophyta");
		map(map, "bear", "Callisto");
		map(map, "hellhound", "Cerberus");
		map(map, "dagannoth", "Dagannoth Kings");
		map(map, "black demon", "Demonic Gorillas", "Skotizo");
		map(map, "monkey", "Demonic Gorillas");
		map(map, "ice giant", "Eldric the Ice King");
		map(map, "gargoyle", "Grotesque Guardians");
		map(map, "greater demon", "K'ril Tsutsaroth", "Skotizo", "Tormented Demons");
		map(map, "kalphite", "Kalphite Queen");
		map(map, "black dragon", "King Black Dragon");
		map(map, "cave kraken", "Kraken");
		map(map, "aviansie", "Kree'arra");
		map(map, "hill giant", "Obor");
		map(map, "scorpion", "Scorpia");
		map(map, "rat", "Scurrius");
		map(map, "gryphon", "Shellbane Gryphon");
		map(map, "smoke devil", "Thermonuclear Smoke Devil");
		map(map, "tzhaar", "TzTok-Jad", "TzKal-Zuk");
		map(map, "skeleton", "Vet'ion");
		map(map, "blue dragon", "Vorkath");
		map(map, "zombie", "Vorkath");
		return Collections.unmodifiableMap(map);
	}

	private static void map(Map<String, List<String>> map, String task, String... bosses)
	{
		List<String> values = Collections.unmodifiableList(Arrays.asList(bosses));
		map.put(task, values);
		map.put(task + "s", values);
	}

	private static String normalizeTask(String taskName)
	{
		String normalized = NameMatcher.normalize(taskName);
		return normalized.startsWith("the ") ? normalized.substring(4) : normalized;
	}
}
