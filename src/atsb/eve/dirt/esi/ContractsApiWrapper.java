package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.Stats;
import atsb.eve.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.ContractsApi;
import net.evetech.esi.models.GetCharactersCharacterIdContracts200Ok;
import net.evetech.esi.models.GetCharactersCharacterIdContractsContractIdItems200Ok;
import net.evetech.esi.models.GetCorporationsCorporationIdContracts200Ok;
import net.evetech.esi.models.GetCorporationsCorporationIdContractsContractIdItems200Ok;

public class ContractsApiWrapper {

	private static Logger log = LogManager.getLogger();

	private Connection db;
	private ContractsApi capi;

	public ContractsApiWrapper(Connection db) {
		this.db = db;
		capi = new ContractsApi();
	}

	public List<GetCharactersCharacterIdContracts200Ok> getCharacterContracts(int charId, int page, String token)
			throws ApiException {
		String etag = Utils.getEtag(db, "char-contract-" + charId + "-" + page);
		log.trace("Executing API query getCharacterContracts(" + charId + ", " + page + ")");
		ApiResponse<List<GetCharactersCharacterIdContracts200Ok>> resp;
		try {
			Stats.esiCalls++;
			resp = capi.getCharactersCharacterIdContractsWithHttpInfo(charId, Utils.getApiDatasource(), etag, page,
					token);
		} catch (ApiException e) {
			Stats.esiErrors++;
			throw e;
		}
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "char-contract-" + charId + "-" + page, etag);
		return resp.getData();
	}

	public List<GetCharactersCharacterIdContractsContractIdItems200Ok> getCharacterContractItems(int charId,
			int contractId, String token) throws ApiException {
		log.trace("Executing API query getCharacterContractItems(" + charId + ", " + contractId + ")");
		ApiResponse<List<GetCharactersCharacterIdContractsContractIdItems200Ok>> resp;
		try {
			Stats.esiCalls++;
			resp = capi.getCharactersCharacterIdContractsContractIdItemsWithHttpInfo(charId, contractId, Utils.getApiDatasource(), null, token);
		} catch(ApiException e) {
			Stats.esiErrors++;
			throw e;
		}
		log.trace("API query returned status code " + resp.getStatusCode());
		return resp.getData();
	}

	public List<GetCorporationsCorporationIdContracts200Ok> getCorporationContracts(int corpId, int page, String token)
			throws ApiException {
		String etag = Utils.getEtag(db, "corp-contract-" + corpId + "-" + page);
		log.trace("Executing API query getCorporationContracts(" + corpId + ", " + page + ")");
		ApiResponse<List<GetCorporationsCorporationIdContracts200Ok>> resp;
		try {
			Stats.esiCalls++;
			resp = capi.getCorporationsCorporationIdContractsWithHttpInfo(corpId, Utils.getApiDatasource(), etag, page,
					token);
		} catch (ApiException e) {
			Stats.esiErrors++;
			throw e;
		}
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "corp-contract-" + corpId + "-" + page, etag);
		return resp.getData();
	}

	public List<GetCorporationsCorporationIdContractsContractIdItems200Ok> getCorporationContractItems(int corpId,
			int contractId, String token) throws ApiException {
		log.trace("Executing API query getCorporationContractItems(" + corpId + ", " + contractId + ")");
		ApiResponse<List<GetCorporationsCorporationIdContractsContractIdItems200Ok>> resp;
		try {
			Stats.esiCalls++;
			resp = capi.getCorporationsCorporationIdContractsContractIdItemsWithHttpInfo(contractId, corpId, Utils.getApiDatasource(), null, token);
		} catch(ApiException e) {
			Stats.esiErrors++;
			throw e;
		}
		log.trace("API query returned status code " + resp.getStatusCode());
		return resp.getData();
	}

}
