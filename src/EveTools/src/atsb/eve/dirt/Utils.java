package atsb.eve.dirt;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

	private static Logger logger = Logger.getLogger(Utils.class.toString());

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

	public static Properties readProperties(String filePath) throws IOException {
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(new File(filePath));
		props.load(fis);
		Utils.closeQuietly(fis);
		return props;
	}

	public static Properties readProperties() throws IOException {
		return readProperties(System.getProperties().getProperty("config"));
	}

	public static boolean parseBoolProp(Properties props, String propName,
			boolean _default) {
		String tmp = props.getProperty(propName);
		if (tmp == null) {
			Utils.logger.log(Level.FINE, "Using default " + propName + ": "
					+ _default);
			return _default;
		} else {
			return Boolean.parseBoolean(tmp.trim());
		}
	}

	public static List<Integer> parseIntListProp(Properties props, String propName) {
		String tmp = props.getProperty(propName);
		List<Integer> values = new ArrayList<Integer>();
		if (tmp == null) {
			Utils.logger.log(Level.FINE, "No list for " + propName);
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
					Utils.logger.log(Level.WARNING, propName + " '" + s
							+ "' is not a valid integer");
				}
			}
		}
		return values;
	}

	public static String parseStrProp(Properties props, String propName,
			String _default) {
		String tmp = props.getProperty(propName).trim();
		if (tmp == null) {
			Utils.logger.log(Level.FINE, "Using default " + propName + ": "
					+ _default);
			return _default;
		}
		return tmp;
	}

	public static int parseIntProp(Properties props, String propName, int _default) {
		String tmp = props.getProperty(propName);
		if (tmp == null) {
			Utils.logger.log(Level.FINE, "Using default " + propName + ": "
					+ _default);
			return _default;
		}
		try {
			return Integer.parseInt(tmp.trim());
		} catch (NumberFormatException e) {
			Utils.logger.log(Level.WARNING, propName + " '" + tmp
					+ "' is not a valid integer");
			return _default;
		}
	}

}
