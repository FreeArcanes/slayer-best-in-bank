package com.freearcanes.slayergear;

import java.awt.image.BufferedImage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ToolbarIconTest
{
	@Test
	public void trimsTransparentInventoryPadding()
	{
		BufferedImage source = new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB);
		for (int y = 10; y <= 19; y++)
		{
			for (int x = 12; x <= 23; x++)
			{
				source.setRGB(x, y, 0xFFFFFFFF);
			}
		}

		BufferedImage result = SlayerGearAdvisorPlugin.toolbarIcon(source);

		assertEquals(14, result.getWidth());
		assertEquals(12, result.getHeight());
	}

	@Test
	public void leavesBlankImageUsable()
	{
		BufferedImage source = new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB);
		assertSame(source, SlayerGearAdvisorPlugin.toolbarIcon(source));
	}
}
