package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import atsb.eve.dirt.util.DbInfo;
import atsb.eve.dirt.util.Utils;

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

		List<Integer> regions = Utils.parseIntList(Utils.getProperty(db, PROPERTY_MARKET_ORDERS_REGIONS));
		int period = Integer.parseInt(Utils.getProperty(db, PROPERTY_MARKET_ORDERS_PERIOD));
		for (Integer regionId : regions) {
			addPeriodicTask(new PublicMarketOrdersTask(dbInfo, regionId), period);
			logger.log(Level.INFO, "Queued public market order task for " + regionId);
		}

		regions = Utils.parseIntList(Utils.getProperty(db, PROPERTY_MARKET_HISTORY_REGIONS));
		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_MARKET_HISTORY_PERIOD));
		for (int regionId : regions) {
			addPeriodicTask(new MarketHistoryTask(dbInfo, regionId), period);
			logger.log(Level.INFO, "Queued public market history task for " + regionId);
		}

		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_PUBLIC_STRUCTURES_PERIOD));
		addPeriodicTask(new PublicStructuresTask(dbInfo), period);
		logger.log(Level.INFO, "Queued public structures task");

		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_INSURANCE_PRICES_PERIOD));
		addPeriodicTask(new InsurancePricesTask(dbInfo), period);
		logger.log(Level.INFO, "Queued insurance prices task");

		/*
		 * addPeriodicTask(new MetaCharacterTask(this, cfg),
		 * cfg.getCharacterDataPeriod());
		 */
	}

	protected void addSingleTask(Runnable r) {
		this.schedule(r, 0, TimeUnit.MINUTES);
	}

	protected void addPeriodicTask(Runnable r, long period) {
		scheduleWithFixedDelay(r, 0, period, TimeUnit.MINUTES);
	}

	public static void main(String[] args) {
		new DirtTaskDaemon();
	}
}
