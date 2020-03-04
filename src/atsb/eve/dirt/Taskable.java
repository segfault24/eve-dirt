package atsb.eve.dirt;

import java.util.Collection;

import atsb.eve.dirt.task.DirtTask;

public interface Taskable {

	public void addTask(DirtTask t);
	public void addTasks(Collection<DirtTask> ts);

}
