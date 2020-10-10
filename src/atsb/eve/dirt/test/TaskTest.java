package atsb.eve.dirt.test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import atsb.eve.dirt.task.CharacterOrdersTask;
import atsb.eve.dirt.task.DirtTask;
import atsb.eve.dirt.task.DoctrineTask;
import atsb.eve.util.DbInfo;
import atsb.eve.util.DbPool;

public class TaskTest {

	public void run() {
		DbPool dbPool = new DbPool(new DbInfo());
		dbPool.setMinPoolSize(2);
		//DirtTask t = new CharacterOrdersTask(96151338);
		DirtTask t = new CharacterOrdersTask(2114247422);
		t.setDbPool(dbPool);
		t.run();
	}

	public static void main(String[] args) {
		Configurator.setRootLevel(Level.DEBUG);
		new TaskTest().run();
	}

}
