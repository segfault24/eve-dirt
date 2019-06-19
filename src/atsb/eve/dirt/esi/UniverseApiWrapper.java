package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.UniverseApi;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;
import net.evetech.esi.models.GetUniverseTypesTypeIdOk;

public class UniverseApiWrapper {

	private static Logger log = LogManager.getLogger();

	private Connection db;
	private UniverseApi uapi;

	public UniverseApiWrapper(Connection db) {
		this.db = db;
		uapi = new UniverseApi();
	}

	public List<Long> getUniverseStructures() throws ApiException {
		String etag = Utils.getEtag(db, "universe-structures");
		log.trace("Executing API query getUniverseStructures()");
		ApiResponse<List<Long>> resp = uapi.getUniverseStructuresWithHttpInfo(Utils.getApiDatasource(), null, etag);
		log.trace("API query returned status code " + resp.getStatusCode());
		if (!resp.getData().isEmpty()) {
			Utils.upsertEtag(db, "universe-structures", Utils.getEtag(resp.getHeaders()));
		}
		return resp.getData();
	}

	public GetUniverseStructuresStructureIdOk getUniverseStructuresStructureId(Long structId,
			String token) throws ApiException {
		String etag = Utils.getEtag(db, "universe-structures-" + structId);
		log.trace("Executing API query getUniverseStructuresStructureId(" + structId + ")");
		ApiResponse<GetUniverseStructuresStructureIdOk> resp = uapi.getUniverseStructuresStructureIdWithHttpInfo(
				structId, Utils.getApiDatasource(), etag, token);
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "universe-structures-" + structId, Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public List<Integer> getUniverseTypes(int page) throws ApiException {
		String etag = Utils.getEtag(db, "inv-types-" + page);
		log.trace("Executing API query getUniverseTypes(" + page + ")");
		ApiResponse<List<Integer>> resp = uapi.getUniverseTypesWithHttpInfo(Utils.getApiDatasource(), etag, page);
		log.trace("API query returned status code " + resp.getStatusCode());
		if (!resp.getData().isEmpty()) {
			Utils.upsertEtag(db, "inv-types-" + page, etag);
		}
		return resp.getData();
	}

	public GetUniverseTypesTypeIdOk getUniverseType(int typeId) throws ApiException {
		String etag = Utils.getEtag(db, "inv-type-" + typeId);
		log.trace("Executing API query getUniverseType(" + typeId + ")");
		ApiResponse<GetUniverseTypesTypeIdOk> resp = uapi.getUniverseTypesTypeIdWithHttpInfo(typeId, Utils.getApiLanguage(), Utils.getApiDatasource(), etag, Utils.getApiLanguage());
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "inv-type-" + typeId, Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

}
