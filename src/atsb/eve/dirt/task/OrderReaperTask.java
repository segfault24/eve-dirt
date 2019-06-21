package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.MarketOrderTable;
import atsb.eve.dirt.DirtConstants;
import atsb.eve.util.Utils;

public class OrderReaperTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	@Override
	public String getTaskName() {
		return "order-reaper";
	}

	@Override
	protected void runTask() {
		try {
			long maxAgeMinutes = Long.parseLong(Utils.getProperty(getDb(), DirtConstants.PROPERTY_MARKET_ORDERS_MAX_AGE));
			Timestamp olderThan = new Timestamp(System.currentTimeMillis() - maxAgeMinutes*60*1000);
			int count = MarketOrderTable.deleteOldOrders(getDb(), olderThan);
			log.debug("Deleted " + count + " old market orders");
		} catch (SQLException e) {
			log.fatal("Failed to delete old market orders", e);
		}
	}

}