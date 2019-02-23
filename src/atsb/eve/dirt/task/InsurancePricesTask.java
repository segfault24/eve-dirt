package atsb.eve.dirt.task;

import net.evetech.ApiException;
import net.evetech.esi.models.GetInsurancePrices200Ok;
import net.evetech.esi.models.GetInsurancePricesLevel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.esi.InsuranceApiWrapper;
import atsb.eve.dirt.util.Utils;

/**
 * Task to pull all ship insurance prices and payouts.
 * 
 * @author austin
 */
public class InsurancePricesTask extends DirtTask {

	private static Logger log = LogManager.getLogger();
	private static String DELETE_SQL = "TRUNCATE TABLE `insurancePrice`";
	private static String INSERT_SQL = "INSERT INTO insurancePrice (`typeId`,`name`,`cost`,`payout`) VALUES (?,?,?,?)";

	@Override
	public String getTaskName() {
		return "insurance-prices";
	}

	@Override
	public void runTask() {
		List<GetInsurancePrices200Ok> prices = getInsurancePrices();
		log.debug("Retrieved " + prices.size() + " insurance price records");

		try {
			getDb().setAutoCommit(false);

			PreparedStatement stmt = getDb().prepareStatement(DELETE_SQL);
			stmt.execute();
			Utils.closeQuietly(stmt);

			stmt = getDb().prepareStatement(INSERT_SQL);
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

			getDb().commit();
			getDb().setAutoCommit(true);
			log.debug("Inserted " + prices.size() + " insurance price records");
		} catch (SQLException e) {
			log.fatal("Unexpected failure while processing insurance prices", e);
		}
	}

	private List<GetInsurancePrices200Ok> getInsurancePrices() {
		InsuranceApiWrapper iapiw = new InsuranceApiWrapper(getDb());
		List<GetInsurancePrices200Ok> items = new ArrayList<GetInsurancePrices200Ok>();
		try {
			items = iapiw.getInsurancePrices();
		} catch (ApiException e) {
			log.error("Failed to retrieve list of insurance prices", e);
		}
		return items;
	}

}
