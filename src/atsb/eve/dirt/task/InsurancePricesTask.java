package atsb.eve.dirt.task;

import net.evetech.ApiException;
import net.evetech.esi.models.GetInsurancePrices200Ok;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.InsuranceTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.InsuranceApiWrapper;
import atsb.eve.model.InsurancePrice;

/**
 * Task to pull all ship insurance prices and payouts.
 * 
 * @author austin
 */
public class InsurancePricesTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	@Override
	public String getTaskName() {
		return "insurance-prices";
	}

	@Override
	public void runTask() {
		List<InsurancePrice> prices = getInsurancePrices();
		log.debug("Retrieved " + prices.size() + " insurance price records");

		try {
			getDb().setAutoCommit(false);
			InsuranceTable.deleteAll(getDb());
			InsuranceTable.insertMany(getDb(), prices);
			getDb().commit();
			getDb().setAutoCommit(true);
			log.debug("Inserted " + prices.size() + " insurance price records");
		} catch (SQLException e) {
			log.fatal("Unexpected failure while processing insurance prices", e);
		}
	}

	private List<InsurancePrice> getInsurancePrices() {
		InsuranceApiWrapper iapiw = new InsuranceApiWrapper(getDb());
		List<GetInsurancePrices200Ok> items = new ArrayList<GetInsurancePrices200Ok>();
		try {
			items = iapiw.getInsurancePrices();
		} catch (ApiException e) {
			log.error("Failed to retrieve list of insurance prices", e);
		}
		List<InsurancePrice> mine = new ArrayList<InsurancePrice>();
		for (GetInsurancePrices200Ok api : items) {
			mine.add(TypeUtil.convert(api));
		}
		return mine;
	}

}
