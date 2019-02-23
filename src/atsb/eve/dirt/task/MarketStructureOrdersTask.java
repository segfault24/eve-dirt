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
import atsb.eve.dirt.model.OAuthUser;
import atsb.eve.dirt.util.OAuthUtils;
import net.evetech.ApiException;
import net.evetech.esi.models.GetMarketsStructuresStructureId200Ok;

/**
 * @author austin
 *
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
		int keyId = 1;

		// get the structure's regionid

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

		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetMarketsStructuresStructureId200Ok> orders = new ArrayList<>();
		int page = 1;
		do {
			try {
				orders = mapiw.get(structId, 0, authToken);
			} catch (ApiException e) {
				log.error("Failed to retrieve page " + page + " of orders for strcture " + structId);
				break;
			}
			List<MarketOrder> l = new ArrayList<MarketOrder>(orders.size());
			for (GetMarketsStructuresStructureId200Ok o : orders) {
				MarketOrder m = new MarketOrder(o);
				m.setRegion(0);
				m.setRetrieved(now);
				l.add(m);
			}
			try {
				MarketOrderDb.insertMany(getDb(), l);
				log.debug("Inserted " + orders.size() + " orders for structure " + structId + " page " + page);
			} catch (SQLException e) {
				log.error("Unexpected failure while processing page " + page + " for structure " + structId, e);
			}
		} while (orders.size() > 0);
	}

}
