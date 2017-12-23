package atsb.eve.dirt.mer;

import java.io.IOException;
import java.util.Properties;

import atsb.eve.dirt.Utils;

public class MERLoaderProperties {

	private String dbDriver;
	private String dbHost;
	private int dbPort;

	private String dbName;
	private String dbUser;
	private String dbPass;

	public MERLoaderProperties() {
		Properties props;
		try {
			props = Utils.readProperties();
		} catch(IOException e) {
			props = new Properties();
		}

		dbDriver = Utils.parseStrProp(props, "dbdriver", "mysql");
		dbHost = Utils.parseStrProp(props, "dbhost", "localhost");
		dbPort = Utils.parseIntProp(props, "dbport", 3306);

		dbName = Utils.parseStrProp(props, "dbname", "eve");
		dbUser = Utils.parseStrProp(props, "dbuser", "dirt.merloader");
		dbPass = Utils.parseStrProp(props, "dbpass", "password");
	}

	public String getDbConnectionString() {
		return "jdbc:" + dbDriver + "://" + dbHost + ":" + dbPort + "/"
				+ dbName + "?useSSL=false";
	}

	public String getDbHost() {
		return dbHost;
	}

	public int getDbPort() {
		return dbPort;
	}

	public String getDbName() {
		return dbName;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPass() {
		return dbPass;
	}

}
