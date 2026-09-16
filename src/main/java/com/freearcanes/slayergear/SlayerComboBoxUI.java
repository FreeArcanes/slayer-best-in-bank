package com.freearcanes.slayergear;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import net.runelite.client.ui.FontManager;

/**
 * Plugin-owned combo box presentation which stays consistent across host
 * operating systems and RuneLite look-and-feel choices.
 */
final class SlayerComboBoxUI extends BasicComboBoxUI
{
	private static final int ARROW_WIDTH = 25;
	private final PanelTheme theme;

	private SlayerComboBoxUI(PanelTheme theme)
	{
		this.theme = theme;
	}

	static void installOn(JComboBox<String> comboBox, PanelTheme theme)
	{
		PanelTheme selected = theme == null ? PanelTheme.RUNELITE : theme;
		comboBox.setUI(new SlayerComboBoxUI(selected));
		comboBox.setRenderer(new ThemeListCellRenderer(selected));
		comboBox.setBackground(selected.row);
		comboBox.setForeground(selected.text);
		comboBox.setBorder(BorderFactory.createLineBorder(selected.border));
		comboBox.setOpaque(true);
	}

	@Override
	protected JButton createArrowButton()
	{
		JButton button = new ArrowButton(theme);
		button.setName("ComboBox.arrowButton");
		return button;
	}

	@Override
	protected ComboPopup createPopup()
	{
		return new ThemeComboPopup(comboBox, theme);
	}

	@Override
	protected Insets getInsets()
	{
		return new Insets(0, 0, 0, 0);
	}

	private static final class ThemeComboPopup extends BasicComboPopup
	{
		private ThemeComboPopup(JComboBox<Object> comboBox, PanelTheme theme)
		{
			super(comboBox);
			setBorder(BorderFactory.createLineBorder(theme.border));
			setBackground(theme.surface);
			list.setBackground(theme.surface);
			list.setForeground(theme.text);
			list.setSelectionBackground(theme.rowHover);
			list.setSelectionForeground(theme.gold);
			list.setFixedCellHeight(24);
			list.setFont(FontManager.getRunescapeSmallFont());
			scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
			scroller.getViewport().setBackground(theme.surface);
			JScrollBar scrollBar = scroller.getVerticalScrollBar();
			scrollBar.setUI(new ThemeScrollBarUI(theme));
			scrollBar.setPreferredSize(new Dimension(7, 0));
			scrollBar.setOpaque(false);
		}
	}

	private static final class ThemeScrollBarUI extends BasicScrollBarUI
	{
		private final PanelTheme theme;

		private ThemeScrollBarUI(PanelTheme theme)
		{
			this.theme = theme;
		}

		@Override
		protected void paintTrack(Graphics graphics, JComponent component, Rectangle bounds)
		{
			// The popup surface itself is the track.
		}

		@Override
		protected void paintThumb(Graphics graphics, JComponent component, Rectangle bounds)
		{
			if (bounds.isEmpty() || !scrollbar.isEnabled())
			{
				return;
			}
			Graphics2D g = (Graphics2D) graphics.create();
			try
			{
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(isDragging ? theme.activeScrollThumb : theme.scrollThumb);
				g.fillRoundRect(bounds.x + 1, bounds.y + 1,
					Math.max(3, bounds.width - 2), Math.max(3, bounds.height - 2), 5, 5);
			}
			finally
			{
				g.dispose();
			}
		}

		@Override
		protected JButton createDecreaseButton(int orientation)
		{
			return zeroSizeButton();
		}

		@Override
		protected JButton createIncreaseButton(int orientation)
		{
			return zeroSizeButton();
		}

		private static JButton zeroSizeButton()
		{
			JButton button = new JButton();
			Dimension zero = new Dimension(0, 0);
			button.setPreferredSize(zero);
			button.setMinimumSize(zero);
			button.setMaximumSize(zero);
			return button;
		}
	}

	private static final class ThemeListCellRenderer extends JLabel
		implements ListCellRenderer<String>
	{
		private final PanelTheme theme;

		private ThemeListCellRenderer(PanelTheme theme)
		{
			this.theme = theme;
			setOpaque(true);
			setFont(FontManager.getRunescapeSmallFont());
			setBorder(new EmptyBorder(3, 7, 3, 7));
			setVerticalAlignment(SwingConstants.CENTER);
		}

		@Override
		public Component getListCellRendererComponent(
			JList<? extends String> list,
			String value,
			int index,
			boolean selected,
			boolean hasFocus)
		{
			setText(value == null ? "" : value);
			setBackground(selected ? theme.rowHover : (index < 0 ? theme.row : theme.surface));
			setForeground(selected ? theme.gold : theme.text);
			return this;
		}
	}

	private static final class ArrowButton extends JButton
	{
		private final PanelTheme theme;

		private ArrowButton(PanelTheme theme)
		{
			this.theme = theme;
			setBorder(new EmptyBorder(0, 0, 0, 0));
			setContentAreaFilled(false);
			setFocusPainted(false);
			setFocusable(false);
			setRolloverEnabled(true);
			setPreferredSize(new Dimension(ARROW_WIDTH, 24));
			setMinimumSize(new Dimension(ARROW_WIDTH, 16));
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			try
			{
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
				Color background = getModel().isPressed() || getModel().isRollover()
					? theme.rowHover : theme.raisedSurface;
				g.setColor(background);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(theme.border);
				g.drawLine(0, 0, 0, getHeight());

				int centerX = getWidth() / 2;
				int centerY = getHeight() / 2 + 1;
				g.setColor(getModel().isRollover() ? theme.gold : theme.softText);
				int[] x = {centerX - 4, centerX + 4, centerX};
				int[] y = {centerY - 2, centerY - 2, centerY + 3};
				g.fillPolygon(x, y, 3);
			}
			finally
			{
				g.dispose();
			}
		}
	}
}
