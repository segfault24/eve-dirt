package atsb.eve.dirt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {

	private static Logger logger = Logger.getLogger(Config.class.toString());

	private int numThreads;

	private String dbDriver;
	private String dbHost;
	private int dbPort;

	private String dbName;
	private String dbUser;
	private String dbPass;

	private String adminEmail;
	private int scraperAuthKeyId;
	private String ssoClientId;
	private String ssoSecretKey;

	private List<Integer> marketOrdersRegions;
	private int marketOrdersPeriod;
	private List<Integer> marketHistoryRegions;
	private int marketHistoryPeriod;

	private int publicStructuresPeriod;
	private int insurancePricesPeriod;
	private int characterDataPeriod;
	private int characterDataExpires;

	public Config() {
		String configFilePath = System.getProperties().getProperty("config");
		if (configFilePath == null) {
			configFilePath = "cfg/daemon.properties";
		}

		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(configFilePath));
			props.load(fis);
		} catch (IOException e) {
			logger.log(
					Level.WARNING,
					"Failure when reading config properties: "
							+ e.getLocalizedMessage());
		}
		Utils.closeQuietly(fis);

		numThreads = parseIntProp(props, "threads", 1);

		dbDriver = parseStrProp(props, "dbdriver", "mysql");
		dbHost = parseStrProp(props, "dbhost", "localhost");
		dbPort = parseIntProp(props, "dbport", 3306);

		dbName = parseStrProp(props, "dbname", "eve");
		dbUser = parseStrProp(props, "dbuser", "dirt.scraper");
		dbPass = parseStrProp(props, "dbpass", "password");

		adminEmail = parseStrProp(props, "adminemail", "root@localhost");
		scraperAuthKeyId = parseIntProp(props, "scraperkeyid", 0);
		ssoClientId = parseStrProp(props, "ssoclientid", "");
		ssoSecretKey = parseStrProp(props, "ssosecretkey", "");

		marketOrdersRegions = parseIntListProp(props, "marketorders.regions");
		marketOrdersPeriod = parseIntProp(props, "marketorders.period", 60);
		marketHistoryRegions = parseIntListProp(props, "markethistory.regions");
		marketHistoryPeriod = parseIntProp(props, "markethistory.period", 1440);

		publicStructuresPeriod = parseIntProp(props, "publicstructures.period",
				1440);
		insurancePricesPeriod = parseIntProp(props, "insuranceprices.period",
				240);
		characterDataPeriod = parseIntProp(props, "characterdata.period", 15);
		characterDataExpires = parseIntProp(props, "characterdata.expires", 60);
	}

	public int getNumThreads() {
		return numThreads;
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

	public List<Integer> getMarketOrdersRegions() {
		return marketOrdersRegions;
	}

	public int getMarketOrdersPeriod() {
		return marketOrdersPeriod;
	}

	public List<Integer> getMarketHistoryRegions() {
		return marketHistoryRegions;
	}

	public int getMarketHistoryPeriod() {
		return marketHistoryPeriod;
	}

	public int getPublicStructuresPeriod() {
		return publicStructuresPeriod;
	}

	public int getInsurancePricesPeriod() {
		return insurancePricesPeriod;
	}

	public int getCharacterDataPeriod() {
		return characterDataPeriod;
	}

	public int getCharacterDataExpires() {
		return characterDataExpires;
	}

	// private helper functions

	private static String parseStrProp(Properties props, String propName,
			String _default) {
		String tmp = props.getProperty(propName).trim();
		if (tmp == null) {
			logger.log(Level.CONFIG, "Using default " + propName + ": "
					+ _default);
			return _default;
		}
		return tmp;
	}

	private static int parseIntProp(Properties props, String propName,
			int _default) {
		String tmp = props.getProperty(propName);
		if (tmp == null) {
			logger.log(Level.CONFIG, "Using default " + propName + ": "
					+ _default);
			return _default;
		}
		try {
			return Integer.parseInt(tmp.trim());
		} catch (NumberFormatException e) {
			logger.log(Level.WARNING, propName + " '" + tmp
					+ "' is not a valid integer");
			return _default;
		}
	}

	private static List<Integer> parseIntListProp(Properties props,
			String propName) {
		String tmp = props.getProperty(propName);
		List<Integer> values = new ArrayList<Integer>();
		if (tmp == null) {
			logger.log(Level.CONFIG, "No list for " + propName);
			return values; // return an empty list
		}
		String[] tmps = tmp.split(",");
		if (tmps.length == 1 && tmps[0] == "") {
			// the property name is there, but no value is set
			// have to catch this or it would generate a warning below
		} else {
			for (String s : tmps) {
				try {
					values.add(Integer.parseInt(s.trim()));
				} catch (NumberFormatException e) {
					logger.log(Level.WARNING, propName + " '" + s
							+ "' is not a valid integer");
				}
			}
		}
		return values;
	}

	private static boolean parseBoolProp(Properties props, String propName,
			boolean _default) {
		String tmp = props.getProperty(propName);
		if (tmp == null) {
			logger.log(Level.CONFIG, "Using default " + propName + ": "
					+ _default);
			return _default;
		} else {
			return Boolean.parseBoolean(tmp.trim());
		}
	}
}
