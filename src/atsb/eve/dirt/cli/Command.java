package atsb.eve.dirt.cli;

import java.sql.Connection;

import atsb.eve.dirt.DirtTaskDaemon;
import atsb.eve.util.DbPool;

public abstract class Command {

	protected DirtTaskDaemon daemon;
	protected DbPool dbPool;
	protected Connection db;

	final void setHelpers(DirtTaskDaemon daemon, DbPool dbPool, Connection db) {
		this.daemon = daemon;
		this.dbPool = dbPool;
		this.db = db;
	}

	public abstract String getCommandString();

	public abstract String getOptionString();

	public abstract String getHelpString();

	public abstract void execute(String[] args);

}
