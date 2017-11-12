package atsb.eve.dirt;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DirtTaskDaemon extends ScheduledThreadPoolExecutor {

	public DirtTaskDaemon(int poolSize) {
		super(poolSize);
	}

	public void addTask(Runnable r, long period) {
		scheduleAtFixedRate(r, 0, period, TimeUnit.MINUTES);
	}

	public static void main(String[] args) {
		DirtTaskDaemon daemon = new DirtTaskDaemon(8);
		Config cfg = new Config();

		for (int regionId : cfg.getMarketOrdersRegions()) {
			daemon.addTask(new PublicMarketOrdersTask(cfg, regionId),
					cfg.getMarketOrdersPeriod());
		}

		for (int regionId : cfg.getMarketHistoryRegions()) {
			daemon.addTask(new MarketHistoryTask(cfg, regionId),
					cfg.getMarketHistoryPeriod());
		}

		daemon.addTask(new PublicStructuresTask(cfg, cfg.getScraperAuthKeyId(),
				cfg.getPublicStructuresPurge()), cfg
				.getPublicStructuresPeriod());
		
		//new PublicStructuresTask(cfg, cfg.getScraperAuthKeyId(),
		//		cfg.getPublicStructuresPurge()).run();

		// daemon.addTask(new CharacterOrdersTask(cfg, characterId), 60);
		// daemon.addTask(new CharacterContractsTask(cfg, characterId), 5);
		
	}
}
