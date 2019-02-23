package atsb.eve.dirt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbPool {

	private static Logger log = LogManager.getLogger();

	private DbInfo info;
	private int minSize = 4;

	private List<Connection> free;
	private List<Connection> inuse;

	public DbPool(DbInfo info) {
		this.info = info;
		free = new ArrayList<Connection>();
		inuse = new ArrayList<Connection>();
	}

	public synchronized void setMinPoolSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMinPoolSize() {
		return minSize;
	}

	public synchronized Connection acquire() throws SQLException {
		Connection c;
		if (!free.isEmpty()) {
			c = free.remove(0);
		} else {
			c = createConnection();
		}
		inuse.add(c);
		return c;
	}

	public synchronized void release(Connection c) {
		if (c == null) {
			return;
		}
		if (!inuse.remove(c) || free.size() + inuse.size() > minSize) {
			Utils.closeQuietly(c);
		} else {
			free.add(c);
		}
	}

	/**
	 * Force shutdown all database connections
	 */
	public synchronized void closeAll() {
		log.info("Closing all open database connections in pool");
		for (Connection c : free) {
			Utils.closeQuietly(c);
		}
		free.clear();
		for (Connection c : inuse) {
			Utils.closeQuietly(c);
		}
		inuse.clear();
	}

	private Connection createConnection() throws SQLException {
		return DriverManager.getConnection(info.getDbConnectionString(), info.getUser(), info.getPass());
	}

}
