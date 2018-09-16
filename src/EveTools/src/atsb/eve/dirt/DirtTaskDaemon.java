package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import atsb.eve.dirt.util.DbInfo;
import atsb.eve.dirt.util.TaskStatus;
import atsb.eve.dirt.util.Utils;

/**
 * Main class to launch tasks
 * 
 * @author austin
 */
public class DirtTaskDaemon extends ScheduledThreadPoolExecutor {

	private static Logger logger = Logger.getLogger(DirtTaskDaemon.class.toString());

	private static final String PROPERTY_NUM_THREADS = "threads";
	private static final String PROPERTY_MARKET_ORDERS_REGIONS = "marketorders.regions";
	private static final String PROPERTY_MARKET_ORDERS_PERIOD = "marketorders.period";
	private static final String PROPERTY_MARKET_HISTORY_REGIONS = "markethistory.regions";
	private static final String PROPERTY_MARKET_HISTORY_PERIOD = "markethistory.period";
	private static final String PROPERTY_PUBLIC_STRUCTURES_PERIOD = "publicstructures.period";
	private static final String PROPERTY_INSURANCE_PRICES_PERIOD = "insuranceprices.period";

	private DbInfo dbInfo;

	public DirtTaskDaemon() {
		super(1);
		dbInfo = new DbInfo();

		Connection db;
		try {
			db = DriverManager.getConnection(dbInfo.getDbConnectionString(), dbInfo.getUser(), dbInfo.getPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: " + e.getLocalizedMessage());
			return;
		}

		int threads = Integer.parseInt(Utils.getProperty(db, PROPERTY_NUM_THREADS));
		setCorePoolSize(threads);
		logger.log(Level.INFO, "Starting with " + threads + " worker threads");

		// public market orders for specific regions
		List<Integer> regions = Utils.parseIntList(Utils.getProperty(db, PROPERTY_MARKET_ORDERS_REGIONS));
		int period = Integer.parseInt(Utils.getProperty(db, PROPERTY_MARKET_ORDERS_PERIOD));
		for (Integer regionId : regions) {
			addPeriodicTask(db, new PublicMarketOrdersTask(dbInfo, regionId), period);
			logger.log(Level.INFO, "Queued public market order task for " + regionId);
		}

		// market history for specific regions
		regions = Utils.parseIntList(Utils.getProperty(db, PROPERTY_MARKET_HISTORY_REGIONS));
		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_MARKET_HISTORY_PERIOD));
		for (int regionId : regions) {
			addPeriodicTask(db, new MarketHistoryTask(dbInfo, regionId), period);
			logger.log(Level.INFO, "Queued public market history task for " + regionId);
		}

		// public structure info
		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_PUBLIC_STRUCTURES_PERIOD));
		addPeriodicTask(db, new PublicStructuresTask(dbInfo), period);
		logger.log(Level.INFO, "Queued public structures task");

		// insurance price info
		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_INSURANCE_PRICES_PERIOD));
		addPeriodicTask(db, new InsurancePricesTask(dbInfo), period);
		logger.log(Level.INFO, "Queued insurance prices task");

		/*
		 * addPeriodicTask(new MetaCharacterTask(this, cfg),
		 * cfg.getCharacterDataPeriod());
		 */
	}

	protected void addSingleTask(DirtTask t) {
		schedule(t, 0, TimeUnit.MINUTES);
	}

	protected void addPeriodicTask(Connection db, DirtTask t, long period) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		int offset = 0;
		TaskStatus ts = Utils.getTaskStatus(db, t.getTaskName());
		if (ts != null) {
			offset = (int) (now.getTime() - ts.lastRun.getTime()) / 1000 / 60;
		}
		scheduleAtFixedRate(t, offset, period, TimeUnit.MINUTES);
	}

	public static void main(String[] args) {
		new DirtTaskDaemon();
	}
}
