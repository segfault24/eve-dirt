package atsb.eve.dirt;

import is.ccp.tech.ApiException;
import is.ccp.tech.esi.InsuranceApi;
import is.ccp.tech.esi.models.GetInsurancePrices200Ok;
import is.ccp.tech.esi.models.GetInsurancePricesLevel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private DaemonProperties config;
	private Connection con;

	public InsurancePricesTask(DaemonProperties cfg) {
		this.config = cfg;
	}

	@Override
	public void run() {
		try {
			con = DriverManager.getConnection(config.getDbConnectionString(),
					config.getDbUser(), config.getDbPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: "
					+ e.getLocalizedMessage());
			return;
		}

		List<GetInsurancePrices200Ok> prices = getInsurancePrices();
		logger.log(Level.INFO, "Retrieved " + prices.size()
				+ " insurance price records");

		try {
			con.setAutoCommit(false);

			PreparedStatement stmt = con.prepareStatement(DELETE_SQL);
			stmt.execute();
			Utils.closeQuietly(stmt);

			stmt = con.prepareStatement(INSERT_SQL);
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

			con.commit();
			con.setAutoCommit(true);
			logger.log(Level.INFO, "Inserted " + prices.size()
					+ " insurance price records");
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Unexpected failure while processing insurance pricing", e);
		}

		Utils.closeQuietly(con);
	}

	private List<GetInsurancePrices200Ok> getInsurancePrices() {
		InsuranceApi iapi = new InsuranceApi();
		List<GetInsurancePrices200Ok> items = new ArrayList<GetInsurancePrices200Ok>();
		try {
			items = iapi.getInsurancePrices("tranquility", "en-us",
					config.getUserAgent(), null);
		} catch (ApiException e) {
			logger.log(Level.WARNING,
					"Failed to retrieve list of insurance prices", e);
		}
		return items;
	}

}
