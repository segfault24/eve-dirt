package atsb.eve.dirt.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.TaskLogTable;
import atsb.eve.dirt.Taskable;
import atsb.eve.model.TaskLog;
import atsb.eve.util.DbPool;

public abstract class DirtTask implements Runnable {

	private static Logger log = LogManager.getLogger();

	private Taskable daemon;
	private DbPool dbPool;
	private Connection db;
	private boolean saveStatus = true;

	public abstract String getTaskName();

	protected abstract void runTask();

	public void setDaemon(Taskable daemon) {
		this.daemon = daemon;
	}

	protected Taskable getDaemon() {
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
	public final void run() {
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

		int duration = (int) ((endTime - startTime) / 1000);
		if (saveStatus) {
			TaskLog tl = new TaskLog();
			tl.setTaskName(getTaskName());
			tl.setStartTime(new Timestamp(startTime));
			tl.setFinishTime(new Timestamp(endTime));
			tl.setDuration(duration);
			tl.setSuccess(true);
			tl.setError(null);
			try {
				TaskLogTable.insertTaskLog(db, tl);
			} catch (SQLException e) {
				log.warn("Failed to update TaskStatus for " + getTaskName(), e);
			}
		}

		log.trace("Releasing database connection to pool");
		DbPool.resetConnection(db);
		dbPool.release(db);

		log.info("Completed task " + getTaskName() + " in " + duration + " seconds");
	}

}
