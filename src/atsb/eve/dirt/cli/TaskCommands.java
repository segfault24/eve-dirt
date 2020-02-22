package atsb.eve.dirt.cli;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.TaskStatusTable;
import atsb.eve.model.TaskStatus;

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
				TaskStatus ts = TaskStatusTable.getTaskStatus(db, args[1]);
				if (ts != null) {
					System.out.println("last run: " + ts.getLastRun());
					System.out.println("last duration: " + ts.getLastRunDuration());
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

	public static class TaskClear extends Command {

		@Override
		public String getCommandString() {
			return "task-clear";
		}

		@Override
		public String getOptionString() {
			return "<taskname>";
		}

		@Override
		public String getHelpString() {
			return "clear a task's last run time";
		}

		@Override
		public void execute(String[] args) {
			if (args.length > 1) {
				TaskStatus ts = TaskStatusTable.getTaskStatus(db, args[1]);
				if (ts != null) {
					ts.setLastRun(new Timestamp(24*60*60*1000));
					try {
						TaskStatusTable.upsertTaskStatus(db, ts);
						log.info("cleared status for '" + args[1] + "'");
						System.out.println("cleared status for '" + args[1] + "'");
					} catch (SQLException e) {
						log.error("failed to clear status for '" + args[1] + "'", e);
						System.err.println("failed to clear status for '" + args[1] + "'");
					}
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
