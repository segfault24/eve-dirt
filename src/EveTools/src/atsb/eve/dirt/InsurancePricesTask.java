package atsb.eve.dirt;

import net.evetech.ApiException;
import net.evetech.esi.InsuranceApi;
import net.evetech.esi.models.GetInsurancePrices200Ok;
import net.evetech.esi.models.GetInsurancePricesLevel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import atsb.eve.dirt.util.DbInfo;
import atsb.eve.dirt.util.Utils;

/**
 * Task to pull all ship insurance prices and payouts.
 * 
 * @author austin
 */
public class InsurancePricesTask implements Runnable {

	private static Logger logger = Logger.getLogger(InsurancePricesTask.class
			.toString());
	private static String DELETE_SQL = "TRUNCATE TABLE `insurancePrice`";
	private static String INSERT_SQL = "INSERT INTO insurancePrice (`typeId`,`name`,`cost`,`payout`) VALUES (?,?,?,?)";

	private DbInfo dbInfo;
	private Connection db;

	public InsurancePricesTask(DbInfo dbInfo) {
		this.dbInfo = dbInfo;
	}

	@Override
	public void run() {
		try {
			db = DriverManager.getConnection(dbInfo.getDbConnectionString(),
					dbInfo.getUser(), dbInfo.getPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: "
					+ e.getLocalizedMessage());
			return;
		}

		List<GetInsurancePrices200Ok> prices = getInsurancePrices();
		logger.log(Level.INFO, "Retrieved " + prices.size()
				+ " insurance price records");

		try {
			db.setAutoCommit(false);

			PreparedStatement stmt = db.prepareStatement(DELETE_SQL);
			stmt.execute();
			Utils.closeQuietly(stmt);

			stmt = db.prepareStatement(INSERT_SQL);
			for (GetInsurancePrices200Ok item : prices) {
				for (GetInsurancePricesLevel level : item.getLevels()) {
					stmt.setInt(1, item.getTypeId());
					stmt.setString(2, level.getName());
					stmt.setFloat(3, level.getCost());
					stmt.setFloat(4, level.getPayout());
					stmt.addBatch();
				}
			}
			stmt.executeBatch();

			db.commit();
			db.setAutoCommit(true);
			logger.log(Level.INFO, "Inserted " + prices.size()
					+ " insurance price records");
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Unexpected failure while processing insurance pricing", e);
		}

		Utils.closeQuietly(db);
	}

	private List<GetInsurancePrices200Ok> getInsurancePrices() {
		InsuranceApi iapi = new InsuranceApi();
		List<GetInsurancePrices200Ok> items = new ArrayList<GetInsurancePrices200Ok>();
		try {
			items = iapi.getInsurancePrices(null, "tranquility", null, null);
		} catch (ApiException e) {
			logger.log(Level.WARNING,
					"Failed to retrieve list of insurance prices", e);
		}
		return items;
	}

}
