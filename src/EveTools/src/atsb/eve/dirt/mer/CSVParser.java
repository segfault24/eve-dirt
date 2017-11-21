package atsb.eve.dirt.mer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import atsb.eve.dirt.Utils;

class CSVParser {

	private static Logger logger = Logger.getLogger(CSVParser.class.toString());

	protected BufferedReader br;

	private String[] line;
	private String[] titles;

	public CSVParser(File f) throws FileNotFoundException {
		br = new BufferedReader(new FileReader(f));
		next();
		titles = line.clone();
	}

	public boolean next() {
		boolean retval = false;
		if (br != null) {
			String tmp;
			try {
				tmp = br.readLine();
				if (tmp != null) {
					line = tmp.split(",");
					retval = true;
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE,
						"Error while reading file: " + e.getLocalizedMessage());
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

	public String getColumnTitle(int index) {
		return titles[index];
	}

	public DateTime getDate(int index) {
		return DateTime.parse(line[index]);
	}

	public Short getShort(int index) {
		return Short.parseShort(line[index]);
	}

	public Integer getInt(int index) {
		return Integer.parseInt(line[index]);
	}

	public Long getLong(int index) {
		return Long.parseLong(line[index]);
	}

	public Float getFloat(int index) {
		return Float.parseFloat(line[index]);
	}

	public Double getDouble(int index) {
		return Double.parseDouble(line[index]);
	}

	public Boolean getBoolean(int index) {
		return Boolean.parseBoolean(line[index]);
	}

}
