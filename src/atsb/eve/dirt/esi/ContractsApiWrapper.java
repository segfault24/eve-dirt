package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.ContractsApi;
import net.evetech.esi.models.GetCharactersCharacterIdContracts200Ok;

public class ContractsApiWrapper {

	private static Logger log = LogManager.getLogger();

	private Connection db;
	private ContractsApi capi;

	public ContractsApiWrapper(Connection db) {
		this.db = db;
		capi = new ContractsApi();
	}

	public List<GetCharactersCharacterIdContracts200Ok> getCharacterContracts(int charId, int page, String token) throws ApiException {
		String etag = Utils.getEtag(db, "char-contract-" + charId + "-" + page);
		log.trace("Executing API query getCharacterContracts()");
		ApiResponse<List<GetCharactersCharacterIdContracts200Ok>> resp = capi.getCharactersCharacterIdContractsWithHttpInfo(charId, Utils.getApiDatasource(), etag, page, token);
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "char-contract-" + charId + "-" + page, etag);
		return resp.getData();
	}

}
