package atsb.eve.dirt;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DirtTaskDaemon extends ScheduledThreadPoolExecutor {

	private DaemonProperties cfg;

	public DirtTaskDaemon() {
		super(1);
		cfg = new DaemonProperties();
		setCorePoolSize(cfg.getNumThreads());

		for (int regionId : cfg.getMarketOrdersRegions()) {
			addPeriodicTask(new PublicMarketOrdersTask(cfg, regionId),
					cfg.getMarketOrdersPeriod());
		}

		for (int regionId : cfg.getMarketHistoryRegions()) {
			addPeriodicTask(new MarketHistoryTask(cfg, regionId),
					cfg.getMarketHistoryPeriod());
		}

		addPeriodicTask(new PublicStructuresTask(cfg),
				cfg.getPublicStructuresPeriod());

		addPeriodicTask(new InsurancePricesTask(cfg),
				cfg.getInsurancePricesPeriod());

		/*addPeriodicTask(new MetaCharacterTask(this, cfg),
				cfg.getCharacterDataPeriod());*/
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
