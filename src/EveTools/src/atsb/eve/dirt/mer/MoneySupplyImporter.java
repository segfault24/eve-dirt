package atsb.eve.dirt.mer;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import atsb.eve.dirt.Config;
import atsb.eve.dirt.Utils;

public class MoneySupplyImporter {

	private static Logger logger = Logger.getLogger(MoneySupplyImporter.class
			.toString());

	private static final String INSERT_SQL = "INSERT INTO merMoneySupply (`date`,`character`,`corporation`,`total`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `date`=`date`";

	private Config config;

	public MoneySupplyImporter() {
		this.config = new Config();
	}

	public void doImport(String file) throws FileNotFoundException {
		Connection con;
		try {
			con = DriverManager.getConnection(config.getDbConnectionString(),
					config.getDbUser(), config.getDbPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: "
					+ e.getLocalizedMessage());
			return;
		}

		CSVParser csv = new CSVParser(new File(file));

		try {
			con.setAutoCommit(false);

			PreparedStatement stmt = con.prepareStatement(INSERT_SQL);
			int count = 0;
			while (csv.next()) {
				try {
					stmt.setTimestamp(1, new Timestamp(csv.getDate(0)
							.getMillis()));
					stmt.setLong(2, csv.getDouble(1).longValue());
					stmt.setLong(3, csv.getDouble(2).longValue());
					stmt.setLong(4, csv.getDouble(3).longValue());
				} catch (NumberFormatException e) {
					logger.log(Level.SEVERE,
							"bad number spec:" + e.getLocalizedMessage());
				} catch (IndexOutOfBoundsException e) {
					logger.log(Level.SEVERE, "bad index");
				}
				stmt.addBatch();
				count++;
			}
			stmt.executeBatch();

			con.commit();
			con.setAutoCommit(true);
			logger.log(Level.INFO, "Inserted " + count + " money supply records");
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Unexpected failure while processing money supply records", e);
		}

		Utils.closeQuietly(con);
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			logger.log(Level.WARNING,
					"This program requires one argument, no more, no less.");
			return;
		}

		MoneySupplyImporter msi = new MoneySupplyImporter();
		try {
			msi.doImport(args[0]);
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage());
		}
	}
}
