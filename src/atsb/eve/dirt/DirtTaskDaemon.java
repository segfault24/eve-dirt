package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.model.TaskStatus;
import atsb.eve.dirt.task.DirtTask;
import atsb.eve.dirt.task.InsurancePricesTask;
import atsb.eve.dirt.task.MarketHistoryTask;
import atsb.eve.dirt.task.MarketRegionOrdersTask;
import atsb.eve.dirt.task.MetaCharacterOrdersTask;
import atsb.eve.dirt.task.MetaStructureOrdersTask;
import atsb.eve.dirt.task.MetaWalletTask;
import atsb.eve.dirt.task.OrderReaperTask;
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

	private final DbPool dbPool;
	private HashMap<String, ScheduledFuture<?>> futures;

	public DirtTaskDaemon() {
		super(1);

		log.info("==================================");
		log.info("==  DirtTaskDaemon Starting Up  ==");
		log.info("==================================");

		dbPool = new DbPool(new DbInfo());
		Connection db;
		try {
			db = dbPool.acquire();
		} catch (SQLException e) {
			log.fatal("Failed to acquire database connection", e);
			return;
		}

		int threads = Utils.getIntProperty(db, Utils.PROPERTY_NUM_THREADS);
		setCorePoolSize(threads);
		dbPool.setMinPoolSize(threads);
		log.info("Starting with " + threads + " worker threads");

		futures = new HashMap<String, ScheduledFuture<?>>();

		// public market orders for specific regions
		List<Integer> regions = Utils.parseIntList(Utils.getProperty(db, Utils.PROPERTY_MARKET_ORDERS_REGIONS));
		int period = Utils.getIntProperty(db, Utils.PROPERTY_MARKET_REGION_ORDERS_PERIOD);
		for (Integer regionId : regions) {
			addPeriodicTask(db, new MarketRegionOrdersTask(regionId), period);
		}

		// structure market orders
		period = Utils.getIntProperty(db, Utils.PROPERTY_MARKET_STRUCTURE_ORDERS_PERIOD);
		addPeriodicTask(db, new MetaStructureOrdersTask(), period);

		// auto-delete old market orders that might not be cleaned up elsewhere
		addPeriodicTask(db, new OrderReaperTask(), 30);

		// market history for specific regions
		regions = Utils.parseIntList(Utils.getProperty(db, Utils.PROPERTY_MARKET_HISTORY_REGIONS));
		period = Utils.getIntProperty(db, Utils.PROPERTY_MARKET_HISTORY_PERIOD);
		for (int regionId : regions) {
			addPeriodicTask(db, new MarketHistoryTask(regionId), period);
		}

		// public structure info
		period = Utils.getIntProperty(db, Utils.PROPERTY_PUBLIC_STRUCTURES_PERIOD);
		addPeriodicTask(db, new PublicStructuresTask(), period);

		// insurance price info
		period = Utils.getIntProperty(db, Utils.PROPERTY_INSURANCE_PRICES_PERIOD);
		addPeriodicTask(db, new InsurancePricesTask(), period);

		// character wallet
		period = Utils.getIntProperty(db, Utils.PROPERTY_WALLET_PERIOD);
		addPeriodicTask(db, new MetaWalletTask(), period);

		// character orders
		period = Utils.getIntProperty(db, Utils.PROPERTY_CHARACTER_ORDERS_PERIOD);
		addPeriodicTask(db, new MetaCharacterOrdersTask(), period);

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
	public void addTask(DirtTask t) {
		t.setDaemon(this);
		t.setDbPool(dbPool);
		futures.put(t.getTaskName(), schedule(t, 0, TimeUnit.MINUTES));
		log.debug("Queued " + t.getTaskName());
	}

	/**
	 * @param db
	 * @param t
	 * @param period
	 */
	public void addPeriodicTask(Connection db, DirtTask t, long period) {
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
		futures.put(t.getTaskName(), scheduleAtFixedRate(t, initialDelay, period, TimeUnit.MINUTES));
		log.debug("Queued " + t.getTaskName() + " with period=" + period + " initialDelay=" + initialDelay);
	}

	/**
	 * 
	 */
	protected void removeAllTasks() {
		for (ScheduledFuture<?> f : futures.values()) {
			f.cancel(false);
		}
		purge();
		futures.clear();
	}

	public static void main(String[] args) {
		DirtTaskDaemon d = new DirtTaskDaemon();
		new TaskCli(d).loop();
	}
}
