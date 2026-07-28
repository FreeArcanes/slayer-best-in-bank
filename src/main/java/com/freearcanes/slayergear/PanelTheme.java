package com.freearcanes.slayergear;

import java.awt.Color;

public enum PanelTheme
{
	MIDNIGHT(
		"Midnight",
		new Color(25, 26, 29),
		new Color(31, 33, 37),
		new Color(36, 38, 43),
		new Color(34, 36, 40),
		new Color(41, 44, 49),
		new Color(50, 53, 59),
		new Color(238, 239, 241),
		new Color(193, 196, 201),
		new Color(132, 136, 144),
		new Color(96, 100, 108),
		new Color(239, 181, 74),
		new Color(72, 201, 190),
		new Color(100, 157, 235),
		new Color(97, 194, 123),
		new Color(231, 166, 74),
		new Color(220, 103, 93),
		new Color(78, 81, 89),
		new Color(118, 122, 132),
		12,
		9),
	RUNELITE(
		"RuneLite",
		new Color(30, 30, 30),
		new Color(37, 37, 37),
		new Color(45, 45, 45),
		new Color(41, 41, 41),
		new Color(51, 51, 51),
		new Color(62, 62, 62),
		new Color(221, 221, 221),
		new Color(190, 190, 190),
		new Color(145, 145, 145),
		new Color(103, 103, 103),
		new Color(255, 152, 31),
		new Color(74, 190, 183),
		new Color(90, 150, 220),
		new Color(90, 185, 100),
		new Color(255, 152, 31),
		new Color(220, 80, 70),
		new Color(82, 82, 82),
		new Color(118, 118, 118),
		8,
		6),
	OLD_SCHOOL(
		"Old School",
		new Color(31, 25, 18),
		new Color(43, 34, 23),
		new Color(54, 43, 28),
		new Color(49, 39, 26),
		new Color(62, 49, 31),
		new Color(104, 80, 43),
		new Color(237, 220, 174),
		new Color(207, 187, 137),
		new Color(159, 137, 93),
		new Color(112, 93, 59),
		new Color(255, 184, 54),
		new Color(93, 185, 154),
		new Color(111, 155, 199),
		new Color(90, 200, 78),
		new Color(235, 154, 52),
		new Color(211, 74, 62),
		new Color(113, 88, 48),
		new Color(151, 117, 62),
		4,
		3),
	CLASSIC_DARK(
		"Classic Dark",
		new Color(18, 18, 18),
		new Color(25, 25, 25),
		new Color(32, 32, 32),
		new Color(29, 29, 29),
		new Color(39, 39, 39),
		new Color(58, 58, 58),
		new Color(232, 232, 232),
		new Color(191, 191, 191),
		new Color(130, 130, 130),
		new Color(88, 88, 88),
		new Color(214, 176, 79),
		new Color(86, 177, 168),
		new Color(103, 145, 197),
		new Color(91, 181, 105),
		new Color(211, 151, 70),
		new Color(205, 87, 78),
		new Color(73, 73, 73),
		new Color(105, 105, 105),
		7,
		5),
	HIGH_CONTRAST(
		"High Contrast",
		new Color(8, 8, 10),
		new Color(17, 17, 20),
		new Color(29, 29, 34),
		new Color(23, 23, 27),
		new Color(40, 40, 47),
		new Color(105, 105, 116),
		Color.WHITE,
		new Color(224, 224, 229),
		new Color(174, 174, 184),
		new Color(126, 126, 138),
		new Color(255, 203, 61),
		new Color(72, 224, 211),
		new Color(103, 174, 255),
		new Color(90, 230, 112),
		new Color(255, 184, 61),
		new Color(255, 91, 83),
		new Color(116, 116, 130),
		new Color(166, 166, 181),
		9,
		7);

	private final String label;
	final Color panelBackground;
	final Color surface;
	final Color raisedSurface;
	final Color row;
	final Color rowHover;
	final Color border;
	final Color text;
	final Color softText;
	final Color mutedText;
	final Color faintText;
	final Color gold;
	final Color teal;
	final Color blue;
	final Color success;
	final Color warning;
	final Color danger;
	final Color scrollThumb;
	final Color activeScrollThumb;
	final int cardRadius;
	final int rowRadius;

	PanelTheme(
		String label,
		Color panelBackground,
		Color surface,
		Color raisedSurface,
		Color row,
		Color rowHover,
		Color border,
		Color text,
		Color softText,
		Color mutedText,
		Color faintText,
		Color gold,
		Color teal,
		Color blue,
		Color success,
		Color warning,
		Color danger,
		Color scrollThumb,
		Color activeScrollThumb,
		int cardRadius,
		int rowRadius)
	{
		this.label = label;
		this.panelBackground = panelBackground;
		this.surface = surface;
		this.raisedSurface = raisedSurface;
		this.row = row;
		this.rowHover = rowHover;
		this.border = border;
		this.text = text;
		this.softText = softText;
		this.mutedText = mutedText;
		this.faintText = faintText;
		this.gold = gold;
		this.teal = teal;
		this.blue = blue;
		this.success = success;
		this.warning = warning;
		this.danger = danger;
		this.scrollThumb = scrollThumb;
		this.activeScrollThumb = activeScrollThumb;
		this.cardRadius = cardRadius;
		this.rowRadius = rowRadius;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
