package atsb.eve.dirt.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import atsb.eve.dirt.zkill.Zkillmail;

public class KillmailTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private String data;

	public KillmailTask(String data) {
		this.data = data;
		setSaveStatus(false);
	}

	@Override
	public String getTaskName() {
		return "killmail";
	}

	@Override
	protected void runTask() {
		Gson gson = new Gson();
		Zkillmail k = gson.fromJson(data, Zkillmail.class);
	}

}
