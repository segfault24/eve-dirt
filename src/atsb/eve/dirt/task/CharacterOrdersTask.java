package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.ApiAuthTable;
import atsb.eve.db.CharOrderTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.MarketApiWrapper;
import atsb.eve.dirt.esi.auth.OAuthUtil;
import atsb.eve.model.CharOrder;
import atsb.eve.model.OAuthUser;
import net.evetech.ApiException;
import net.evetech.esi.models.GetCharactersCharacterIdOrders200Ok;

/**
 * Task to retrieve order information for a character.
 * 
 * @author austin
 */
public class CharacterOrdersTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int charId;

	public CharacterOrdersTask(int charId) {
		this.charId = charId;
	}

	@Override
	public String getTaskName() {
		return "character-orders-" + charId;
	}

	@Override
	protected void runTask() {
		Timestamp deleteBefore = new Timestamp(System.currentTimeMillis() - 5 * 60 * 1000);
		Timestamp now = new Timestamp(System.currentTimeMillis());

		// get api auth info
		OAuthUser auth;
		try {
			auth = ApiAuthTable.getUserByCharId(getDb(), charId);
			if (auth == null) {
				log.fatal("No auth details found for char=" + charId);
				return;
			}
		} catch (SQLException e) {
			log.fatal("Failed to get auth details for char=" + charId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}

		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetCharactersCharacterIdOrders200Ok> aos;
		try {
			aos = mapiw.getMarketsCharacterIdOrders(charId, OAuthUtil.getAuthToken(getDb(), auth));
			log.debug("Retrieved " + aos.size() + " character orders");
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.fatal("Failed to query character orders: " + e.getLocalizedMessage());
				log.debug(e);
			}
			return;
		}
		List<CharOrder> os = new ArrayList<CharOrder>(aos.size());
		for (GetCharactersCharacterIdOrders200Ok ao : aos) {
			CharOrder o = TypeUtil.convert(ao);
			o.setRetrieved(now);
			o.setCharId(charId);
			os.add(o);
		}
		try {
			getDb().setAutoCommit(false);
			CharOrderTable.upsertMany(getDb(), os);
			getDb().commit();
			getDb().setAutoCommit(true);
			log.debug("Inserted " + os.size() + " orders for character " + charId);
		} catch (SQLException e) {
			log.error("Unexpected failure while processing orders for character " + charId + ": " + e.getLocalizedMessage());
			log.debug(e);
		}

		// delete old orders (where 'retrieved' is older than 'now')
		try {
			int count = CharOrderTable.deleteOldOrdersByChar(getDb(), charId, deleteBefore);
			log.debug("Deleted " + count + " old market orders for character " + charId);
		} catch (SQLException e) {
			log.fatal("Failed to delete old market orders for character " + charId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}
	}

}
