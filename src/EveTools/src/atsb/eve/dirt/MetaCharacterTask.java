package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import atsb.eve.dirt.util.DirtProperties;
import atsb.eve.dirt.util.Utils;

/**
 * 
 * 
 * @author austin
 */
public class MetaCharacterTask implements Runnable {

	private static Logger logger = Logger
			.getLogger(MetaCharacterTask.class.toString());

	private DirtTaskDaemon daemon;
	private DirtProperties config;
	private Connection con;

	public MetaCharacterTask(DirtTaskDaemon daemon, DirtProperties cfg) {
		this.daemon = daemon;
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

		// get all characters that need refresh
		// generate new task for each char that need refresh
		//daemon.addSingleTask(new CharacterTask(config));

		Utils.closeQuietly(con);
	}

}
