package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.Stats;
import atsb.eve.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.WalletApi;
import net.evetech.esi.models.GetCharactersCharacterIdWalletJournal200Ok;
import net.evetech.esi.models.GetCharactersCharacterIdWalletTransactions200Ok;

public class WalletApiWrapper {

	private static Logger log = LogManager.getLogger();

	private WalletApi wapi;

	public WalletApiWrapper(Connection db) {
		wapi = new WalletApi();
	}

	public List<GetCharactersCharacterIdWalletTransactions200Ok> getWalletTransactions(int charId, Long beforeTrans,
			String token) throws ApiException {
		Stats.esiCalls++;
		log.trace("Executing API query getWalletTransactions()");
		ApiResponse<List<GetCharactersCharacterIdWalletTransactions200Ok>> resp;
		resp = wapi.getCharactersCharacterIdWalletTransactionsWithHttpInfo(charId, Utils.getApiDatasource(),
				beforeTrans, null, token);
		log.trace("API query returned status code " + resp.getStatusCode());
		return resp.getData();
	}

	public List<GetCharactersCharacterIdWalletJournal200Ok> getWalletJournal(int charId, int page, String token)
			throws ApiException {
		Stats.esiCalls++;
		log.trace("Executing API query getWalletJournal()");
		ApiResponse<List<GetCharactersCharacterIdWalletJournal200Ok>> resp;
		resp = wapi.getCharactersCharacterIdWalletJournalWithHttpInfo(charId, Utils.getApiDatasource(), null, page,
				token);
		log.trace("API query returned status code " + resp.getStatusCode());
		return resp.getData();
	}

}
