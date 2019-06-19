package atsb.eve.dirt.mer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import atsb.eve.util.Utils;

class CSVParser {

	private static Logger log = LogManager.getLogger();

	private BufferedReader br;
	private String[] line;
	private HashMap<String, Integer> headers;

	public CSVParser(File f, boolean hasHeaders) throws FileNotFoundException, CSVException {
		br = new BufferedReader(new FileReader(f));
		if (hasHeaders) {
			headers = new HashMap<String, Integer>();
			try {
				if (next()) {
					for (int i = 0; i < line.length; i++) {
						String colHeader = line[i].trim().toLowerCase();
						headers.put(colHeader, i);
						log.debug("Identified column '" + colHeader + "' at index " + i);
					}
				} else {
					throw new CSVException("Failed to parse CSV column headers");
				}
			} catch (IOException e) {
				throw new CSVException("Failure when reading file");
			}
		}
	}

	public boolean next() throws IOException {
		boolean retval = false;
		if (br != null) {
			String tmp = br.readLine();
			if (tmp != null) {
				line = tmp.replace("\"", "").trim().split(",");
				retval = true;
			}
		}

		// something went wrong somewhere
		if (!retval) {
			Utils.closeQuietly(br);
			br = null;
			line = null;
		}

		return retval;
	}

	private String idx(int i) throws CSVException {
		if (i < 0 || i >= line.length) {
			throw new CSVException("Bad column index '" + i
					+ "'; there are only " + line.length
					+ " columns in this row");
		}
		return line[i];
	}

	public int getColumnIndex(String colTitle) throws CSVException {
		Integer idx = headers.get(colTitle.toLowerCase());
		if (idx == null) {
			throw new CSVException("Could not find column '" + colTitle + "'");
		}
		return idx.intValue();
	}

	public int getNumCols() {
		return headers.size();
	}

	public DateTime getDate(int i) throws CSVException {
		return DateTime.parse(idx(i));
	}

	public DateTime getDate(String colTitle) throws CSVException {
		return getDate(getColumnIndex(colTitle));
	}

	public short getShort(int i) throws CSVException {
		return Short.parseShort(idx(i));
	}

	public short getShort(String colTitle) throws CSVException {
		return getShort(getColumnIndex(colTitle));
	}

	public int getInt(int i) throws CSVException {
		return Integer.parseInt(idx(i));
	}

	public int getInt(String colTitle) throws CSVException {
		return getInt(getColumnIndex(colTitle));
	}

	public long getLong(int i) throws CSVException {
		return Long.parseLong(idx(i));
	}

	public long getLong(String colTitle) throws CSVException {
		return getLong(getColumnIndex(colTitle));
	}

	public float getFloat(int i) throws CSVException {
		return Float.parseFloat(idx(i));
	}

	public float getFloat(String colTitle) throws CSVException {
		return getFloat(getColumnIndex(colTitle));
	}

	public double getDouble(int i) throws CSVException {
		return Double.parseDouble(idx(i));
	}

	public double getDouble(String colTitle) throws CSVException {
		return getDouble(getColumnIndex(colTitle));
	}

	public boolean getBool(int i) throws CSVException {
		return Boolean.parseBoolean(idx(i));
	}

	public boolean getBool(String colTitle) throws CSVException {
		return getBool(getColumnIndex(colTitle));
	}
}
