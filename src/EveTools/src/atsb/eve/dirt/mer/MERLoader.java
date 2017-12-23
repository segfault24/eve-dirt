package atsb.eve.dirt.mer;

import java.io.File;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import atsb.eve.dirt.Utils;

/**
 * @author austin
 *
 */
public class MERLoader {

	private static Logger logger = Logger.getLogger(MERLoader.class.toString());

	private MERLoaderProperties props;

	private Timestamp monthYear;
	private String db;
	private ArrayList<FieldMapping> cols;
	private String SQL_STATEMENT;

	public MERLoader(String yrmo, String configFile) throws Exception,
			IOException {
		props = new MERLoaderProperties();
		parseYearMonth(yrmo);
		loadConfig(configFile);
		genSqlStatement();
		logger.log(Level.FINE, SQL_STATEMENT);
	}

	private void parseYearMonth(String yrmo) {
		DateTime begOfMonth = DateTime.parse(yrmo);
		DateTime endOfMonth = begOfMonth.plusMonths(1).minusDays(1);
		monthYear = new Timestamp(endOfMonth.getMillis());
	}

	private void loadConfig(String configFile) throws Exception, IOException {
		Properties cfg = Utils.readProperties(configFile);
		db = cfg.getProperty("db");
		if (db == null || db.isEmpty()) {
			throw new Exception("Property 'db' is required, but was not found.");
		}
		logger.log(Level.FINE, "db: " + db);

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
			logger.log(Level.FINE, "sqlColumn: " + sqlColumn + ", csvColumn: "
					+ csvColumn + ", type: " + type.toString());
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

	public void doImport(String csvFilePath) throws FileNotFoundException,
			IOException, CSVException {
		Connection con;
		try {
			con = DriverManager.getConnection(props.getDbConnectionString(),
					props.getDbUser(), props.getDbPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: "
					+ e.getLocalizedMessage());
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
									stmt.setLong(i + 1,
											csv.getLong(col.csvColumn()));
								} else if (col.type() == MappingType.DOUBLE) {
									stmt.setDouble(i + 1,
											csv.getDouble(col.csvColumn()));
								} else if (col.type() == MappingType.TIMESTAMP) {
									stmt.setTimestamp(
											i + 1,
											new Timestamp(csv.getDate(
													col.csvColumn()).getMillis()));
								} else {
									logger.log(Level.WARNING,
											"How are you seeing this message... oh god what did you do");
								}
							} catch (NumberFormatException e) {
								logger.log(
										Level.WARNING,
										"Failed to parse as type '"
												+ col.type().type().toString() + "': "
												+ e.getLocalizedMessage());
							}
						}
					}
				} catch (CSVException e) {
					logger.log(Level.WARNING, "Failure while reading line: "
							+ e.getLocalizedMessage());
					e.printStackTrace();
				}

				// add to the batch
				stmt.addBatch();
				count++;
			}
			stmt.executeBatch();

			con.commit();
			con.setAutoCommit(true);
			logger.log(Level.INFO, "Inserted " + count + " records");
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Unexpected failure while processing records", e);
		}

		Utils.closeQuietly(con);
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out
					.println("This program requires at least three arguments.");
			System.out.println("  Usage:");
			System.out
					.println("    ./load-mer.sh  <year-month>  <config>  <csv1>  ...");
			System.out.println("  Example:");
			System.out
					.println("    ./load-mer.sh  2017-07  regstat.config  RegionalStats.csv");
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
