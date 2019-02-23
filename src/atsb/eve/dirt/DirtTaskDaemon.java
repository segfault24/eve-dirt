package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.model.TaskStatus;
import atsb.eve.dirt.task.DirtTask;
import atsb.eve.dirt.task.InsurancePricesTask;
import atsb.eve.dirt.task.MarketHistoryTask;
import atsb.eve.dirt.task.MarketRegionOrdersTask;
import atsb.eve.dirt.task.PublicStructuresTask;
import atsb.eve.dirt.util.DbInfo;
import atsb.eve.dirt.util.DbPool;
import atsb.eve.dirt.util.TaskUtils;
import atsb.eve.dirt.util.Utils;

/**
 * Main class to launch tasks
 * 
 * @author austin
 */
public class DirtTaskDaemon extends ScheduledThreadPoolExecutor {

	private static Logger log = LogManager.getLogger();

	private static final String PROPERTY_NUM_THREADS = "threads";
	private static final String PROPERTY_MARKET_ORDERS_REGIONS = "marketorders.regions";
	private static final String PROPERTY_MARKET_ORDERS_PERIOD = "marketorders.period";
	private static final String PROPERTY_MARKET_HISTORY_REGIONS = "markethistory.regions";
	private static final String PROPERTY_MARKET_HISTORY_PERIOD = "markethistory.period";
	private static final String PROPERTY_PUBLIC_STRUCTURES_PERIOD = "publicstructures.period";
	private static final String PROPERTY_INSURANCE_PRICES_PERIOD = "insuranceprices.period";

	private final DbPool dbPool;

	public DirtTaskDaemon() {
		super(1);

		dbPool = new DbPool(new DbInfo());
		Connection db;
		try {
			db = dbPool.acquire();
		} catch (SQLException e) {
			log.fatal("Failed to acquire database connection", e);
			return;
		}

		int threads = Integer.parseInt(Utils.getProperty(db, PROPERTY_NUM_THREADS));
		setCorePoolSize(threads);
		dbPool.setMinPoolSize(threads);
		log.info("Starting with " + threads + " worker threads");

		// public market orders for specific regions
		List<Integer> regions = Utils.parseIntList(Utils.getProperty(db, PROPERTY_MARKET_ORDERS_REGIONS));
		int period = Integer.parseInt(Utils.getProperty(db, PROPERTY_MARKET_ORDERS_PERIOD));
		for (Integer regionId : regions) {
			addPeriodicTask(db, new MarketRegionOrdersTask(regionId), period);
		}

		// market history for specific regions
		regions = Utils.parseIntList(Utils.getProperty(db, PROPERTY_MARKET_HISTORY_REGIONS));
		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_MARKET_HISTORY_PERIOD));
		for (int regionId : regions) {
			addPeriodicTask(db, new MarketHistoryTask(regionId), period);
		}

		// public structure info
		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_PUBLIC_STRUCTURES_PERIOD));
		addPeriodicTask(db, new PublicStructuresTask(), period);

		// insurance price info
		period = Integer.parseInt(Utils.getProperty(db, PROPERTY_INSURANCE_PRICES_PERIOD));
		addPeriodicTask(db, new InsurancePricesTask(), period);

		/*
		 * addPeriodicTask(new MetaCharacterTask(),
		 * cfg.getCharacterDataPeriod());
		 */

		// release connection to pool
		dbPool.release(db);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				dbPool.closeAll();
			}
		});
	}

	/**
	 * @param t
	 */
	protected void addTask(DirtTask t) {
		t.setDaemon(this);
		t.setDbPool(dbPool);
		schedule(t, 0, TimeUnit.MINUTES);
		log.info("Queued " + t.getTaskName());
	}

	/**
	 * @param db
	 * @param t
	 * @param period
	 */
	protected void addPeriodicTask(Connection db, DirtTask t, long period) {
		TaskStatus ts = TaskUtils.getTaskStatus(db, t.getTaskName());
		long initialDelay = 0;
		if (ts != null) {
			long lastRun = ts.getLastRun().getTime() / 1000 / 60;
			long now = System.currentTimeMillis() / 1000 / 60;
			long minSinceLastRun = now - lastRun;
			if (minSinceLastRun < period) {
				initialDelay = period - minSinceLastRun;
			} else {
				initialDelay = 0;
			}
		}
		t.setDaemon(this);
		t.setDbPool(dbPool);
		scheduleAtFixedRate(t, initialDelay, period, TimeUnit.MINUTES);
		log.info("Queued " + t.getTaskName() + " with period=" + period + " initialDelay=" + initialDelay);
	}

	public static void main(String[] args) {
		DirtTaskDaemon d = new DirtTaskDaemon();
		new TaskCli(d).loop();
	}
}
