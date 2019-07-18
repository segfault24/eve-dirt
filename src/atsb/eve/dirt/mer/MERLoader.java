package atsb.eve.dirt.mer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.MapTables;
import atsb.eve.model.Region;
import atsb.eve.util.Utils;

/**
 * @author austin
 *
 */
public class MERLoader {

	private static Logger log = LogManager.getLogger();

	private Timestamp monthYear;
	private String table;
	private ArrayList<FieldMapping> cols;
	private String SQL_STATEMENT;

	public MERLoader(LocalDate d, String configFile) throws Exception, IOException {
		monthYear = new Timestamp(d.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000);
		loadConfig(configFile);
		genSqlStatement();
		log.trace(SQL_STATEMENT);
	}

	private void loadConfig(String configFile) throws Exception, IOException {
		Properties cfg = new Properties();
		FileInputStream fis = new FileInputStream(new File(configFile));
		cfg.load(fis);
		Utils.closeQuietly(fis);

		table = cfg.getProperty("table");
		if (table == null || table.isEmpty()) {
			throw new Exception("Property 'table' is required, but was not found.");
		}
		log.debug("table: " + table);

		cols = new ArrayList<FieldMapping>();
		Enumeration<?> e = cfg.propertyNames();

		while (e.hasMoreElements()) {
			String sqlColumn = (String) e.nextElement();

			if (sqlColumn.equalsIgnoreCase("table")) {
				continue;
			}

			String[] v = cfg.getProperty(sqlColumn).split(",");
			if (v.length != 2) {
				throw new Exception("Bad field spec for '" + sqlColumn + "'");
			}

			String csvColumn = v[0];
			MappingType type = MappingType.translate(v[1]);

			cols.add(new FieldMapping(sqlColumn, csvColumn, type));
			log.debug("sqlColumn: " + sqlColumn + ", csvColumn: " + csvColumn + ", type: " + type.toString());
		}
	}

	private void genSqlStatement() {
		String cmd = "INSERT IGNORE INTO `" + table;
		String keys = "";
		String values = "";
		int numCols = cols.size();
		for (int i = 0; i < numCols; i++) {
			FieldMapping col = cols.get(i);
			keys += "`" + col.sqlColumn() + "`";
			if (i < numCols - 1) {
				keys += ",";
			}
			values += "?";
			if (i < numCols - 1) {
				values += ",";
			}
		}
		SQL_STATEMENT = cmd + "` (" + keys + ") VALUES (" + values + ")";
	}

	public void doImport(Connection db, File csvFile) throws FileNotFoundException, IOException, CSVException {
		CSVParser csv = new CSVParser(csvFile, true);
		try {
			db.setAutoCommit(false);
			PreparedStatement stmt = db.prepareStatement(SQL_STATEMENT);

			// for every line in the csv file
			int count = 0;
			while (csv.next()) {
				// look for a value for every sql column
				try {
					for (int i = 0; i < cols.size(); i++) {
						FieldMapping col = cols.get(i);

						if (col.csvColumn().equals("&")) {
							stmt.setTimestamp(i + 1, monthYear);
						} else {
							try {
								if (col.type() == MappingType.LONG) {
									stmt.setLong(i + 1, csv.getLong(col.csvColumn()));
								} else if (col.type() == MappingType.DOUBLE) {
									stmt.setDouble(i + 1, csv.getDouble(col.csvColumn()));
								} else if (col.type() == MappingType.TIMESTAMP) {
									stmt.setTimestamp(i + 1, new Timestamp(csv.getDate(col.csvColumn()).getMillis()));
								} else if (col.type() == MappingType.STRING) {
									stmt.setString(i + 1, csv.getString(col.csvColumn()));
								} else {
									log.warn("How are you seeing this message... oh god what did you do");
								}
							} catch (NumberFormatException e) {
								log.warn("Failed to parse as type '" + col.type().type().toString()
										+ "': " + e.getLocalizedMessage());
							} catch (CSVException e) {
								// regstat only has region names, not ids for some dumb reason
								if (col.sqlColumn().equalsIgnoreCase("regionid")) {
									Region r = MapTables.findRegionByName(db, csv.getString("regionName"));
									stmt.setLong(i + 1, r.getReigonId());
								} else {
									throw e;
								}
							}
						}
					}
				} catch (CSVException e) {
					log.warn("Failure while reading line: " + e.getLocalizedMessage());
					e.printStackTrace();
				}

				// add to the batch
				stmt.addBatch();
				count++;
			}
			stmt.executeBatch();

			db.commit();
			db.setAutoCommit(true);
			log.debug("Inserted " + count + " records");
		} catch (SQLException e) {
			log.warn("Unexpected failure while processing records", e);
		}
	}

}
