package atsb.eve.dirt;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import atsb.eve.dirt.task.DirtTask;
import atsb.eve.dirt.task.MERTask;
import atsb.eve.util.DbInfo;
import atsb.eve.util.DbPool;

public class MERTest {

	public void run() {
		DbPool dbPool = new DbPool(new DbInfo());
		dbPool.setMinPoolSize(2);
		DirtTask t = new MERTask();
		t.setDbPool(dbPool);
		t.run();
	}

	public static void main(String[] args) {
		Configurator.setRootLevel(Level.DEBUG);
		new MERTest().run();
	}

}
