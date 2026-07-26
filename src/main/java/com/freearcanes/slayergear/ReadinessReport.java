package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ReadinessReport
{
	private final int gearPacked;
	private final int gearTotal;
	private final boolean protectionReady;
	private final boolean weaponAmmoReady;
	private final String spellStatus;
	private final int suppliesPacked;
	private final int suppliesTotal;
	private final List<String> missingCritical;

	ReadinessReport(
		int gearPacked,
		int gearTotal,
		boolean protectionReady,
		boolean weaponAmmoReady,
		String spellStatus,
		int suppliesPacked,
		int suppliesTotal,
		List<String> missingCritical)
	{
		this.gearPacked = gearPacked;
		this.gearTotal = gearTotal;
		this.protectionReady = protectionReady;
		this.weaponAmmoReady = weaponAmmoReady;
		this.spellStatus = spellStatus == null ? "Not required" : spellStatus;
		this.suppliesPacked = suppliesPacked;
		this.suppliesTotal = suppliesTotal;
		this.missingCritical = Collections.unmodifiableList(new ArrayList<>(missingCritical));
	}

	static ReadinessReport empty()
	{
		return new ReadinessReport(0, 0, true, true, "Not required", 0, 0, Collections.emptyList());
	}

	int getGearPacked() { return gearPacked; }
	int getGearTotal() { return gearTotal; }
	boolean isProtectionReady() { return protectionReady; }
	boolean isWeaponAmmoReady() { return weaponAmmoReady; }
	String getSpellStatus() { return spellStatus; }
	int getSuppliesPacked() { return suppliesPacked; }
	int getSuppliesTotal() { return suppliesTotal; }
	List<String> getMissingCritical() { return missingCritical; }
	boolean isReadyToLeave()
	{
		return gearTotal > 0
			&& gearPacked >= gearTotal
			&& protectionReady
			&& weaponAmmoReady
			&& missingCritical.isEmpty();
	}
}