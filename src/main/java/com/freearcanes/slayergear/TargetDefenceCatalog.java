package com.freearcanes.slayergear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Resolves Slayer assignments to the maintained Wiki DPS monster dataset. */
final class TargetDefenceCatalog
{
	private static final String RESOURCE = "/com/freearcanes/slayergear/slayer-targets.tsv";
	private static final Map<String, List<TargetDefence>> BY_NAME = load();
	private static final Map<String, List<String>> FAMILIES = families();

	private TargetDefenceCatalog() {}

	static List<TargetDefence> find(String taskName, String profileKey)
	{
		return find(taskName, profileKey, null);
	}

	static List<TargetDefence> find(String taskName, String profileKey, GearStrategy strategy)
	{
		List<TargetDefence> strategyTarget = strategyTarget(strategy);
		if (!strategyTarget.isEmpty()) return strategyTarget;
		String task = normalize(taskName);
		List<String> family = FAMILIES.get(task);
		if (family != null)
		{
			List<TargetDefence> result = new ArrayList<>();
			for (String member : family) result.addAll(BY_NAME.getOrDefault(member, Collections.emptyList()));
			return distinct(result);
		}
		List<String> candidates = new ArrayList<>();
		candidates.addAll(nameCandidates(task));
		candidates.addAll(nameCandidates(normalize(profileKey)));
		for (String candidate : candidates)
		{
			List<TargetDefence> targets = BY_NAME.get(candidate);
			if (targets != null && !targets.isEmpty()) return distinct(targets);
		}
		return Collections.emptyList();
	}

	private static List<TargetDefence> strategyTarget(GearStrategy strategy)
	{
		if (strategy == null) return Collections.emptyList();
		String strategyName = normalize(strategy.getName());
		String longestMatch = "";
		for (String monsterName : BY_NAME.keySet())
		{
			if (monsterName.length() > longestMatch.length()
				&& monsterName.length() >= 5
				&& (strategyName.equals(monsterName)
					|| strategyName.startsWith(monsterName + " ")
					|| strategyName.endsWith(" " + monsterName)))
			{
				longestMatch = monsterName;
			}
		}
		return longestMatch.isEmpty() ? Collections.emptyList()
			: distinct(BY_NAME.get(longestMatch));
	}

	private static List<TargetDefence> distinct(List<TargetDefence> values)
	{
		Map<String, TargetDefence> unique = new LinkedHashMap<>();
		for (TargetDefence target : values) unique.putIfAbsent(target.signature(), target);
		return Collections.unmodifiableList(new ArrayList<>(unique.values()));
	}

	private static Map<String, List<TargetDefence>> load()
	{
		Map<String, List<TargetDefence>> result = new HashMap<>();
		try (InputStream stream = TargetDefenceCatalog.class.getResourceAsStream(RESOURCE))
		{
			if (stream == null) return Collections.emptyMap();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					if (!line.isEmpty() && line.charAt(0) == '\uFEFF') line = line.substring(1);
					if (line.isEmpty() || line.charAt(0) == '#' || line.startsWith("name\t")) continue;
					String[] field = line.split("\t", -1);
					if (field.length != 16) continue;
					TargetDefence target = new TargetDefence(field[0], integer(field[2]), integer(field[3]),
						integer(field[4]), integer(field[5]), integer(field[6]), integer(field[7]),
						integer(field[8]), integer(field[9]), integer(field[10]), integer(field[11]),
						integer(field[12]), integer(field[13]), field[14], field[15]);
					result.computeIfAbsent(normalize(field[0]), ignored -> new ArrayList<>()).add(target);
				}
			}
		}
		catch (IOException ignored)
		{
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(result);
	}

	private static Map<String, List<String>> families()
	{
		Map<String, List<String>> result = new HashMap<>();
		result.put("fossil island wyverns", List.of("ancient wyvern", "spitting wyvern", "taloned wyvern", "long tailed wyvern"));
		result.put("dagannoth", List.of("dagannoth"));
		result.put("dagannoths", List.of("dagannoth"));
		result.put("elves", List.of("elf archer", "elf warrior"));
		return Collections.unmodifiableMap(result);
	}

	private static int integer(String value)
	{
		try { return Integer.parseInt(value); }
		catch (NumberFormatException ignored) { return 0; }
	}

	private static List<String> nameCandidates(String value)
	{
		List<String> result = new ArrayList<>();
		result.add(value);
		if (value.endsWith("s") && !value.endsWith("ss"))
		{
			result.add(value.substring(0, value.length() - 1));
		}
		if (value.endsWith("ies"))
		{
			result.add(value.substring(0, value.length() - 3) + "y");
		}
		if (value.endsWith("ves"))
		{
			result.add(value.substring(0, value.length() - 3) + "f");
		}
		if (value.endsWith("ses"))
		{
			result.add(value.substring(0, value.length() - 2));
		}
		return result;
	}

	private static String normalize(String value)
	{
		return value == null ? "" : value.toLowerCase(Locale.ENGLISH)
			.replaceAll("[^a-z0-9]+", " ").trim();
	}
}
