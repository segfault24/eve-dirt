package atsb.eve.dirt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {

	private static Config me = null;
	private static Logger logger = Logger.getLogger(Config.class.toString());

	private String dbDriver = "mysql";
	private String dbHost = "127.0.0.1";
	private String dbPort = "3306";
	private String dbName = "eve";
	private String dbUser = "admin";
	private String dbPass = "";
	private String adminEmail = "root@localhost";
	private int scraperAuthKeyId = -1;
	private String ssoClientId = "";
	private String ssoSecretKey = "";

	private Config() {
		String configFilePath = System.getProperties().getProperty("config");
		if (configFilePath == null) {
			configFilePath = "../cfg/db.config";
		}

		Properties props = new Properties();
		try {
			FileInputStream fis = new FileInputStream(new File(configFilePath));
			props.load(fis);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failure when reading config properties, using defaults..", e);
		}

		if (props.getProperty("dbdriver") != null)
			dbDriver = props.getProperty("dbdriver");

		if (props.getProperty("dbhost") != null)
			dbHost = props.getProperty("dbhost");

		if (props.getProperty("dbport") != null)
			dbPort = props.getProperty("dbport");

		if (props.getProperty("dbname") != null)
			dbName = props.getProperty("dbname");

		if (props.getProperty("dbuser") != null)
			dbUser = props.getProperty("dbuser");

		if (props.getProperty("dbpass") != null)
			dbPass = props.getProperty("dbpass");

		if (props.getProperty("adminemail") != null)
			adminEmail = props.getProperty("adminemail");

		if (props.getProperty("scraperkeyid") != null)
			scraperAuthKeyId = Integer.parseInt(props.getProperty("scraperkeyid"));

		if (props.getProperty("ssoclientid") != null)
			ssoClientId = props.getProperty("ssoclientid");

		if (props.getProperty("ssosecretkey") != null)
			ssoSecretKey = props.getProperty("ssosecretkey");
	}

	public static Config getInstance() {
		if (me == null) {
			return new Config();
		} else {
			return me;
		}
	}

	public String getDbConnectionString() {
		return "jdbc:" + dbDriver + "://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false";
	}

	public String getDbHost() {
		return dbHost;
	}

	public String getDbPort() {
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

	public String getAdminEmail() {
		return adminEmail;
	}

	public String getUserAgent() {
		return "DIRTbot/0.1 (" + adminEmail + ")";
	}

	public int getScraperAuthKeyId() {
		return scraperAuthKeyId;
	}

	public String getSSOClientId() {
		return ssoClientId;
	}

	public String getSSOSecretKey() {
		return ssoSecretKey;
	}

}
