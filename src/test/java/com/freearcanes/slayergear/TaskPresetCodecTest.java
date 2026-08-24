package com.freearcanes.slayergear;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaskPresetCodecTest
{
	@Test
	public void roundTripsPunctuationAndUnicodeWithoutDelimitersLeaking()
	{
		Map<String, String> input = new LinkedHashMap<>();
		input.put("objective", "VALUE");
		input.put("pinned", "Karil's leathertop, Dizana’s quiver (l)");
		input.put("excluded", "item=a;item=b");

		assertEquals(input, TaskPresetCodec.decode(TaskPresetCodec.encode(input)));
	}

	@Test
	public void rejectsWrongVersionsMalformedBase64AndOversizedTokens()
	{
		assertTrue(TaskPresetCodec.decode("SBIB2;x=eA;").isEmpty());
		assertTrue(TaskPresetCodec.decode("SBIB1;x=***;").isEmpty());
		assertTrue(TaskPresetCodec.decode("SBIB1;missing-separator;").isEmpty());
		assertTrue(TaskPresetCodec.decode("SBIB1;x=" + "a".repeat(8200)).isEmpty());
	}
}
