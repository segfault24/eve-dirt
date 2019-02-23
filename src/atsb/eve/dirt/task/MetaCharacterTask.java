package atsb.eve.dirt.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * 
 * @author austin
 */
public class MetaCharacterTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	public MetaCharacterTask() {
	}

	@Override
	public String getTaskName() {
		return "meta-character";
	}

	@Override
	public void runTask() {
		// get all characters that need refresh
		// generate new task for each char that need refresh
		// daemon.addSingleTask(new CharacterTask(config));
	}

}
