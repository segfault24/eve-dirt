package atsb.eve.dirt;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskCli {

	private static Logger log = LogManager.getLogger();

	private DirtTaskDaemon d;

	public TaskCli(DirtTaskDaemon d) {
		this.d = d;
	}

	private void help() {
		System.out.println("poolsize <int>");
		System.out.println("status");
		System.out.println("help");
	}

	private void status() {
		System.out.println("poolsize: " + d.getPoolSize());
		System.out.println("complete: " + d.getCompletedTaskCount());
		System.out.println("  queued: " + d.getQueue().size());
		System.out.println("  active: " + d.getActiveCount());
	}

	private void setPoolSize(String[] parts) {
		if (parts.length > 1) {
			try {
				int s = Integer.parseInt(parts[1]);
				d.setCorePoolSize(s);
				d.setMaximumPoolSize(s);
				log.info("Set core pool size to " + s);
			} catch (NumberFormatException e) {
				System.err.println("bad number specified");
			}
		} else {
			System.err.println("please specify a number");
		}
	}

	public void loop() {
		Scanner in = new Scanner(System.in);

		boolean done = false;
		while (!done) {
			System.out.print("> ");
			String line = in.nextLine();

			String[] parts = line.split("\\s+");
			if (parts.length == 0) {
				continue;
			}

			switch (parts[0]) {
			case "poolsize":
				setPoolSize(parts);
				break;
			// -------------
			case "status":
				status();
				break;
			case "help":
			case "?":
				help();
				break;
			case "":
				break;
			default:
				System.out.println("unknown command");
			}
		}

		in.close();
	}

}
