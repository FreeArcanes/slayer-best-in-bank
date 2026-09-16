package com.freearcanes.slayergear;

import java.lang.reflect.Method;
import net.runelite.client.config.ConfigItem;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class SlayerGearAdvisorConfigTest
{
	@Test
	public void araxxorSwitchPreferenceRemainsVisibleInSettings() throws Exception
	{
		Method method = SlayerGearAdvisorConfig.class.getMethod("araxxorSwitchPreference");
		ConfigItem item = method.getAnnotation(ConfigItem.class);

		assertNotNull(item);
		assertFalse(item.hidden());
	}
}
