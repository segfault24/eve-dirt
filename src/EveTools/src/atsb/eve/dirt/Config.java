package atsb.eve.dirt;

import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class Config {

	private static final String BOT = "DIRTbot";
	private static final String VER = "0.1";

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
		Properties props;
		try {
			props = Utils.readProperties();
		} catch(IOException e) {
			props = new Properties();
		}

		numThreads = Utils.parseIntProp(props, "threads", 1);

		dbDriver = Utils.parseStrProp(props, "dbdriver", "mysql");
		dbHost = Utils.parseStrProp(props, "dbhost", "localhost");
		dbPort = Utils.parseIntProp(props, "dbport", 3306);

		dbName = Utils.parseStrProp(props, "dbname", "eve");
		dbUser = Utils.parseStrProp(props, "dbuser", "dirt.scraper");
		dbPass = Utils.parseStrProp(props, "dbpass", "password");

		adminEmail = Utils.parseStrProp(props, "adminemail", "root@localhost");
		scraperAuthKeyId = Utils.parseIntProp(props, "scraperkeyid", 0);
		ssoClientId = Utils.parseStrProp(props, "ssoclientid", "");
		ssoSecretKey = Utils.parseStrProp(props, "ssosecretkey", "");

		marketOrdersRegions = Utils.parseIntListProp(props,
				"marketorders.regions");
		marketOrdersPeriod = Utils.parseIntProp(props, "marketorders.period",
				60);
		marketHistoryRegions = Utils.parseIntListProp(props,
				"markethistory.regions");
		marketHistoryPeriod = Utils.parseIntProp(props, "markethistory.period",
				1440);

		publicStructuresPeriod = Utils.parseIntProp(props,
				"publicstructures.period", 1440);
		insurancePricesPeriod = Utils.parseIntProp(props,
				"insuranceprices.period", 240);
		characterDataPeriod = Utils.parseIntProp(props, "characterdata.period",
				15);
		characterDataExpires = Utils.parseIntProp(props,
				"characterdata.expires", 60);
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
		return BOT + "/" + VER + " (" + adminEmail + ")";
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

}
