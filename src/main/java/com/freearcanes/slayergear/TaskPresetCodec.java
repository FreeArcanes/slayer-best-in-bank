package com.freearcanes.slayergear;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class TaskPresetCodec
{
	private static final String PREFIX = "SBIB1;";
	private static final int MAX_TOKEN_LENGTH = 8192;

	private TaskPresetCodec() {}

	static String encode(Map<String, String> values)
	{
		StringBuilder token = new StringBuilder(PREFIX);
		if (values != null)
		{
			for (Map.Entry<String, String> entry : values.entrySet())
			{
				if (!validKey(entry.getKey())) continue;
				String value = entry.getValue() == null ? "" : entry.getValue();
				if (value.length() > 1000) value = value.substring(0, 1000);
				token.append(entry.getKey()).append('=')
					.append(Base64.getUrlEncoder().withoutPadding().encodeToString(
						value.getBytes(StandardCharsets.UTF_8))).append(';');
			}
		}
		return token.toString();
	}

	static Map<String, String> decode(String token)
	{
		if (token == null || !token.startsWith(PREFIX)
			|| token.length() > MAX_TOKEN_LENGTH) return Collections.emptyMap();
		Map<String, String> values = new LinkedHashMap<>();
		try
		{
			for (String part : token.substring(PREFIX.length()).split(";"))
			{
				if (part.isEmpty()) continue;
				int separator = part.indexOf('=');
				if (separator <= 0) return Collections.emptyMap();
				String key = part.substring(0, separator);
				if (!validKey(key)) return Collections.emptyMap();
				String value = new String(Base64.getUrlDecoder().decode(
					part.substring(separator + 1)), StandardCharsets.UTF_8);
				if (value.length() > 1000) return Collections.emptyMap();
				values.put(key, value);
			}
		}
		catch (IllegalArgumentException exception)
		{
			return Collections.emptyMap();
		}
		return values;
	}

	private static boolean validKey(String key)
	{
		return key != null && key.matches("[a-zA-Z][a-zA-Z0-9]{0,39}");
	}
}
