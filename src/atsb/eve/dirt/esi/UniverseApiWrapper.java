package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.Stats;
import atsb.eve.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.UniverseApi;
import net.evetech.esi.models.GetUniverseConstellationsConstellationIdOk;
import net.evetech.esi.models.GetUniverseRegionsRegionIdOk;
import net.evetech.esi.models.GetUniverseStationsStationIdOk;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;
import net.evetech.esi.models.GetUniverseSystemsSystemIdOk;
import net.evetech.esi.models.GetUniverseTypesTypeIdOk;
import net.evetech.esi.models.PostUniverseNames200Ok;

public class UniverseApiWrapper {

	private static Logger log = LogManager.getLogger();

	private Connection db;
	private UniverseApi uapi;

	public UniverseApiWrapper(Connection db) {
		this.db = db;
		uapi = new UniverseApi();
	}

	public List<Long> getUniverseStructures() throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "universe-structures");
		log.trace("Executing API query getUniverseStructures()");
		ApiResponse<List<Long>> resp = uapi.getUniverseStructuresWithHttpInfo(Utils.getApiDatasource(), null, etag);
		log.trace("API query returned status code " + resp.getStatusCode());
		if (!resp.getData().isEmpty()) {
			Utils.upsertEtag(db, "universe-structures", Utils.getEtag(resp.getHeaders()));
		}
		return resp.getData();
	}

	public GetUniverseStructuresStructureIdOk getUniverseStructuresStructureId(Long structId, String token)
			throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "universe-structures-" + structId);
		log.trace("Executing API query getUniverseStructuresStructureId(" + structId + ")");
		ApiResponse<GetUniverseStructuresStructureIdOk> resp = uapi
				.getUniverseStructuresStructureIdWithHttpInfo(structId, Utils.getApiDatasource(), etag, token);
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "universe-structures-" + structId, Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public List<Integer> getUniverseTypes(int page) throws ApiException {
		Stats.esiCalls++;
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
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "inv-type-" + typeId);
		log.trace("Executing API query getUniverseType(" + typeId + ")");
		ApiResponse<GetUniverseTypesTypeIdOk> resp = uapi.getUniverseTypesTypeIdWithHttpInfo(typeId,
				Utils.getApiLanguage(), Utils.getApiDatasource(), etag, Utils.getApiLanguage());
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "inv-type-" + typeId, Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public List<Integer> getUniverseRegions() throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "regions");
		log.trace("Executing API query getUniverseRegions()");
		ApiResponse<List<Integer>> resp = uapi.getUniverseRegionsWithHttpInfo(Utils.getApiDatasource(), etag);
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "regions", Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public GetUniverseRegionsRegionIdOk getUniverseRegion(int regionId) throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "region-" + regionId);
		log.trace("Executing API query getUniverseRegion(" + regionId + ")");
		ApiResponse<GetUniverseRegionsRegionIdOk> resp = uapi.getUniverseRegionsRegionIdWithHttpInfo(regionId,
				Utils.getApiLanguage(), Utils.getApiDatasource(), etag, Utils.getApiLanguage());
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "region-" + regionId, Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public List<Integer> getUniverseConstellations() throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "constellations");
		log.trace("Executing API query getUniverseConstellations()");
		ApiResponse<List<Integer>> resp = uapi.getUniverseConstellationsWithHttpInfo(Utils.getApiDatasource(), etag);
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "constellations", Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public GetUniverseConstellationsConstellationIdOk getUniverseConstellation(int constellationId)
			throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "constellation-" + constellationId);
		log.trace("Executing API query getUniverseConstellation(" + constellationId + ")");
		ApiResponse<GetUniverseConstellationsConstellationIdOk> resp = uapi.getUniverseConstellationsConstellationIdWithHttpInfo(
				constellationId, Utils.getApiLanguage(), Utils.getApiDatasource(), etag, Utils.getApiLanguage());
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "constellation-" + constellationId, Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public List<Integer> getUnverseSolarSystems() throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "solar-systems");
		log.trace("Executing API query getUniverseSolarSystems()");
		ApiResponse<List<Integer>> resp = uapi.getUniverseSystemsWithHttpInfo(Utils.getApiDatasource(), etag);
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "solar-systems", Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public GetUniverseSystemsSystemIdOk getUniverseSolarSystem(int solarSystemId) throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "solar-system-" + solarSystemId);
		log.trace("Executing API query getUniverseSolarSystem(" + solarSystemId + ")");
		ApiResponse<GetUniverseSystemsSystemIdOk> resp = uapi.getUniverseSystemsSystemIdWithHttpInfo(solarSystemId,
				Utils.getApiLanguage(), Utils.getApiDatasource(), etag, Utils.getApiLanguage());
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "solar-system-" + solarSystemId, Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public GetUniverseStationsStationIdOk getUniverseStation(int stationId) throws ApiException {
		Stats.esiCalls++;
		String etag = Utils.getEtag(db, "station-" + stationId);
		log.trace("Executing API query getUniverseStation(" + stationId + ")");
		ApiResponse<GetUniverseStationsStationIdOk> resp = uapi.getUniverseStationsStationIdWithHttpInfo(stationId, Utils.getApiDatasource(),  etag);
		log.trace("API query returned status code " + resp.getStatusCode());
		Utils.upsertEtag(db, "station-" + stationId, Utils.getEtag(resp.getHeaders()));
		return resp.getData();
	}

	public List<PostUniverseNames200Ok> postUniverseNames(List<Integer> ids) throws ApiException {
		Stats.esiCalls++;
		log.trace("Executing API query postUniverseNames(...various...)");
		ApiResponse<List<PostUniverseNames200Ok>> resp = uapi.postUniverseNamesWithHttpInfo(ids, Utils.getApiDatasource());
		log.trace("API query returned status code " + resp.getStatusCode());
		return resp.getData();
	}

}
