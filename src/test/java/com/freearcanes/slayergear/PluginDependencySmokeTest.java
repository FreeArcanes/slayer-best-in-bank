package com.freearcanes.slayergear;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.plugins.banktags.BankTagsConfig;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.plugins.banktags.tabs.LayoutManager;
import net.runelite.client.plugins.banktags.tabs.TabInterface;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginDependencySmokeTest
{
	@Test
	public void declaredDependenciesProvideRequiredBindings()
		throws ReflectiveOperationException
	{
		PluginDependency[] dependencies =
			SlayerGearAdvisorPlugin.class.getAnnotationsByType(PluginDependency.class);
		List<Plugin> dependencyInstances = instantiateDependencies(dependencies);
		ConfigManager configManager = mock(ConfigManager.class);
		SlayerGearAdvisorConfig config = mock(SlayerGearAdvisorConfig.class);
		when(configManager.getConfig(eq(SlayerGearAdvisorConfig.class)))
			.thenReturn(config);
		when(configManager.getConfig(eq(BankTagsConfig.class)))
			.thenReturn(mock(BankTagsConfig.class));

		Module testBindings = binder ->
		{
			binder.bind(Client.class).toInstance(mock(Client.class));
			binder.bind(ClientThread.class).toInstance(mock(ClientThread.class));
			binder.bind(ConfigManager.class).toInstance(configManager);
			binder.bind(ItemManager.class).toInstance(mock(ItemManager.class));
			binder.bind(SpriteManager.class).toInstance(mock(SpriteManager.class));
			binder.bind(ChatboxPanelManager.class)
				.toInstance(mock(ChatboxPanelManager.class));
			binder.bind(EventBus.class).toInstance(mock(EventBus.class));
			binder.bind(GearScorer.class).toInstance(mock(GearScorer.class));
			binder.bind(InventoryCapacityPlanner.class)
				.toInstance(mock(InventoryCapacityPlanner.class));
			binder.bind(SlayerGearPanel.class).toInstance(mock(SlayerGearPanel.class));
			binder.bind(BankRecommendationOverlay.class)
				.toInstance(mock(BankRecommendationOverlay.class));
			binder.bind(TaskPrepReminderOverlay.class)
				.toInstance(mock(TaskPrepReminderOverlay.class));
			binder.bind(BankAdvisorButton.class).toInstance(mock(BankAdvisorButton.class));
			binder.bind(TieredBankLayout.class).toInstance(mock(TieredBankLayout.class));
			binder.bind(BankSearch.class).toInstance(mock(BankSearch.class));
			binder.bind(TagManager.class).toInstance(mock(TagManager.class));
			binder.bind(TabInterface.class).toInstance(mock(TabInterface.class));
			binder.bind(LayoutManager.class).toInstance(mock(LayoutManager.class));
			binder.bind(OverlayManager.class).toInstance(mock(OverlayManager.class));
			binder.bind(ClientToolbar.class).toInstance(mock(ClientToolbar.class));
			binder.bind(Boolean.class)
				.annotatedWith(Names.named("developerMode"))
				.toInstance(false);
			for (Plugin dependency : dependencyInstances)
			{
				@SuppressWarnings("unchecked")
				Class<Plugin> dependencyClass =
					(Class<Plugin>) dependency.getClass();
				binder.bind(dependencyClass).toInstance(dependency);
				binder.install(dependency);
			}
		};

		Injector parent = Guice.createInjector(testBindings);
		SlayerGearAdvisorPlugin plugin = new SlayerGearAdvisorPlugin();
		Injector pluginInjector = parent.createChildInjector(binder ->
		{
			binder.bind(SlayerGearAdvisorPlugin.class).toInstance(plugin);
			binder.install(plugin);
		});

		assertSame(plugin, pluginInjector.getInstance(SlayerGearAdvisorPlugin.class));
		assertTrue(Arrays.stream(dependencies)
			.anyMatch(dependency -> dependency.value() == BankTagsPlugin.class));
	}

	private static List<Plugin> instantiateDependencies(
		PluginDependency[] dependencies)
		throws ReflectiveOperationException
	{
		List<Plugin> instances = new ArrayList<>();
		for (PluginDependency dependency : dependencies)
		{
			try
			{
				instances.add(dependency.value().getDeclaredConstructor().newInstance());
			}
			catch (InvocationTargetException exception)
			{
				Throwable cause = exception.getCause();
				if (cause instanceof ReflectiveOperationException)
				{
					throw (ReflectiveOperationException) cause;
				}
				throw exception;
			}
		}
		return instances;
	}
}
