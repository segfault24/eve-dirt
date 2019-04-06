package atsb.eve.dirt.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task to retrieve order information for a character.
 * 
 * @author austin
 */
public class CharacterOrdersTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int charId;

	@Override
	public String getTaskName() {
		return "character-orders-" + charId;
	}

	@Override
	protected void runTask() {
		
	}

}
