package atsb.eve.dirt.util;

import java.sql.Timestamp;

public class TaskStatus {
	public String taskName;
	public Timestamp lastRun;

	public TaskStatus(String taskName, Timestamp lastRun) {
		this.taskName = taskName;
		this.lastRun = lastRun;
	}
}