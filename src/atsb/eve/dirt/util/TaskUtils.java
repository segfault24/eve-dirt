package atsb.eve.dirt.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import atsb.eve.dirt.model.TaskStatus;

/**
 * @author austin
 *
 */
public class TaskUtils {

	private static final String SELECT_TASK_SQL = "SELECT `taskName`,`lastRun`,`lastRunDuration` FROM taskStatus WHERE `taskName`=?";
	private static final String UPSERT_TASK_SQL = "INSERT INTO taskStatus (`taskName`,`lastRun`,`lastRunDuration`) VALUES(?,?,?) ON DUPLICATE KEY UPDATE `lastRun`=?,`lastRunDuration`=?";

	/**
	 * Get the TaskStatus for the given taskName
	 * 
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
				return new TaskStatus(taskName, rs.getTimestamp(2), rs.getInt(3));
			} else {
				return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}

	/**
	 * Inserts or updates the given TaskStatus
	 * 
	 * @param db
	 * @param taskStatus
	 * @throws SQLException
	 */
	public static void upsertTaskStatus(Connection db, TaskStatus taskStatus) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(UPSERT_TASK_SQL);
		stmt.setString(1, taskStatus.getTaskName());
		stmt.setTimestamp(2, taskStatus.getLastRun());
		stmt.setInt(3, taskStatus.getLastRunDuration());
		stmt.setTimestamp(4, taskStatus.getLastRun());
		stmt.setInt(5, taskStatus.getLastRunDuration());
		stmt.execute();
	}

}
