package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.MarketOrderTable;
import atsb.eve.dirt.db.StructureTable;
import atsb.eve.dirt.esi.MarketApiWrapper;
import atsb.eve.dirt.model.MarketOrder;
import atsb.eve.dirt.model.OAuthUser;
import atsb.eve.dirt.model.Structure;
import atsb.eve.dirt.util.OAuthUtils;
import atsb.eve.dirt.util.Utils;
import net.evetech.ApiException;
import net.evetech.esi.models.GetMarketsStructuresStructureId200Ok;

/**
 * Task to get bulk market orders by structure.
 * 
 * @author austin
 */
public class MarketStructureOrdersTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private long structId;

	public MarketStructureOrdersTask(long structId) {
		this.structId = structId;
	}

	@Override
	public String getTaskName() {
		return "market-structure-orders-" + structId;
	}

	@Override
	protected void runTask() {
		Timestamp now = new Timestamp(System.currentTimeMillis());

		// TODO: get list of keyIds allowed to query struct orders from dirtStructAuth
		int keyId = Integer.parseInt(Utils.getProperty(getDb(), Utils.PROPERTY_SCRAPER_KEY_ID));

		// get the structure's region id
		// queue a task to get its info if we don't have it
		int regionId = 0;
		Structure s = StructureTable.find(getDb(), structId);
		if (s == null) {
			getDaemon().addTask(new StructureTask(structId));
		} else {
			regionId = s.getRegionId();
		}

		// get the auth token
		OAuthUser auth;
		try {
			auth = OAuthUtils.loadFromSql(getDb(), keyId);
			if (auth == null) {
				log.fatal("No auth details found for key=" + keyId);
				return;
			}
		} catch (Exception e) {
			log.fatal("Failed to get auth details for key=" + keyId, e);
			return;
		}
		String authToken = OAuthUtils.getAuthToken(getDb(), auth);

		// iterate through the pages
		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetMarketsStructuresStructureId200Ok> orders = new ArrayList<>();
		int page = 0;
		int totalOrders = 0;
		do {
			page++;
			try {
				orders = mapiw.getMarketsStructuresStructureId(structId, page, authToken);
			} catch (ApiException e) {
				if (e.getCode() == 304) {
					continue;
				} else {
					log.error("Failed to retrieve page " + page + " of orders for structure " + structId, e);
					break;
				}
			}
			log.debug("Retrieved " + orders.size() + " orders for structure " + structId + " page " + page);
			if (orders.isEmpty()) {
				break;
			}

			totalOrders += orders.size();
			List<MarketOrder> l = new ArrayList<MarketOrder>(orders.size());
			for (GetMarketsStructuresStructureId200Ok o : orders) {
				MarketOrder m = new MarketOrder(o);
				m.setRegion(regionId);
				m.setRetrieved(now);
				l.add(m);
			}
			try {
				getDb().setAutoCommit(false);
				MarketOrderTable.insertMany(getDb(), l);
				getDb().commit();
				getDb().setAutoCommit(true);
				log.debug("Inserted " + orders.size() + " orders for structure " + structId + " page " + page);
			} catch (SQLException e) {
				log.error("Unexpected failure while processing page " + page + " for structure " + structId, e);
			}
		} while (orders.size() > 0);

		// delete old orders (where 'retrieved' is older than 'now')
		if (totalOrders > 0) {
			try {
				MarketOrderTable.deleteOldStructureOrders(getDb(), structId, now);
				log.debug("Cleared old orders for structure " + structId);
			} catch (SQLException e) {
				log.fatal("Unexpected failure while processing structure " + structId, e);
				return;
			}
		}
	}

}
