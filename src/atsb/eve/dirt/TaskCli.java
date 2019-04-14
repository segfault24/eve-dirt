package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.StructAuthTable;
import atsb.eve.dirt.model.TaskStatus;
import atsb.eve.dirt.task.StructureTask;
import atsb.eve.dirt.util.DbInfo;
import atsb.eve.dirt.util.DbPool;
import atsb.eve.dirt.util.TaskUtils;
import atsb.eve.dirt.util.Utils;

public class TaskCli {

	private static Logger log = LogManager.getLogger();

	private DirtTaskDaemon d;
	private DbPool dbPool;
	private Connection db;

	public TaskCli(DirtTaskDaemon d) {
		this.d = d;
		dbPool = new DbPool(new DbInfo());
		try {
			db = dbPool.acquire();
		} catch (SQLException e) {
			log.fatal("Failed to acquire database connection", e);
			return;
		}
	}

	private void help() {
		log.debug("help command invoked");
		System.out.println("poolsize <numthreads>");
		System.out.println("cleartask <taskname>");
		System.out.println("removeall");
		System.out.println("addstruct <structid>");
		System.out.println("status");
		System.out.println("help");
		System.out.println("exit");
	}

	private void status() {
		log.debug("status command invoked");
		System.out.println("poolsize: " + d.getPoolSize());
		System.out.println("complete: " + d.getCompletedTaskCount());
		System.out.println("  queued: " + d.getQueue().size());
		System.out.println("  active: " + d.getActiveCount());
	}

	private void setPoolSize(String[] parts) {
		log.debug("poolsize command invoked");
		if (parts.length > 1) {
			try {
				int s = Integer.parseInt(parts[1]);
				d.setCorePoolSize(s);
				d.setMaximumPoolSize(s);
				log.info("Set core pool size to " + s);
			} catch (NumberFormatException e) {
				System.err.println("bad number specified");
			}
		} else {
			System.err.println("please specify a number");
		}
	}

	private void clearTask(String[] parts) {
		log.debug("cleartask command invoked");
		if (parts.length > 1) {
			TaskStatus ts = TaskUtils.getTaskStatus(db, parts[1]);
			if (ts != null) {
				ts.setLastRun(new Timestamp(1000));
				try {
					TaskUtils.upsertTaskStatus(db, ts);
				} catch (SQLException e) {
					log.debug("failed to upsert TaskStatus", e);
					System.err.println("failed to clear task status");
				}
			} else {
				log.debug("could not find task " + parts[1]);
				System.err.println("could not find task '" + parts[1] + "'");
			}
			
		} else {
			log.debug("no task name specified");
			System.err.println("no task name specified");
		}
	}

	private void removeAllTasks() {
		log.debug("removeall command invoked");
		d.removeAllTasks();
	}

	private void addStruct(String[] parts) {
		log.debug("addstruct command invoked");
		if (parts.length > 1) {
			Long structId = Long.parseLong(parts[1]);
			if (structId != null) {
				int keyId = Utils.getIntProperty(db, Utils.PROPERTY_SCRAPER_KEY_ID);
				StructureTask st = new StructureTask(structId, keyId);
				st.setDaemon(d);
				st.setDbPool(dbPool);
				st.run();
				try {
					StructAuthTable.insert(db, structId, keyId);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			log.debug("no structure id specified");
			System.err.println("no structure id specified");
		}
	}

	private void exit() {
		log.debug("exit command invoked");
		log.warn("received exit command from task cli");
		dbPool.release(db);
		System.exit(0);
	}

	public void loop() {
		if (db == null) {
			return;
		}
		Scanner in = new Scanner(System.in);

		boolean done = false;
		while (!done) {
			System.out.print("> ");
			String line = in.nextLine();

			String[] parts = line.split("\\s+");
			if (parts.length == 0) {
				continue;
			}

			switch (parts[0]) {
			case "poolsize":
				setPoolSize(parts);
				break;
			case "cleartask":
				clearTask(parts);
				break;
			case "removeall":
				removeAllTasks();
				break;
			case "addstruct":
				addStruct(parts);
				break;
			case "status":
				status();
				break;
			case "exit":
				exit();
				break;
			case "help":
			case "?":
				help();
				break;
			case "":
				break;
			default:
				System.out.println("unknown command");
			}
		}

		in.close();
	}

}
