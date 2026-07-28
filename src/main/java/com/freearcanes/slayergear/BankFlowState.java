package com.freearcanes.slayergear;

/**
 * Small client-thread coordination state for bank refresh operations.
 * Keeping the queue guards together makes complete event sequences testable
 * without launching the RuneLite client.
 */
final class BankFlowState
{
	private boolean bankOpen;
	private boolean filterActive;
	private boolean recalculationQueued;
	private boolean filterTransitionQueued;
	private boolean bankViewRefreshQueued;
	private boolean strategyCycleQueued;
	private boolean loadoutLocked;
	private boolean loadoutRefreshPending;
	private boolean loadoutRefreshQueued;
	private boolean inventorySetupNeutralizationQueued;

	void openBank()
	{
		if (!bankOpen)
		{
			loadoutLocked = false;
			loadoutRefreshPending = false;
			loadoutRefreshQueued = false;
			inventorySetupNeutralizationQueued = false;
		}
		bankOpen = true;
	}

	void closeBank()
	{
		bankOpen = false;
		filterActive = false;
		filterTransitionQueued = false;
		bankViewRefreshQueued = false;
		recalculationQueued = false;
		loadoutLocked = false;
		loadoutRefreshPending = false;
		loadoutRefreshQueued = false;
		inventorySetupNeutralizationQueued = false;
	}

	void resetSession()
	{
		bankOpen = false;
		filterActive = false;
		recalculationQueued = false;
		filterTransitionQueued = false;
		bankViewRefreshQueued = false;
		strategyCycleQueued = false;
		loadoutLocked = false;
		loadoutRefreshPending = false;
		loadoutRefreshQueued = false;
		inventorySetupNeutralizationQueued = false;
	}

	boolean isBankOpen() { return bankOpen; }
	boolean isFilterActive() { return filterActive; }

	void activateFilter()
	{
		filterActive = bankOpen;
	}

	void deactivateFilter()
	{
		filterActive = false;
		bankViewRefreshQueued = false;
		inventorySetupNeutralizationQueued = false;
	}

	boolean queueRecalculation()
	{
		if (recalculationQueued) return false;
		recalculationQueued = true;
		return true;
	}

	void completeRecalculation() { recalculationQueued = false; }

	boolean beginFilterTransition()
	{
		if (filterTransitionQueued) return false;
		filterTransitionQueued = true;
		return true;
	}

	void completeFilterTransition() { filterTransitionQueued = false; }

	boolean queueBankViewRefresh()
	{
		if (!bankOpen || !filterActive || bankViewRefreshQueued) return false;
		bankViewRefreshQueued = true;
		return true;
	}

	void completeBankViewRefresh() { bankViewRefreshQueued = false; }

	boolean queueStrategyCycle()
	{
		if (strategyCycleQueued) return false;
		strategyCycleQueued = true;
		return true;
	}

	void completeStrategyCycle() { strategyCycleQueued = false; }

	void lockLoadout()
	{
		loadoutLocked = bankOpen;
	}

	void unlockLoadout()
	{
		loadoutLocked = false;
		loadoutRefreshPending = false;
	}

	boolean isLoadoutLocked() { return loadoutLocked; }
	boolean isLoadoutRefreshPending() { return loadoutRefreshPending; }

	void markLoadoutRefreshPending()
	{
		if (loadoutLocked)
		{
			loadoutRefreshPending = true;
		}
	}

	boolean queueLoadoutRefresh()
	{
		if (!bankOpen || loadoutRefreshQueued) return false;
		loadoutRefreshQueued = true;
		return true;
	}

	void completeLoadoutRefresh()
	{
		loadoutRefreshQueued = false;
	}

	boolean queueInventorySetupNeutralization()
	{
		if (!bankOpen || !filterActive || inventorySetupNeutralizationQueued) return false;
		inventorySetupNeutralizationQueued = true;
		return true;
	}

	void completeInventorySetupNeutralization()
	{
		inventorySetupNeutralizationQueued = false;
	}
}
