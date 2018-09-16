package atsb.eve.dirt;

public abstract class DirtTask implements Runnable {

	protected String taskName;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
