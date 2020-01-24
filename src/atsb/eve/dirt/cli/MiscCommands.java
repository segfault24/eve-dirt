package atsb.eve.dirt.cli;

import java.sql.SQLException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import atsb.eve.db.StructAuthTable;
import atsb.eve.dirt.DirtConstants;
import atsb.eve.dirt.Stats;
import atsb.eve.dirt.task.StructureTask;
import atsb.eve.util.Utils;

public class MiscCommands {

	private static Logger log = LogManager.getLogger();

	public static class Status extends Command {

		@Override
		public String getCommandString() {
			return "status";
		}

		@Override
		public String getOptionString() {
			return "";
		}

		@Override
		public String getHelpString() {
			return "get the status of the executor";
		}

		@Override
		public void execute(String[] args) {
			System.out.println("poolsize: " + daemon.getPoolSize());
			System.out.println("complete: " + daemon.getCompletedTaskCount());
			System.out.println("  queued: " + daemon.getQueue().size());
			System.out.println("  active: " + daemon.getActiveCount());
		}

	}

	public static class PoolSize extends Command {

		@Override
		public String getCommandString() {
			return "poolsize";
		}

		@Override
		public String getOptionString() {
			return "<numthreads>";
		}

		@Override
		public String getHelpString() {
			return "get/set the number of worker threads";
		}

		@Override
		public void execute(String[] args) {
			if (args.length > 1) {
				try {
					int s = Integer.parseInt(args[1]);
					if (s <= 0) {
						System.err.println("poolsize must be greater than 0");
						return;
					} else {
						daemon.setCorePoolSize(s);
						daemon.setMaximumPoolSize(s);
						log.info("Core pool size was set to " + s);
					}
				} catch (NumberFormatException e) {
					System.err.println("bad number");
					return;
				}
			}
			System.out.println(daemon.getCorePoolSize());
		}

	}

	public static class LogLevel extends Command {

		@Override
		public String getCommandString() {
			return "loglevel";
		}

		@Override
		public String getOptionString() {
			return "<level>";
		}

		@Override
		public String getHelpString() {
			return "get/set the loglevel";
		}

		@Override
		public void execute(String[] args) {
			if (args.length > 1) {
				try {
					Level l = Level.valueOf(args[1]);
					Configurator.setRootLevel(l);
				} catch (IllegalArgumentException e) {
					log.error("invalid loglevel requested '" + args[1] + "'");
					System.err.println("invalid loglevel");
					return;
				}
			}
			System.out.println(LogManager.getRootLogger().getLevel().toString());
		}

	}

	public static class StructMarketAdd extends Command {

		@Override
		public String getCommandString() {
			return "structadd";
		}

		@Override
		public String getOptionString() {
			return "<structid>";
		}

		@Override
		public String getHelpString() {
			return "add a market structure to the database, using the default esi key";
		}

		@Override
		public void execute(String[] args) {
			if (args.length > 1) {
				Long structId = Long.parseLong(args[1]);
				if (structId != null) {
					int keyId = Utils.getIntProperty(db, DirtConstants.PROPERTY_SCRAPER_KEY_ID);
					StructureTask st = new StructureTask(structId, keyId);
					st.setDaemon(daemon);
					st.setDbPool(dbPool);
					st.run();
					try {
						StructAuthTable.insert(db, structId, keyId);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				log.debug("no structure id specified");
				System.err.println("no structure id specified");
			}
		}

	}

	public static class PrintStats extends Command {

		@Override
		public String getCommandString() {
			return "stats";
		}

		@Override
		public String getOptionString() {
			return "";
		}

		@Override
		public String getHelpString() {
			return "print statistics";
		}

		@Override
		public void execute(String[] args) {
			System.out.println("ESI Calls: " + Stats.esiCalls);
			System.out.println("ESI Errors: " + Stats.esiErrors);
			System.out.println("SSO Calls: " + Stats.ssoCalls);
			System.out.println("SSO Errors: " + Stats.ssoErrors);
		}
	}

}
