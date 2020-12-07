package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.ApiAuthTable;
import atsb.eve.db.WalletJournalTable;
import atsb.eve.db.WalletTransactionTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.WalletApiWrapper;
import atsb.eve.dirt.esi.auth.OAuthUtil;
import atsb.eve.model.OAuthUser;
import atsb.eve.model.WalletJournalEntry;
import atsb.eve.model.WalletTransaction;
import net.evetech.ApiException;
import net.evetech.esi.models.GetCharactersCharacterIdWalletJournal200Ok;
import net.evetech.esi.models.GetCharactersCharacterIdWalletTransactions200Ok;

/**
 * Task to retrieve wallet info for a character.
 * 
 * @author austin
 */
public class WalletTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int charId;

	public WalletTask(int charId) {
		this.charId = charId;
	}

	@Override
	public String getTaskName() {
		return "wallet-" + charId;
	}

	@Override
	protected void runTask() {
		WalletApiWrapper wapiw = new WalletApiWrapper(getDb());

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

		log.debug("Retrieving wallet transactions for character " + charId);
		getTransactions(wapiw, auth);
		log.debug("Retrieving wallet journal entries for character " + charId);
		getJournalEntries(wapiw, auth);
	}

	private void getTransactions(WalletApiWrapper wapiw, OAuthUser oau) {
		Long beforeTrans = null;
		boolean result = true;
		do {
			List<GetCharactersCharacterIdWalletTransactions200Ok> ats;
			try {
				ats = wapiw.getWalletTransactions(charId, beforeTrans, OAuthUtil.getAuthToken(getDb(), oau));
				log.debug("Retrieved " + ats.size() + " wallet transactions");
			} catch (ApiException e) {
				log.fatal("Failed to query wallet transactions: " + e.getLocalizedMessage());
				log.debug(e);
				break;
			}
			if (ats.isEmpty()) {
				break;
			}
			List<WalletTransaction> ts = new ArrayList<WalletTransaction>(ats.size());
			for (GetCharactersCharacterIdWalletTransactions200Ok at : ats) {
				WalletTransaction t = TypeUtil.convert(at);
				t.setCharId(charId);
				ts.add(t);
			}
			beforeTrans = ts.get(ts.size() - 1).getTransactionId();
			result = WalletTransactionTable.insertMany(getDb(), ts);
		} while(result);
	}

	private void getJournalEntries(WalletApiWrapper wapiw, OAuthUser oau) {
		boolean result = true;
		int page = 1;
		do {
			List<GetCharactersCharacterIdWalletJournal200Ok> ajs;
			try {
				ajs = wapiw.getWalletJournal(charId, page, OAuthUtil.getAuthToken(getDb(), oau));
				log.debug("Retrieved " + ajs.size() + " wallet journal entries");
			} catch (ApiException e) {
				log.error("Failed to retrieve page " + page + " of wallet journal for character " + charId + ": " + e.getLocalizedMessage());
				log.debug(e);
				break;
			}
			if (ajs.isEmpty()) {
				break;
			}
			List<WalletJournalEntry> js = new ArrayList<WalletJournalEntry>(ajs.size());
			for (GetCharactersCharacterIdWalletJournal200Ok aj : ajs) {
				WalletJournalEntry j = TypeUtil.convert(aj);
				j.setCharId(charId);
				js.add(j);
			}
			result = WalletJournalTable.insertMany(getDb(), js);
			page++;
		} while(result);
	}

}
