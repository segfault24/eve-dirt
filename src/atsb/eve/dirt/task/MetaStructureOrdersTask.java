package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.StructAuthTable;

/**
 * Meta-task that enqueues tasks for structure order data
 * 
 * @author austin
 */
public class MetaStructureOrdersTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	@Override
	public String getTaskName() {
		return "meta-structure-orders";
	}

	@Override
	protected void runTask() {
		List<Long> structIds;
		try {
			structIds = StructAuthTable.getAllUniqueStructs(getDb());
		} catch (SQLException e) {
			log.fatal("Failed to retrieve structure ids", e);
			return;
		}
		for (Long structId : structIds) {
			getDaemon().addTask(new MarketStructureOrdersTask(structId));
		}
	}

}
