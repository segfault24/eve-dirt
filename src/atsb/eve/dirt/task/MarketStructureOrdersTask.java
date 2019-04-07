package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.ApiAuthTable;
import atsb.eve.dirt.db.MarketOrderTable;
import atsb.eve.dirt.db.StructAuthTable;
import atsb.eve.dirt.db.StructureTable;
import atsb.eve.dirt.esi.MarketApiWrapper;
import atsb.eve.dirt.model.MarketOrder;
import atsb.eve.dirt.model.OAuthUser;
import atsb.eve.dirt.model.Structure;
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

		// get auth keys that are authorized to read the structure's market
		List<Integer> keys;
		try {
			keys = StructAuthTable.findAuthKeyByStruct(getDb(), structId);
		} catch (SQLException e1) {
			log.fatal("Failed to search for auth keys for structure " + structId);
			return;
		}
		int keyId;
		if (!keys.isEmpty()) {
			keyId = keys.get(0);
		} else {
			log.fatal("Failed to find any auth keys for structure " + structId);
			return;
		}

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
			auth = ApiAuthTable.getUserByKeyId(getDb(), keyId);
			if (auth == null) {
				log.fatal("No auth details found for key=" + keyId);
				return;
			}
		} catch (SQLException e) {
			log.fatal("Failed to get auth details for key=" + keyId, e);
			return;
		}

		// iterate through the pages
		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetMarketsStructuresStructureId200Ok> orders = new ArrayList<>();
		int page = 0;
		int totalOrders = 0;
		do {
			page++;
			try {
				orders = mapiw.getMarketsStructureIdOrders(structId, page, auth.getAuthToken());
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
				int count = MarketOrderTable.deleteOldStructureOrders(getDb(), structId, now);
				log.debug("Deleted " + count + " old market orders for structure " + structId);
			} catch (SQLException e) {
				log.fatal("Failed to delete old market orders for structure " + structId, e);
				return;
			}
		}
	}

}
