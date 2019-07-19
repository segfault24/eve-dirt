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

import atsb.eve.db.TaskStatusTable;
import atsb.eve.dirt.cli.TaskCli;
import atsb.eve.dirt.task.DirtTask;
import atsb.eve.dirt.task.InsurancePricesTask;
import atsb.eve.dirt.task.InvMarketGroupsTask;
import atsb.eve.dirt.task.InvTypesTask;
import atsb.eve.dirt.task.MERTask;
import atsb.eve.dirt.task.MarketHistoryTask;
import atsb.eve.dirt.task.MarketRegionOrdersTask;
import atsb.eve.dirt.task.MetaCharacterMarketTask;
import atsb.eve.dirt.task.MetaWalletTask;
import atsb.eve.dirt.task.OrderReaperTask;
import atsb.eve.dirt.task.PublicStructuresTask;
import atsb.eve.dirt.task.UnknownIdsTask;
import atsb.eve.dirt.zkill.KillstreamWorker;
import atsb.eve.model.TaskStatus;
import atsb.eve.util.DbInfo;
import atsb.eve.util.DbPool;
import atsb.eve.util.Utils;

/**
 * Main class to launch tasks
 * 
 * @author austin
 */
public class DirtTaskDaemon extends ScheduledThreadPoolExecutor implements Taskable {

	private static Logger log = LogManager.getLogger();

	private final DbPool dbPool;
	private HashMap<String, ScheduledFuture<?>> futures = new HashMap<String, ScheduledFuture<?>>();

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

		int threads = Utils.getIntProperty(db, DirtConstants.PROPERTY_NUM_THREADS);
		setCorePoolSize(threads);
		dbPool.setMinPoolSize(threads);
		log.info("Starting with " + threads + " worker threads");

		addTasks(db);

		// release connection to pool
		dbPool.release(db);

		// start the killstream
		if(Utils.getBoolProperty(db, DirtConstants.PROPERTY_KILLSTREAM_ENABLED)) {
			new Thread(new KillstreamWorker(this)).start();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				dbPool.closeAll();
			}
		});
	}

	private void addTasks(Connection db) {
		// public market orders for specific regions
		List<Integer> regions = Utils.parseIntList(Utils.getProperty(db, DirtConstants.PROPERTY_MARKET_ORDERS_REGIONS));
		int period = Utils.getIntProperty(db, DirtConstants.PROPERTY_MARKET_ORDERS_PERIOD);
		for (Integer regionId : regions) {
			addPeriodicTask(db, new MarketRegionOrdersTask(regionId), period);
		}

		// auto-delete old market orders that might not be cleaned up elsewhere
		addPeriodicTask(db, new OrderReaperTask(), 30);

		// market history for specific regions
		regions = Utils.parseIntList(Utils.getProperty(db, DirtConstants.PROPERTY_MARKET_HISTORY_REGIONS));
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_MARKET_HISTORY_PERIOD);
		for (int regionId : regions) {
			addPeriodicTask(db, new MarketHistoryTask(regionId), period);
		}

		// public structure info
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_PUBLIC_STRUCTURES_PERIOD);
		addPeriodicTask(db, new PublicStructuresTask(), period);

		// insurance price info
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_INSURANCE_PRICES_PERIOD);
		addPeriodicTask(db, new InsurancePricesTask(), period);

		// type & group info
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_TYPE_INFO_PERIOD);
		addPeriodicTask(db, new InvTypesTask(), period);
		addPeriodicTask(db, new InvMarketGroupsTask(), period);

		// monthly econ report
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_MER_PERIOD);
		addPeriodicTask(db, new MERTask(), period);

		// character wallet
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_WALLET_PERIOD);
		addPeriodicTask(db, new MetaWalletTask(), period);

		// character orders and contracts
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_CHARACTER_MARKET_PERIOD);
		addPeriodicTask(db, new MetaCharacterMarketTask(), period);

		// unknown ids resolution
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_UNKNOWN_IDS_PERIOD);
		addPeriodicTask(db, new UnknownIdsTask(), period);
	}

	/**
	 * @param t
	 */
	@Override
	public void addTask(DirtTask t) {
		t.setDaemon(this);
		t.setDbPool(dbPool);
		schedule(t, 0, TimeUnit.MINUTES);
		log.debug("Queued " + t.getTaskName());
	}

	/**
	 * @param db
	 * @param t
	 * @param period
	 */
	public void addPeriodicTask(Connection db, DirtTask t, long period) {
		TaskStatus ts = TaskStatusTable.getTaskStatus(db, t.getTaskName());
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
	public void removeAllTasks() {
		for (ScheduledFuture<?> f : futures.values()) {
			f.cancel(false);
		}
		purge();
		futures.clear();
	}

	public static void main(String[] args) {
		DirtTaskDaemon d = new DirtTaskDaemon();
		if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
			TaskCli cli = new TaskCli(d);
			cli.loop();
		}
	}

}
