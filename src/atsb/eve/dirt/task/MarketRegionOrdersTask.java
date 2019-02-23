package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.MarketOrderDb;
import atsb.eve.dirt.esi.MarketApiWrapper;
import atsb.eve.dirt.model.MarketOrder;

import net.evetech.ApiException;
import net.evetech.esi.models.GetMarketsRegionIdOrders200Ok;

/**
 * Task to get bulk market orders by region.
 * 
 * @author austin
 */
public class MarketRegionOrdersTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int region;

	public MarketRegionOrdersTask(int region) {
		this.region = region;
	}

	@Override
	public String getTaskName() {
		return "market-region-orders-" + region;
	}

	@Override
	protected void runTask() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		try {
			MarketOrderDb.deleteRegion(getDb(), region);
		} catch (SQLException e) {
			log.fatal("Unexpected failure while processing region " + region, e);
			return;
		}

		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetMarketsRegionIdOrders200Ok> orders = new ArrayList<>();
		int page = 1;
		do {
			try {
				orders = mapiw.getMarketsRegionIdOrders(region, page);
			} catch (ApiException e) {
				log.error("Failed to retrieve page " + page + " of orders for region " + region);
				break;
			}
			log.debug("Retrieved " + orders.size() + " orders for region " + region + " page " + page);

			List<MarketOrder> l = new ArrayList<MarketOrder>(orders.size());
			for (GetMarketsRegionIdOrders200Ok o : orders) {
				MarketOrder m = new MarketOrder(o);
				m.setRegion(region);
				m.setRetrieved(now);
				l.add(m);
			}
			try {
				MarketOrderDb.insertMany(getDb(), l);
				log.debug("Inserted " + orders.size() + " orders for region " + region + " page " + page);
			} catch (SQLException e) {
				log.error("Unexpected failure while processing page " + page + " for region " + region, e);
			}

			page++;
		} while (orders.size() > 0);
		log.debug("Retrieved " + page + " pages for region " + region);
	}

}
