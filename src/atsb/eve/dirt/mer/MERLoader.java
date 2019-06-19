package atsb.eve.dirt.mer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import atsb.eve.util.DbInfo;
import atsb.eve.util.Utils;

/**
 * @author austin
 *
 */
public class MERLoader {

	private static Logger log = LogManager.getLogger();

	private DbInfo dbInfo;

	private Timestamp monthYear;
	private String db;
	private ArrayList<FieldMapping> cols;
	private String SQL_STATEMENT;

	public MERLoader(String yrmo, String configFile) throws Exception, IOException {
		dbInfo = new DbInfo();
		parseYearMonth(yrmo);
		loadConfig(configFile);
		genSqlStatement();
		log.trace(SQL_STATEMENT);
	}

	private void parseYearMonth(String yrmo) {
		DateTime begOfMonth = DateTime.parse(yrmo);
		DateTime endOfMonth = begOfMonth.plusMonths(1).minusDays(1);
		monthYear = new Timestamp(endOfMonth.getMillis());
	}

	private void loadConfig(String configFile) throws Exception, IOException {

		Properties cfg = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(configFile));
			cfg.load(fis);
		} catch (IOException e) {
			throw new RuntimeException("Could not read config file");
		} finally {
			Utils.closeQuietly(fis);
		}

		db = cfg.getProperty("db");
		if (db == null || db.isEmpty()) {
			throw new Exception("Property 'db' is required, but was not found.");
		}
		log.debug("db: " + db);

		cols = new ArrayList<FieldMapping>();
		Enumeration<?> e = cfg.propertyNames();

		while (e.hasMoreElements()) {
			String sqlColumn = (String) e.nextElement();

			if (sqlColumn.equalsIgnoreCase("db")) {
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
		String cmd = "INSERT IGNORE INTO `" + db;
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

	public void doImport(String csvFilePath) throws FileNotFoundException, IOException, CSVException {
		Connection con;
		try {
			con = DriverManager.getConnection(dbInfo.getDbConnectionString(), dbInfo.getUser(), dbInfo.getPass());
		} catch (SQLException e) {
			log.fatal("Failed to open database connection: " + e.getLocalizedMessage());
			return;
		}

		CSVParser csv = new CSVParser(new File(csvFilePath), true);
		try {
			con.setAutoCommit(false);
			PreparedStatement stmt = con.prepareStatement(SQL_STATEMENT);

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
								} else {
									log.warn("How are you seeing this message... oh god what did you do");
								}
							} catch (NumberFormatException e) {
								log.warn("Failed to parse as type '" + col.type().type().toString()
										+ "': " + e.getLocalizedMessage());
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

			con.commit();
			con.setAutoCommit(true);
			log.debug("Inserted " + count + " records");
		} catch (SQLException e) {
			log.warn("Unexpected failure while processing records", e);
		}

		Utils.closeQuietly(con);
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("This program requires at least three arguments.");
			System.out.println("  Usage:");
			System.out.println("    ./load-mer.sh  <year-month>  <config>  <csv1>  ...");
			System.out.println("  Example:");
			System.out.println("    ./load-mer.sh  2017-07  regstat.config  RegionalStats.csv");
			System.exit(1);
		}

		try {
			MERLoader loader = new MERLoader(args[0], args[1]);
			for (int i = 2; i < args.length; i++) {
				loader.doImport(args[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
