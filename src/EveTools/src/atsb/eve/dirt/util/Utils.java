package atsb.eve.dirt.util;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

	private static Logger logger = Logger.getLogger(Utils.class.toString());

	private static final String GET_PROPERTY_SQL = "SELECT `propertyValue` FROM property WHERE `propertyName`=?";
	private static final String SELECT_TASK_SQL = "SELECT `taskName`,`lastRun` FROM taskStatus WHERE `taskName`=?";
	private static final String UPSERT_TASK_SQL = "INSERT INTO taskStatus (`taskName`,`lastRun`) VALUES(?,?) ON DUPLICATE KEY UPDATE `lastRun`=?";

	public static void closeQuietly(AutoCloseable c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public static void closeQuietly(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	public static List<Integer> parseIntList(String input) {
		List<Integer> values = new ArrayList<Integer>();
		for (String s : input.split(",")) {
			values.add(Integer.parseInt(s.trim()));
		}
		return values;
	}

	public static String getProperty(Connection db, String propertyName) {
		if (db == null || propertyName == null || propertyName.isEmpty()) {
			logger.log(Level.WARNING, "db and propertyName must be non-null and non-empty");
			return null;
		}

		String propertyValue = "";
		try {
			PreparedStatement stmt = db.prepareStatement(GET_PROPERTY_SQL);
			stmt.setString(1, propertyName);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				propertyValue = rs.getString("propertyValue");
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "failed to read property '" + propertyName + "' from database", e);
		}

		return propertyValue;
	}

	/**
	 * @param db
	 * @param taskName
	 * @return
	 */
	public static TaskStatus getTaskStatus(Connection db, String taskName) {
		try {
			PreparedStatement stmt = db.prepareStatement(SELECT_TASK_SQL);
			stmt.setString(1, taskName);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new TaskStatus(taskName, rs.getTimestamp(2));
			} else {
				logger.log(Level.WARNING, "taskStatus for \"" + taskName + "\" not found");
				return null;
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * Inserts or updates the given TaskStatus
	 * 
	 * @param db
	 * @param taskStatus
	 */
	public static void upsertTaskStatus(Connection db, TaskStatus taskStatus) {
		try {
			PreparedStatement stmt = db.prepareStatement(UPSERT_TASK_SQL);
			stmt.setString(1, taskStatus.taskName);
			stmt.setTimestamp(2, taskStatus.lastRun);
			stmt.setTimestamp(3, taskStatus.lastRun);
			stmt.execute();
		} catch (SQLException e) {
			logger.log(Level.WARNING, e.getLocalizedMessage());
		}
	}

	public static void updateTaskLastRun(Connection db, String taskName) {
		upsertTaskStatus(db, new TaskStatus(taskName, new Timestamp(System.currentTimeMillis())));
	}

}
