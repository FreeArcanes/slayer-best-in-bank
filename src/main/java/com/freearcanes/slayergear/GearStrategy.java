package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class GearStrategy
{
	private final String name;
	private final String location;
	private final String rationale;
	private final CombatStyle combatStyle;
	private final AttackType attackType;
	private final WeaponRule weaponRule;
	private final int minimumMagic;
	private final int minimumRanged;
	private final String requiredWeapon;
	private final String requiredOffhand;
	private final List<String> preferredItems;
	private final double magicDefenceWeight;
	private final double prayerWeight;
	private final boolean ancientAoe;

	private GearStrategy(Builder builder)
	{
		this.name = builder.name;
		this.location = builder.location;
		this.rationale = builder.rationale;
		this.combatStyle = builder.combatStyle;
		this.attackType = builder.attackType;
		this.weaponRule = builder.weaponRule;
		this.minimumMagic = builder.minimumMagic;
		this.minimumRanged = builder.minimumRanged;
		this.requiredWeapon = builder.requiredWeapon;
		this.requiredOffhand = builder.requiredOffhand;
		this.preferredItems = Collections.unmodifiableList(new ArrayList<>(builder.preferredItems));
		this.magicDefenceWeight = builder.magicDefenceWeight;
		this.prayerWeight = builder.prayerWeight;
		this.ancientAoe = builder.ancientAoe;
	}

	static Builder builder()
	{
		return new Builder();
	}

	String getName() { return name; }
	String getLocation() { return location; }
	String getRationale() { return rationale; }
	CombatStyle getCombatStyle() { return combatStyle; }
	AttackType getAttackType() { return attackType; }
	WeaponRule getWeaponRule() { return weaponRule; }
	int getMinimumMagic() { return minimumMagic; }
	int getMinimumRanged() { return minimumRanged; }
	String getRequiredWeapon() { return requiredWeapon; }
	String getRequiredOffhand() { return requiredOffhand; }
	List<String> getPreferredItems() { return preferredItems; }
	double getMagicDefenceWeight() { return magicDefenceWeight; }
	double getPrayerWeight() { return prayerWeight; }
	boolean isAncientAoe() { return ancientAoe; }

	static final class Builder
	{
		private String name = "Slayer setup";
		private String location = "Task-dependent";
		private String rationale = "Best owned gear for this strategy.";
		private CombatStyle combatStyle = CombatStyle.MELEE;
		private AttackType attackType = AttackType.BALANCED;
		private WeaponRule weaponRule = WeaponRule.ANY;
		private int minimumMagic = 1;
		private int minimumRanged = 1;
		private String requiredWeapon;
		private String requiredOffhand;
		private final List<String> preferredItems = new ArrayList<>();
		private double magicDefenceWeight = 0.08;
		private double prayerWeight = 1.2;
		private boolean ancientAoe;

		Builder name(String value) { this.name = value; return this; }
		Builder location(String value) { this.location = value; return this; }
		Builder rationale(String value) { this.rationale = value; return this; }
		Builder combatStyle(CombatStyle value) { this.combatStyle = value; return this; }
		Builder attackType(AttackType value) { this.attackType = value; return this; }
		Builder weaponRule(WeaponRule value) { this.weaponRule = value; return this; }
		Builder minimumMagic(int value) { this.minimumMagic = value; return this; }
		Builder minimumRanged(int value) { this.minimumRanged = value; return this; }
		Builder requiredWeapon(String value) { this.requiredWeapon = value; return this; }
		Builder requiredOffhand(String value) { this.requiredOffhand = value; return this; }
		Builder preferredItem(String value) { if (value != null) this.preferredItems.add(value); return this; }
		Builder magicDefenceWeight(double value) { this.magicDefenceWeight = value; return this; }
		Builder prayerWeight(double value) { this.prayerWeight = value; return this; }
		Builder ancientAoe(boolean value) { this.ancientAoe = value; return this; }
		GearStrategy build() { return new GearStrategy(this); }
	}
}
