package com.freearcanes.slayergear;

import java.util.Locale;

final class NameMatcher
{
	private NameMatcher() {}

	static String normalize(String value)
	{
		return value == null ? "" : value.trim().toLowerCase(Locale.ENGLISH);
	}

	static boolean matchesAnyToken(String name, String expression)
	{
		if (expression == null || expression.trim().isEmpty())
		{
			return true;
		}
		String normalized = normalize(name);
		for (String token : expression.toLowerCase(Locale.ENGLISH).split("\\|"))
		{
			String trimmed = token.trim();
			if (!trimmed.isEmpty() && normalized.contains(trimmed))
			{
				return true;
			}
		}
		return false;
	}

	static boolean containsAny(String value, String... tokens)
	{
		String normalized = normalize(value);
		for (String token : tokens)
		{
			if (normalized.contains(normalize(token)))
			{
				return true;
			}
		}
		return false;
	}
}
