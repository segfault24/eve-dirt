package atsb.eve.dirt.cli;

import java.sql.SQLException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import atsb.eve.db.StructAuthTable;
import atsb.eve.dirt.DirtConstants;
import atsb.eve.dirt.Stats;
import atsb.eve.dirt.task.InvMarketGroupsTask;
import atsb.eve.dirt.task.InvTypesTask;
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
			System.out.println("   uptime: " + Stats.uptime());
			System.out.println("esi calls: " + Stats.esiCalls + "  errors: " + Stats.esiErrors);
			System.out.println("sso calls: " + Stats.ssoCalls + "  errors: " + Stats.ssoErrors);
			System.out.println(" poolsize: " + daemon.getPoolSize());
			System.out.println(" complete: " + daemon.getCompletedTaskCount());
			System.out.println("   queued: " + daemon.getQueue().size());
			System.out.println("   active: " + daemon.getActiveCount());
		}

	}

	public static class PoolSize extends Command {

		@Override
		public String getCommandString() {
			return "pool-size";
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
			return "log-level";
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

	public static class StructAdd extends Command {

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
				Long structId;
				try {
					structId = Long.parseLong(args[1]);
				} catch (NumberFormatException e) {
					log.error("bad id specified");
					System.err.println("bad id specified");
					return;
				}
				if (structId != null) {
					int keyId;
					try {
						keyId = Utils.getIntProperty(db, DirtConstants.PROPERTY_SCRAPER_KEY_ID);
					} catch (NumberFormatException e) {
						// retry?
						keyId = Utils.getIntProperty(db, DirtConstants.PROPERTY_SCRAPER_KEY_ID);
					}
					// manually run the task
					StructureTask st = new StructureTask(structId, keyId);
					st.setDaemon(daemon);
					st.setDbPool(dbPool);
					st.run();
					// create a link between the struct and the key
					try {
						StructAuthTable.insert(db, structId, keyId);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				log.error("no structure id specified");
				System.err.println("no structure id specified");
			}
		}

	}

	public static class ForceTypeUpdate extends Command {

		@Override
		public String getCommandString() {
			return "type-update";
		}

		@Override
		public String getOptionString() {
			return "";
		}

		@Override
		public String getHelpString() {
			return "forcibly rescan type and groups";
		}

		@Override
		public void execute(String[] args) {
			InvTypesTask i = new InvTypesTask();
			i.forceUpdate(true);
			daemon.addTask(i);
			InvMarketGroupsTask g = new InvMarketGroupsTask();
			g.forceUpdate(true);
			daemon.addTask(g);
		}

	}

}
