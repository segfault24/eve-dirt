package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import atsb.eve.dirt.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.UniverseApi;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;

public class UniverseApiWrapper {

	private Connection db;
	private UniverseApi uapi;

	public UniverseApiWrapper(Connection db) {
		this.db = db;
		uapi = new UniverseApi();
	}

	public List<Long> getUniverseStructures() throws ApiException {
		String etag = Utils.getEtag(db, "universe-structures");
		ApiResponse<List<Long>> resp = uapi.getUniverseStructuresWithHttpInfo(Utils.getApiDatasource(), etag);
		Utils.upsertEtag(db, "universe-structures", Utils.getEtag(resp));
		return resp.getData();
	}

	public GetUniverseStructuresStructureIdOk getUniverseStructuresStructureId(Long structureId,
			String token) throws ApiException {
		String etag = Utils.getEtag(db, "universe-structures-" + structureId);
		ApiResponse<GetUniverseStructuresStructureIdOk> resp = uapi.getUniverseStructuresStructureIdWithHttpInfo(
				structureId, Utils.getApiDatasource(), etag, token);
		Utils.upsertEtag(db, "universe-structures-" + structureId, Utils.getEtag(resp));
		return resp.getData();
	}

}
