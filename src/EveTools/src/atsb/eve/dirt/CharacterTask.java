package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import atsb.eve.dirt.util.DbInfo;
import atsb.eve.dirt.util.Utils;

/**
 * 
 * 
 * @author austin
 */
public class CharacterTask implements Runnable {

	private static Logger logger = Logger
			.getLogger(CharacterTask.class.toString());

	private DbInfo dbInfo;
	private Connection db;

	public CharacterTask(DbInfo dbInfo) {
		this.dbInfo = dbInfo;
	}

	@Override
	public void run() {
		try {
			db = DriverManager.getConnection(dbInfo.getDbConnectionString(),
					dbInfo.getUser(), dbInfo.getPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: "
					+ e.getLocalizedMessage());
			return;
		}

		// do work here

		// info
		// contracts
		// wallet
		// orders
		// assets
		// industry?

		Utils.closeQuietly(db);
	}

}
