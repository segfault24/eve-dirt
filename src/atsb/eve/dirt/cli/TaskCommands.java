package atsb.eve.dirt.cli;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.TaskStatusTable;
import atsb.eve.model.TaskStatus;

public class TaskCommands {

	private static Logger log = LogManager.getLogger();

	public static class TaskClear extends Command {

		@Override
		public String getCommandString() {
			return "taskclear";
		}

		@Override
		public String getOptionString() {
			return "<taskname>";
		}

		@Override
		public String getHelpString() {
			return "";
		}

		@Override
		public void execute(String[] args) {
			if (args.length > 1) {
				TaskStatus ts = TaskStatusTable.getTaskStatus(db, args[1]);
				if (ts != null) {
					ts.setLastRun(new Timestamp(1000));
					try {
						TaskStatusTable.upsertTaskStatus(db, ts);
					} catch (SQLException e) {
						log.debug("failed to upsert TaskStatus", e);
						System.err.println("failed to clear task status");
					}
				} else {
					log.debug("could not find task " + args[1]);
					System.err.println("could not find task '" + args[1] + "'");
				}

			} else {
				log.debug("no task name specified");
				System.err.println("no task name specified");
			}
		}

	}

	public static class TaskCancelAll extends Command {

		@Override
		public String getCommandString() {
			return "taskcancelall";
		}

		@Override
		public String getOptionString() {
			return "";
		}

		@Override
		public String getHelpString() {
			return "";
		}

		@Override
		public void execute(String[] args) {
			daemon.removeAllTasks();
		}

	}

}
