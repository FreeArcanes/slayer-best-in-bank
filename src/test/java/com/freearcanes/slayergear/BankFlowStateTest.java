package com.freearcanes.slayergear;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BankFlowStateTest
{
	@Test
	public void openFilterConfigRefreshAndCloseSequenceIsStable()
	{
		BankFlowState state = new BankFlowState();
		state.openBank();
		assertTrue(state.isBankOpen());
		assertTrue(state.beginFilterTransition());
		assertFalse(state.beginFilterTransition());

		state.activateFilter();
		state.completeFilterTransition();
		assertTrue(state.isFilterActive());

		assertTrue(state.queueBankViewRefresh());
		assertFalse(state.queueBankViewRefresh());
		state.completeBankViewRefresh();
		assertTrue(state.queueBankViewRefresh());

		state.closeBank();
		assertFalse(state.isBankOpen());
		assertFalse(state.isFilterActive());
		assertFalse(state.queueBankViewRefresh());
	}

	@Test
	public void withdrawalBurstQueuesOnlyOneRecalculation()
	{
		BankFlowState state = new BankFlowState();
		state.openBank();
		assertTrue(state.queueRecalculation());
		assertFalse(state.queueRecalculation());
		assertFalse(state.queueRecalculation());
		state.completeRecalculation();
		assertTrue(state.queueRecalculation());
	}

	@Test
	public void closingBankCancelsPendingBankWork()
	{
		BankFlowState state = new BankFlowState();
		state.openBank();
		state.beginFilterTransition();
		state.activateFilter();
		state.queueRecalculation();
		state.queueBankViewRefresh();

		state.closeBank();

		assertTrue(state.beginFilterTransition());
		assertTrue(state.queueRecalculation());
		assertFalse(state.queueBankViewRefresh());
	}

	@Test
	public void strategyCycleIsDebouncedAndSessionResetReleasesIt()
	{
		BankFlowState state = new BankFlowState();
		assertTrue(state.queueStrategyCycle());
		assertFalse(state.queueStrategyCycle());
		state.completeStrategyCycle();
		assertTrue(state.queueStrategyCycle());

		state.resetSession();
		assertTrue(state.queueStrategyCycle());
		assertFalse(state.isBankOpen());
		assertFalse(state.isFilterActive());
	}

	@Test
	public void filterCannotActivateWithoutAnOpenBank()
	{
		BankFlowState state = new BankFlowState();
		state.activateFilter();
		assertFalse(state.isFilterActive());
		assertFalse(state.queueBankViewRefresh());
	}

	@Test
	public void bankLoadoutLocksAndQueuesOneExplicitRefresh()
	{
		BankFlowState state = new BankFlowState();
		state.openBank();
		state.lockLoadout();
		assertTrue(state.isLoadoutLocked());
		assertFalse(state.isLoadoutRefreshPending());

		state.markLoadoutRefreshPending();
		assertTrue(state.isLoadoutRefreshPending());
		assertTrue(state.queueLoadoutRefresh());
		assertFalse(state.queueLoadoutRefresh());

		state.unlockLoadout();
		state.completeLoadoutRefresh();
		assertFalse(state.isLoadoutLocked());
		assertFalse(state.isLoadoutRefreshPending());
		assertTrue(state.queueLoadoutRefresh());
	}

	@Test
	public void closingBankClearsLockedPlanState()
	{
		BankFlowState state = new BankFlowState();
		state.openBank();
		state.lockLoadout();
		state.markLoadoutRefreshPending();

		state.closeBank();

		assertFalse(state.isLoadoutLocked());
		assertFalse(state.isLoadoutRefreshPending());
		assertFalse(state.queueLoadoutRefresh());
	}
}
