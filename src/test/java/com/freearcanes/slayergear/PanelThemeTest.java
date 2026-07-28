package com.freearcanes.slayergear;

import java.awt.Color;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PanelThemeTest
{
	@Test
	public void exposesExpectedThemeChoices()
	{
		Set<String> labels = Arrays.stream(PanelTheme.values())
			.map(PanelTheme::toString)
			.collect(Collectors.toSet());

		assertEquals(new HashSet<>(Arrays.asList(
			"Midnight",
			"RuneLite",
			"Old School",
			"Classic Dark",
			"High Contrast")), labels);
	}

	@Test
	public void everyThemeProvidesACompleteDistinctPalette()
	{
		Set<Color> backgrounds = new HashSet<>();
		for (PanelTheme theme : PanelTheme.values())
		{
			assertNotNull(theme.panelBackground);
			assertNotNull(theme.surface);
			assertNotNull(theme.border);
			assertNotNull(theme.text);
			assertNotNull(theme.success);
			assertNotNull(theme.warning);
			assertNotNull(theme.danger);
			assertNotEquals(theme.panelBackground, theme.text);
			assertTrue(theme.cardRadius > 0);
			assertTrue(theme.rowRadius > 0);
			backgrounds.add(theme.panelBackground);
		}

		assertEquals(PanelTheme.values().length, backgrounds.size());
	}

	@Test
	public void midnightRemainsTheDefault()
	{
		SlayerGearAdvisorConfig config = new SlayerGearAdvisorConfig() { };
		assertEquals(PanelTheme.MIDNIGHT, config.panelTheme());
	}

	@Test
	public void configEnumTypesAreAccessibleToJdkProxies()
	{
		for (Method method : SlayerGearAdvisorConfig.class.getMethods())
		{
			Class<?> returnType = method.getReturnType();
			if (returnType.isEnum())
			{
				assertTrue(
					returnType.getName() + " must be public",
					Modifier.isPublic(returnType.getModifiers()));
			}
		}
	}
}
