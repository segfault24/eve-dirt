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

public class IskVolumeImporter {

	private static Logger logger = Logger.getLogger(IskVolumeImporter.class
			.toString());

	private static final String INSERT_SQL = "INSERT INTO merIskVolume (`date`,`iskVolume`) VALUES (?,?) ON DUPLICATE KEY UPDATE `iskVolume`=VALUES(`iskVolume`)";

	private Config config;

	public IskVolumeImporter() {
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
			logger.log(Level.INFO, "Inserted " + count + " isk volume records");
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Unexpected failure while processing isk volume records", e);
		}

		Utils.closeQuietly(con);
	}

	public static void main(String[] args) {
		IskVolumeImporter ivi = new IskVolumeImporter();
		try {
			ivi.doImport("/home/austin/tmp/IskVolume.csv");
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage());
		}
	}
}
