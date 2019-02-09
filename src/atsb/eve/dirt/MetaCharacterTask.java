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
public class MetaCharacterTask implements Runnable {

	private static Logger logger = Logger
			.getLogger(MetaCharacterTask.class.toString());

	private DirtTaskDaemon daemon;
	private DbInfo dbInfo;
	private Connection db;

	public MetaCharacterTask(DirtTaskDaemon daemon, DbInfo dbInfo) {
		this.daemon = daemon;
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

		// get all characters that need refresh
		// generate new task for each char that need refresh
		//daemon.addSingleTask(new CharacterTask(config));

		Utils.closeQuietly(db);
	}

}
