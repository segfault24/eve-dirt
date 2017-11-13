package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * 
 * @author austin
 */
public class CharacterTask implements Runnable {

	private static Logger logger = Logger
			.getLogger(CharacterTask.class.toString());

	private Config config;
	private Connection con;

	public CharacterTask(Config cfg) {
		this.config = cfg;
	}

	@Override
	public void run() {
		try {
			con = DriverManager.getConnection(config.getDbConnectionString(),
					config.getDbUser(), config.getDbPass());
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

		Utils.closeQuietly(con);
	}

}
