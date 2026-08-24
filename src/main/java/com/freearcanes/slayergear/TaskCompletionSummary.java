package com.freearcanes.slayergear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TaskCompletionSummary
{
	private final String taskName;
	private final int kills;
	private final long elapsedSeconds;
	private final List<String> consumed;

	TaskCompletionSummary(String taskName, int kills, long elapsedSeconds, List<String> consumed)
	{
		this.taskName = taskName == null ? "" : taskName;
		this.kills = Math.max(0, kills);
		this.elapsedSeconds = Math.max(0, elapsedSeconds);
		this.consumed = consumed == null ? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(consumed));
	}

	String getTaskName() { return taskName; }
	int getKills() { return kills; }
	long getElapsedSeconds() { return elapsedSeconds; }
	List<String> getConsumed() { return consumed; }
	boolean isAvailable() { return !taskName.isEmpty(); }
}
