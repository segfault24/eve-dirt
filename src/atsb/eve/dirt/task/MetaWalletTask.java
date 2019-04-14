package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.ApiAuthTable;

/**
 * Meta-task that enqueues tasks for character wallet data
 * 
 * @author austin
 */
public class MetaWalletTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	@Override
	public String getTaskName() {
		return "meta-character-wallet";
	}

	@Override
	public void runTask() {
		List<Integer> charIds;
		try {
			charIds = ApiAuthTable.getAllCharacters(getDb());
		} catch (SQLException e) {
			log.fatal("Failed to retrieve character ids", e);
			return;
		}
		for (Integer charId : charIds) {
			getDaemon().addTask(new WalletTask(charId));
		}
	}

}
