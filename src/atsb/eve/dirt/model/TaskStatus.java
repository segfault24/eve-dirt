package atsb.eve.dirt.model;

import java.sql.Timestamp;

public class TaskStatus {

	private String taskName;
	private Timestamp lastRun;
	private int lastRunDuration;

	public TaskStatus(String taskName, Timestamp lastRun, int lastRunDuration) {
		this.taskName = taskName;
		this.lastRun = lastRun;
		this.lastRunDuration = lastRunDuration;
	}

	public Timestamp getLastRun() {
		return lastRun;
	}

	public void setLastRun(Timestamp lastRun) {
		this.lastRun = lastRun;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public int getLastRunDuration() {
		return lastRunDuration;
	}

	public void setLastRunDuration(int lastRunDuration) {
		this.lastRunDuration = lastRunDuration;
	}
}