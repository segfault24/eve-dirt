package atsb.eve.dirt.util;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.evetech.ApiResponse;

public class Utils {

	private static Logger log = LogManager.getLogger();

	private static final String SELECT_PROPERTY_SQL = "SELECT `propertyValue` FROM property WHERE `propertyName`=?";
	private static final String SELECT_ETAG_SQL = "SELECT `etag` FROM apiReq WHERE `apiReqName`=?";
	private static final String UPSERT_ETAG_SQL = "INSERT INTO apiReq (`apiReqName`,`etag`) VALUES(?,?) ON DUPLICATE KEY UPDATE `apiReqName`=VALUES(`apiReqName`),`etag`=VALUES(`etag`)";

	public static final String PROPERTY_NUM_THREADS = "threads";
	public static final String PROPERTY_MARKET_ORDERS_REGIONS = "marketorders.regions";
	public static final String PROPERTY_MARKET_REGION_ORDERS_PERIOD = "marketorders.region.period";
	public static final String PROPERTY_MARKET_STRUCTURE_ORDERS_PERIOD = "marketorders.structure.period";
	public static final String PROPERTY_MARKET_ORDERS_MAX_AGE = "marketorders.maxage";
	public static final String PROPERTY_MARKET_HISTORY_REGIONS = "markethistory.regions";
	public static final String PROPERTY_MARKET_HISTORY_PERIOD = "markethistory.period";
	public static final String PROPERTY_PUBLIC_STRUCTURES_PERIOD = "publicstructures.period";
	public static final String PROPERTY_INSURANCE_PRICES_PERIOD = "insuranceprices.period";
	public static final String PROPERTY_WALLET_PERIOD = "wallet.period";
	public static final String PROPERTY_CHARACTER_MARKET_PERIOD = "charactermarket.period";
	public static final String PROPERTY_KILLSTREAM_ENABLED = "killstream.enabled";

	public static final String PROPERTY_SCRAPER_KEY_ID = "scraperkeyid";
	public static final String PROPERTY_SSO_CLIENT_ID = "ssoclientid";
	public static final String PROPERTY_SSO_SECRET_KEY = "ssosecretkey";

	public static void closeQuietly(AutoCloseable c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public static void closeQuietly(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException e) {
			// do nothing
		}
	}

	public static List<Integer> parseIntList(String input) {
		List<Integer> values = new ArrayList<Integer>();
		for (String s : input.split(",")) {
			values.add(Integer.parseInt(s.trim()));
		}
		return values;
	}

	public static List<Long> parseLongList(String input) {
		List<Long> values = new ArrayList<Long>();
		for (String s : input.split(",")) {
			values.add(Long.parseLong(s.trim()));
		}
		return values;
	}

	public static String getProperty(Connection db, String propertyName) {
		if (db == null || propertyName == null || propertyName.isEmpty()) {
			log.warn("The db and propertyName must be non-null and non-empty");
			return null;
		}

		String propertyValue = "";
		try {
			PreparedStatement stmt = db.prepareStatement(SELECT_PROPERTY_SQL);
			stmt.setString(1, propertyName);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				propertyValue = rs.getString("propertyValue");
			}
		} catch (SQLException e) {
			log.warn("Failed to read property '" + propertyName + "' from database", e);
		}

		return propertyValue;
	}

	public static int getIntProperty(Connection db, String propertyName) {
		return Integer.parseInt(getProperty(db, propertyName));
	}

	public static long getLongProperty(Connection db, String propertyName) {
		return Long.parseLong(getProperty(db, propertyName));
	}

	public static boolean getBoolProperty(Connection db, String propertyName) {
		String s = getProperty(db, propertyName);
		// try parsing as an int first
		try {
			int i = Integer.parseInt(s);
			return i == 0 ? false : true;
		} catch(NumberFormatException e) {
		}
		// if the int parse failed, try bool parse (defaults to false)
		return Boolean.parseBoolean(s);
	}

	public static String getApiDatasource() {
		return "tranquility";
	}

	public static String getApiLanguage() {
		return "en-us";
	}

	public static String getEtag(ApiResponse<?> a) {
		if (a == null || a.getHeaders() == null) {
			return null;
		}
		List<String> h = a.getHeaders().get("Etag");
		if (h == null || h.isEmpty()) {
			return null;
		}
		return h.get(0).replaceAll("^\"|\"$", "");
	}

	public static String getEtag(Connection db, String apiReqName) {
		if (db == null || apiReqName == null) {
			return null;
		}
		try {
			PreparedStatement stmt = db.prepareStatement(SELECT_ETAG_SQL);
			stmt.setString(1, apiReqName);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			} else {
				return null;
			}
		} catch (SQLException e) {
			log.warn("Failed to retrieve etag for apiReqName=" + apiReqName, e);
			return null;
		}
	}

	public static void upsertEtag(Connection db, String apiReqName, String etag) {
		if (db == null || apiReqName == null || etag == null) {
			return;
		}
		try {
			PreparedStatement stmt = db.prepareStatement(UPSERT_ETAG_SQL);
			stmt.setString(1, apiReqName);
			stmt.setString(2, etag);
			stmt.execute();
		} catch (SQLException e) {
			log.warn("Failed to upsert etag for apiReqName=" + apiReqName, e);
		}
	}

	public static void resetConnection(Connection c) {
		try {
			if (!c.getAutoCommit()) {
				c.rollback();
				c.setAutoCommit(true);
			}
		} catch (SQLException e) {
			log.error("Failed to reset connection", e);
		}
	}

}
