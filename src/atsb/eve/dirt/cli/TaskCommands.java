package atsb.eve.dirt.cli;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.TaskLogTable;
import atsb.eve.model.TaskLog;

public class TaskCommands {

	private static Logger log = LogManager.getLogger();

	public static class TaskStatusCommand extends Command {

		@Override
		public String getCommandString() {
			return "task-status";
		}

		@Override
		public String getOptionString() {
			return "<taskname>";
		}

		@Override
		public String getHelpString() {
			return "get status of a task";
		}

		@Override
		public void execute(String[] args) {
			if (args.length > 1) {
				TaskLog tl = TaskLogTable.getLatestTaskLog(db, args[1]);
				if (tl != null) {
					System.out.println("last run: " + tl.getFinishTime());
					System.out.println("last duration: " + tl.getDuration());
				} else {
					log.error("could not find status for " + args[1]);
					System.err.println("could not find status for '" + args[1] + "'");
				}
			} else {
				log.error("no task name specified");
				System.err.println("no task name specified");
			}
		}

	}

	public static class TaskList extends Command {

		@Override
		public String getCommandString() {
			return "task-list";
		}

		@Override
		public String getOptionString() {
			return "";
		}

		@Override
		public String getHelpString() {
			return "list all queued tasks";
		}

		@Override
		public void execute(String[] args) {
			ArrayList<String> taskNames = new ArrayList<String>(daemon.getTaskNames());
			Collections.sort(taskNames);
			for (String taskName : taskNames) {
				System.out.println(taskName);
			}
		}

	}

	public static class TaskCancel extends Command {

		@Override
		public String getCommandString() {
			return "task-cancel";
		}

		@Override
		public String getOptionString() {
			return "<taskname|all>";
		}

		@Override
		public String getHelpString() {
			return "cancel a task by string";
		}

		@Override
		public void execute(String[] args) {
			if (args.length <= 1) {
				log.error("no task name specified");
				System.err.println("no task name specified");
			} else if (args[1].equalsIgnoreCase("all")) {
				daemon.cancelAllTasks();
				log.info("cancelled all tasks");
				System.out.println("cancelled all tasks");
			} else if (daemon.getTaskNames().contains(args[1])) {
				if (!daemon.cancelTask(args[1])) {
					log.error("failed to cancel task '" + args[1] + "'");
					System.err.println("failed to cancel task '" + args[1] + "'");
				}
			} else {
				log.error("task not found '" + args[1] + "'");
				System.err.println("task not found '" + args[1] + "'");
			}
		}

	}

	public static class TaskAdd extends Command {

		@Override
		public String getCommandString() {
			return "task-add";
		}

		@Override
		public String getOptionString() {
			return "<taskname>";
		}

		@Override
		public String getHelpString() {
			return "add a task by name";
		}

		@Override
		public void execute(String[] args) {
			System.out.println("NOT IMPLEMENTED");
		}

	}

}
