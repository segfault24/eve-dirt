package atsb.eve.dirt.zkill;

import java.io.IOException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import atsb.eve.dirt.DirtTaskDaemon;
import atsb.eve.dirt.task.KillmailTask;

/**
 * Reads the Zkillboard RedisQ feed and enqueues tasks for each killmail.
 * 
 * @author austin
 */
public class KillstreamWorker implements Runnable {

	private static final String ZKILL_REDISQ_URL = "https://redisq.zkillboard.com/listen.php";
	private static final long SLEEP_TIME_MILLIS = 10000;

	private DirtTaskDaemon d;

	public KillstreamWorker(DirtTaskDaemon d) {
		this.d = d;
	}

	@Override
	public void run() {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(ZKILL_REDISQ_URL).build();
		while (true) {
			String data = "";
			try {
				Response resp = client.newCall(request).execute();
				data = resp.body().string();
			} catch (IOException e) {
				data = "";
			}
			if (data.length() > 50) {
				d.addTask(new KillmailTask(data));
			} else {
				try {
					Thread.sleep(SLEEP_TIME_MILLIS);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

}
