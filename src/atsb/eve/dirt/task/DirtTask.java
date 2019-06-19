package atsb.eve.dirt.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.TaskStatusTable;
import atsb.eve.dirt.DirtTaskDaemon;
import atsb.eve.model.TaskStatus;
import atsb.eve.util.DbPool;

public abstract class DirtTask implements Runnable {

	private static Logger log = LogManager.getLogger();

	private DirtTaskDaemon daemon;
	private DbPool dbPool;
	private Connection db;
	private boolean saveStatus = true;

	public abstract String getTaskName();

	protected abstract void runTask();

	public void setDaemon(DirtTaskDaemon daemon) {
		this.daemon = daemon;
	}

	protected DirtTaskDaemon getDaemon() {
		return daemon;
	}

	public void setDbPool(DbPool dbPool) {
		this.dbPool = dbPool;
	}

	protected Connection getDb() {
		return db;
	}

	public void setSaveStatus(boolean save) {
		saveStatus = save;
	}

	protected boolean isSavingStatus() {
		return saveStatus;
	}

	@Override
	public void run() {
		log.info("Started task " + getTaskName());

		log.trace("Acquiring database connection from pool");
		try {
			db = dbPool.acquire();
		} catch (SQLException e) {
			log.fatal("Failed to acquire database connection, aborting task " + getTaskName(), e);
			return;
		}

		long startTime = Calendar.getInstance().getTimeInMillis();
		runTask();
		long endTime = Calendar.getInstance().getTimeInMillis();

		int duration = (int) ((endTime - startTime) / 1000 / 60);
		if (saveStatus) {
			TaskStatus ts = new TaskStatus(getTaskName(), new Timestamp(startTime), duration);
			try {
				TaskStatusTable.upsertTaskStatus(db, ts);
			} catch (SQLException e) {
				log.warn("Failed to update TaskStatus for " + getTaskName(), e);
			}
		}

		log.trace("Releasing database connection to pool");
		DbPool.resetConnection(db);
		dbPool.release(db);

		log.info("Completed task " + getTaskName() + " in " + duration + " minutes");
	}

}
