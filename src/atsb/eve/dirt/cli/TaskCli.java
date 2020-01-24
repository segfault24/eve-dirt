package atsb.eve.dirt.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.DirtTaskDaemon;
import atsb.eve.util.DbInfo;
import atsb.eve.util.DbPool;

public class TaskCli {

	private static Logger log = LogManager.getLogger();

	private DirtTaskDaemon daemon;
	private DbPool dbPool;
	private Connection db;

	private Map<String, Command> commands = new TreeMap<String, Command>();

	public TaskCli(DirtTaskDaemon d) {
		this.daemon = d;
		dbPool = new DbPool(new DbInfo());
		try {
			db = dbPool.acquire();
		} catch (SQLException e) {
			log.fatal("Failed to acquire database connection", e);
			return;
		}

		addCommand(new MiscCommands.PoolSize());
		addCommand(new MiscCommands.LogLevel());
		addCommand(new MiscCommands.Status());
		addCommand(new MiscCommands.StructMarketAdd());
		addCommand(new MiscCommands.PrintStats());
		addCommand(new TaskCommands.TaskClear());
		addCommand(new TaskCommands.TaskCancelAll());
	}

	private void addCommand(Command c) {
		if (!commands.containsKey(c.getCommandString())) {
			c.setHelpers(daemon, dbPool, db);
			commands.put(c.getCommandString(), c);
		} else {
			log.error("Failed to register command '" + c.getCommandString() + "', already exists");
			return;
		}
	}

	private void help(String[] args) {
		if (args.length > 1) {
			Command c = commands.get(args[1]);
			if (c != null) {
				System.out.println(c.getHelpString());
				return;
			}
		} else {
			for (Entry<String, Command> c : commands.entrySet()) {
				System.out.println(c.getValue().getCommandString() + " " + c.getValue().getOptionString());
			}
			System.out.println("help <command>");
			System.out.println("exit");
		}
	}

	private void exit() {
		dbPool.release(db);
		daemon.shutdown();
		try {
			log.warn("Awaiting completion of tasks (timeout 5 min)");
			System.out.println("awaiting completion of tasks (timeout 5 min)...");
			daemon.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
		}
		System.out.flush();
		System.exit(0);
	}

	public void loop() {
		if (db == null) {
			return;
		}
		Scanner in = new Scanner(System.in);

		boolean done = false;
		while (!done) {
			System.out.print("> ");
			String line = in.nextLine();

			String[] parts = line.split("\\s+");
			if (parts.length == 0) {
				continue;
			}

			switch (parts[0].trim().toLowerCase()) {
			case "":
				break;
			case "help":
			case "?":
				help(parts);
				break;
			case "exit":
			case "quit":
			case "q":
				exit();
				break;
			default:
				Command c = commands.get(parts[0]);
				if (c != null) {
					log.info("Executing command '" + c.getCommandString() + "'");
					c.execute(parts);
				} else {
					System.out.println("unknown command");
				}
			}
		}

		in.close();
	}

}
