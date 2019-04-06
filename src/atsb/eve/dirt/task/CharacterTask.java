package atsb.eve.dirt.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Task to retrieve information about a character.
 * 
 * @author austin
 */
public class CharacterTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int charId;

	public CharacterTask(int charId) {
		this.charId = charId;
	}

	@Override
	public String getTaskName() {
		return "character-" + charId;
	}

	@Override
	public void runTask() {
		// do work here

		// info
		// contracts
		// wallet
		// orders
		// assets
		// industry?
	}

}
