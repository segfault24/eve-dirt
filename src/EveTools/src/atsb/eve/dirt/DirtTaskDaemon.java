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
		DirtTaskDaemon daemon = new DirtTaskDaemon(4);

		int[] regionIds = { 10000002, 10000043, 10000032, 10000030, 10000042,
				10000039, 10000059, 10000014, 10000022, 10000031, 10000056 };
		for (int regionId : regionIds) {
			daemon.addTask(new PublicMarketOrdersTask(regionId), 60);
			daemon.addTask(new MarketHistoryTask(regionId), 60*24);
		}

		// daemon.addTask(new CharacterOrdersTask(characterId), 60);
		// daemon.addTask(new CharacterContractsTask(characterId), 5);
	}

}
