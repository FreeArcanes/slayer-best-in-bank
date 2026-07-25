package com.freearcanes.slayergear;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SlayerGearAdvisorPluginLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SlayerGearAdvisorPlugin.class);
		RuneLite.main(args);
	}
}
