package atsb.eve.dirt.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.ApiAuthTable;
import atsb.eve.db.MarketOrderTable;
import atsb.eve.db.StructAuthTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.MarketApiWrapper;
import atsb.eve.dirt.esi.auth.OAuthUtil;
import atsb.eve.model.MarketOrder;
import atsb.eve.model.OAuthUser;
import atsb.eve.util.Utils;
import net.evetech.ApiException;
import net.evetech.esi.models.GetMarketsRegionIdOrders200Ok;
import net.evetech.esi.models.GetMarketsStructuresStructureId200Ok;

/**
 * Task to get bulk market orders by region, public and structure
 * 
 * @author austin
 */
public class MarketRegionOrdersTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private static final int FORBIDDEN_RETRIES = 5;

	private int region;
	private Timestamp start;

	public MarketRegionOrdersTask(int region) {
		this.region = region;
	}

	@Override
	public String getTaskName() {
		return "market-region-orders-" + region;
	}

	@Override
	protected void runTask() {

		// when ESI returns 304 Not Modified, we touch the retrieved timestamp on orders
		// for that region/structure so that the final delete query doesn't remove them

		start = new Timestamp(System.currentTimeMillis() - 15000); // 15 sec fudge factor
		// get publicly available orders in this region
		doPublicOrders();
		// get orders in structures in this region
		doStructOrders();
		// delete old orders in this region
		doDeleteOld();
	}

	private void doPublicOrders() {
		// iterate through the pages
		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetMarketsRegionIdOrders200Ok> orders = new ArrayList<>();
		int page = 0;
		int totalOrders = 0;
		do {
			page++;
			Timestamp now = new Timestamp(System.currentTimeMillis());
			try {
				orders = mapiw.getMarketsRegionIdOrders(region, page);
			} catch (ApiException e) {
				if (e.getCode() == 304) {
					break;
				} else {
					log.error("Failed to retrieve page " + page + " of orders for region " + region + ": " + e.getLocalizedMessage());
					log.debug(e);
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
				MarketOrder m = TypeUtil.convert(o);
				m.setRegion(region);
				m.setRetrieved(now);
				l.add(m);
			}
			try {
				getDb().setAutoCommit(false);
				getDb().setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				MarketOrderTable.upsertMany(getDb(), l);
				getDb().commit();
				getDb().setAutoCommit(true);
				log.debug("Inserted " + orders.size() + " orders for region " + region + " page " + page);
			} catch (SQLException e) {
				log.error("Unexpected failure while processing page " + page + " for region " + region + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
		} while (orders.size() > 0);

		log.debug("Inserted " + totalOrders + " total orders for region " + region);
	}

	private void doStructOrders() {
		// get struct ids in this region that have at least 1 associated auth key
		List<Long> structs;
		try {
			structs = StructAuthTable.getStructIdByRegion(getDb(), region);
		} catch (SQLException e) {
			log.fatal("Failed to search for authed structures for region " + region + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}
		if (structs.isEmpty()) {
			log.debug("No authed structures for region " + region);
			return;
		}

		for (Long structId : structs) {
			doStructOrder(structId);
		}
	}

	private void doStructOrder(long structId) {
		Timestamp now = new Timestamp(System.currentTimeMillis());

		// get auth key ids associated with this struct
		List<Integer> keys;
		try {
			keys = StructAuthTable.getAuthKeyByStruct(getDb(), structId);
		} catch (SQLException e) {
			log.error("Failed to get auth key id for structure " + structId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}
		if (keys == null || keys.isEmpty()) {
			log.error("No auth key id for structure " + structId);
			return;
		}
		int keyId = keys.get(0);

		// get the auth token
		OAuthUser auth;
		try {
			auth = ApiAuthTable.getUserByKeyId(getDb(), keyId);
		} catch (SQLException e) {
			log.error("Failed to get auth details for key " + keyId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}
		if (auth == null) {
			log.error("No auth details found for key " + keyId);
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
				orders = mapiw.getMarketsStructureIdOrders(structId, page, OAuthUtil.getAuthToken(getDb(), auth));
				Utils.putKV(getDb(), "forbidden-" + structId, "0"); // reset forbidden retry counter
			} catch (ApiException e) {
				if (e.getCode() == 304) {
					break;
				} else if (e.getCode() == 403) {
					forbidden(structId, keyId);
					break;
				} else {
					log.error("Failed to retrieve page " + page + " of orders for structure " + structId + ": " + e.getLocalizedMessage());
					log.debug(e);
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
				MarketOrder m = TypeUtil.convert(o);
				m.setRegion(region);
				m.setRetrieved(now);
				l.add(m);
			}
			try {
				getDb().setAutoCommit(false);
				getDb().setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				MarketOrderTable.upsertMany(getDb(), l);
				getDb().commit();
				getDb().setAutoCommit(true);
				log.debug("Inserted " + orders.size() + " orders for structure " + structId + " page " + page);
			} catch (SQLException e) {
				log.error("Unexpected failure while processing page " + page + " for structure " + structId + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
		} while (orders.size() > 0);

		log.debug("Inserted " + totalOrders + " total orders for structure " + structId);
	}

	private void forbidden(long structId, int keyId) {
		// TODO: try other keys
		String scount = Utils.getKV(getDb(), "forbidden-" + structId);
		int count = 0;
		if (scount != null) {
			try {
				count = Integer.parseInt(scount);
			} catch(NumberFormatException e) {
				log.warn("Failed to parse value for kv key: forbidden-" + structId);
			}
		}
		count++; // increment for this attempt

		log.warn("Access forbidden to structure " + structId + " (attempt " + count + ")");

		if (count > FORBIDDEN_RETRIES) {
			// reached limit, remove key
			try {
				log.warn("Reached retry limit for forbidden, removing structAuth for struct " + structId + " and key " + keyId);
				StructAuthTable.deleteStructAuth(getDb(), structId, keyId);
			} catch (SQLException e) {
				log.error("Failed to remove structAuth for structure " + structId + " with key " + keyId + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
		}
		Utils.putKV(getDb(), "forbidden-" + structId, "" + count);
	}

	private void doDeleteOld() {
		try {
			getDb().setAutoCommit(false);
			getDb().setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			int count = MarketOrderTable.deleteOldOrdersByRegion(getDb(), region, start);
			getDb().commit();
			getDb().setAutoCommit(true);
			log.debug("Deleted " + count + " old market orders for region " + region);
		} catch (SQLException e) {
			log.fatal("Failed to delete old market orders for region " + region + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}
	}

}
