package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Shared ordered alias catalog used by task, route and combat definitions.
 * Registration policy is explicit so broad fallbacks cannot silently replace
 * a curated task, while later combat definitions can intentionally refine a
 * generic classification.
 */
final class AliasCatalog<T>
{
	enum CollisionPolicy
	{
		KEEP_FIRST,
		REPLACE
	}

	private final Function<String, String> normalizer;
	private final Map<String, T> entries = new LinkedHashMap<>();
	private final Map<String, List<T>> ignoredCollisions = new LinkedHashMap<>();

	AliasCatalog(Function<String, String> normalizer)
	{
		this.normalizer = normalizer;
	}

	void register(T value, CollisionPolicy policy, String... aliases)
	{
		if (value == null || aliases == null) return;
		for (String alias : aliases)
		{
			String key = normalizer.apply(alias == null ? "" : alias);
			if (key == null || key.trim().isEmpty())
			{
				throw new IllegalArgumentException("Catalog aliases must not be blank");
			}

			T existing = entries.get(key);
			if (existing == null || existing == value || policy == CollisionPolicy.REPLACE)
			{
				entries.put(key, value);
			}
			else
			{
				ignoredCollisions.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
			}
		}
	}

	T get(String alias)
	{
		if (alias == null) return null;
		return entries.get(normalizer.apply(alias));
	}

	boolean contains(String alias)
	{
		return get(alias) != null;
	}

	int distinctValueCount()
	{
		return (int) entries.values().stream().distinct().count();
	}

	Map<String, T> snapshot()
	{
		return Collections.unmodifiableMap(new LinkedHashMap<>(entries));
	}

	Map<String, List<T>> ignoredCollisionsSnapshot()
	{
		Map<String, List<T>> copy = new LinkedHashMap<>();
		for (Map.Entry<String, List<T>> entry : ignoredCollisions.entrySet())
		{
			copy.put(entry.getKey(),
				Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
		}
		return Collections.unmodifiableMap(copy);
	}
}
