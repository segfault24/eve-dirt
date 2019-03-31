package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.MarketOrderTable;
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

		// iterate through the pages
		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetMarketsRegionIdOrders200Ok> orders = new ArrayList<>();
		int page = 0;
		int totalOrders = 0;
		do {
			page++;
			try {
				orders = mapiw.getMarketsRegionIdOrders(region, page);
			} catch (ApiException e) {
				if (e.getCode() == 304) {
					continue;
				} else {
					log.error("Failed to retrieve page " + page + " of orders for region " + region, e);
					break;
				}
			}
			log.debug("Retrieved " + orders.size() + " orders for region " + region + " page " + page);
			if (orders.isEmpty()) {
				break;
			}

			totalOrders += orders.size();
			List<MarketOrder> l = new ArrayList<MarketOrder>(orders.size());
			for (GetMarketsRegionIdOrders200Ok o : orders) {
				MarketOrder m = new MarketOrder(o);
				m.setRegion(region);
				m.setRetrieved(now);
				l.add(m);
			}
			try {
				getDb().setAutoCommit(false);
				MarketOrderTable.insertMany(getDb(), l);
				getDb().commit();
				getDb().setAutoCommit(true);
				log.debug("Inserted " + orders.size() + " orders for region " + region + " page " + page);
			} catch (SQLException e) {
				log.error("Unexpected failure while processing page " + page + " for region " + region, e);
			}
		} while (orders.size() > 0);

		// delete old orders (where 'retrieved' is older than 'now')
		// TODO: only remove public orders
		if (totalOrders > 0) {
			try {
				MarketOrderTable.deleteOldPublicRegionOrders(getDb(), region, now);
				log.debug("Cleared old orders for region " + region);
			} catch (SQLException e) {
				log.fatal("Unexpected failure while processing region " + region, e);
				return;
			}
		}
	}

}
