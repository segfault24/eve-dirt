package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.TaskLogTable;
import atsb.eve.dirt.cli.TaskCli;
import atsb.eve.dirt.task.CorpContractsTask;
import atsb.eve.dirt.task.DerivedTableTask;
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
import atsb.eve.model.TaskLog;
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

	public DirtTaskDaemon(boolean startWithPeriodicTasks) {
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

		if (startWithPeriodicTasks) {
			log.info("Initializing periodic tasks");
			initPeriodicTasks(db);
		}

		// release connection to pool
		dbPool.release(db);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				dbPool.closeAll();
			}
		});
	}

	private void initPeriodicTasks(Connection db) {
		// public market orders for specific regions
		List<Integer> regions = Utils.parseIntList(Utils.getProperty(db, DirtConstants.PROPERTY_MARKET_ORDERS_REGIONS));
		int period = Utils.getIntProperty(db, DirtConstants.PROPERTY_MARKET_ORDERS_PERIOD);
		for (Integer regionId : regions) {
			addPeriodicTask(db, new MarketRegionOrdersTask(regionId), period);
		}

		// auto-delete old market orders that might not be cleaned up elsewhere
		addPeriodicTask(db, new OrderReaperTask(), 30);

		// auto-regenerate derived tables periodically
		addPeriodicTask(db, new DerivedTableTask(), 120);

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

		// corporation contracts
		period = Utils.getIntProperty(db, DirtConstants.PROPERTY_CORP_CONTRACTS_PERIOD);
		addPeriodicTask(db, new CorpContractsTask(), period);

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
	 * @param ts
	 */
	@Override
	public void addTasks(Collection<DirtTask> ts) {
		for (DirtTask t : ts) {
			addTask(t);
		}
	}

	/**
	 * @param db
	 * @param t
	 * @param period in minutes
	 */
	public void addPeriodicTask(Connection db, DirtTask t, long period) {
		TaskLog ts = TaskLogTable.getLatestTaskLog(db, t.getTaskName());
		long initialDelay = 0;
		if (ts != null) {
			long lastRun = ts.getFinishTime().getTime() / 1000 / 60;
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
	 * @return
	 */
	public Set<String> getTaskNames() {
		return futures.keySet();
	}

	/**
	 * @param taskName
	 * @return
	 */
	public boolean cancelTask(String taskName) {
		return cancelTask(taskName, false);
	}

	/**
	 * @param taskName
	 * @param force
	 * @return
	 */
	public boolean cancelTask(String taskName, boolean force) {
		ScheduledFuture<?> future = futures.get(taskName);
		if (future != null) {
			return future.cancel(force);
		} else {
			return false;
		}
	}

	/**
	 * 
	 */
	public void cancelAllTasks() {
		for (ScheduledFuture<?> f : futures.values()) {
			f.cancel(false);
		}
		purge();
		futures.clear();
	}

	public static void main(String[] args) {
		boolean cli = false;
		boolean notasks = false;
		boolean help = false;
		for (String arg : args) {
			if (arg.equalsIgnoreCase("--cli")) {
				cli = true;
			} else if (arg.equalsIgnoreCase("--notasks")) {
				notasks = true;
			} else if (arg.equalsIgnoreCase("--help")) {
				help = true;
			} else {
				System.err.println("Unknown option \"" + arg + "\"");
				help = true;
			}
		}

		if (help) {
			System.out.println("DirtTaskDaemon");
			System.out.println("  --cli       drop to cli after startup");
			System.out.println("  --notasks   start without initial tasks");
			System.out.println("  --help      show this message");
			return;
		}

		DirtTaskDaemon d = new DirtTaskDaemon(!notasks);
		if (cli) {
			TaskCli c = new TaskCli(d);
			c.loop();
		}
	}

}
