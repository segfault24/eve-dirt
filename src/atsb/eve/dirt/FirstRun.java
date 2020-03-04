package atsb.eve.dirt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import atsb.eve.dirt.task.DirtTask;
import atsb.eve.dirt.task.InvMarketGroupsTask;
import atsb.eve.dirt.task.InvTypesTask;
import atsb.eve.dirt.task.MapDataTask;
import atsb.eve.util.DbInfo;
import atsb.eve.util.DbPool;

/**
 * Standalone program to initially populate static data tables.
 * 
 * @author austin
 */
public class FirstRun implements Taskable {

	private Queue<DirtTask> queue;
	private final DbPool dbPool;
	private int numThreads;

	public FirstRun(int threads) {
		this.numThreads = threads;
		queue = new ArrayBlockingQueue<DirtTask>(1000000);
		dbPool = new DbPool(new DbInfo());
		dbPool.setMinPoolSize(numThreads);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				dbPool.closeAll();
			}
		});
	}

	public void run() {
		addTask(new MapDataTask()); // spawns region, constellation, system, and station tasks
		addTask(new InvTypesTask());
		addTask(new InvMarketGroupsTask());

		List<Thread> threads = new ArrayList<Thread>(numThreads);
		for (int i = 0; i < numThreads; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					DirtTask t;
					while ((t = queue.poll()) != null) {
						t.run();
					}
				}
			});
			threads.add(thread);
			thread.start();

			// 5s between each thread start
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}

		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void addTask(DirtTask t) {
		t.setDbPool(dbPool);
		t.setDaemon(this);
		queue.add(t);
	}

	@Override
	public void addTasks(Collection<DirtTask> ts) {
	}

	public static void main(String[] args) {
		Configurator.setRootLevel(Level.INFO);
		new FirstRun(16).run();
	}

}
