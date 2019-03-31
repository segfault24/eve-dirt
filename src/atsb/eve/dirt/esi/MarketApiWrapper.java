package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.MarketApi;
import net.evetech.esi.models.GetMarketsRegionIdHistory200Ok;
import net.evetech.esi.models.GetMarketsRegionIdOrders200Ok;
import net.evetech.esi.models.GetMarketsStructuresStructureId200Ok;

public class MarketApiWrapper {

	private static Logger log = LogManager.getLogger();

	private Connection db;
	private MarketApi mapi;

	public MarketApiWrapper(Connection db) {
		this.db = db;
		mapi = new MarketApi();
	}

	public List<GetMarketsRegionIdOrders200Ok> getMarketsRegionIdOrders(int regionId, int page) throws ApiException {
		String etag = Utils.getEtag(db, "orders-" + regionId + "-" + page);
		log.trace("Executing API query getMarketsRegionIdOrders(" + regionId + ", " + page + ")");
		ApiResponse<List<GetMarketsRegionIdOrders200Ok>> resp = mapi.getMarketsRegionIdOrdersWithHttpInfo("all",
				regionId, Utils.getApiDatasource(), etag, page, null);
		log.trace("API query returned status code " + resp.getStatusCode());
		if (!resp.getData().isEmpty()) {
			Utils.upsertEtag(db, "orders-" + regionId + "-" + page, Utils.getEtag(resp));
		}
		return resp.getData();
	}

	public List<GetMarketsRegionIdHistory200Ok> getMarketsRegionIdHistory(int regionId, int typeId)
			throws ApiException {
		String etag = Utils.getEtag(db, "history-" + regionId + "-" + typeId);
		log.trace("Executing API query getMarketsRegionIdHistory(" + regionId + ", " + typeId + ")");
		ApiResponse<List<GetMarketsRegionIdHistory200Ok>> resp = mapi.getMarketsRegionIdHistoryWithHttpInfo(regionId,
				typeId, Utils.getApiDatasource(), etag);
		log.trace("API query returned status code " + resp.getStatusCode());
		if (!resp.getData().isEmpty()) {
			Utils.upsertEtag(db, "history-" + regionId + "-" + typeId, Utils.getEtag(resp));
		}
		return resp.getData();
	}

	public List<GetMarketsStructuresStructureId200Ok> getMarketsStructuresStructureId(long structId, int page, String token) throws ApiException {
		String etag = Utils.getEtag(db, "orders-" + structId + "-" + page);
		log.trace("Executing API query getMarketsStructureStructureId(" + structId + ", " + page + ")");
		ApiResponse<List<GetMarketsStructuresStructureId200Ok>> resp = mapi.getMarketsStructuresStructureIdWithHttpInfo(structId, Utils.getApiDatasource(), etag, page, token);
		log.trace("API query returned status code " + resp.getStatusCode());
		if (!resp.getData().isEmpty()) {
			Utils.upsertEtag(db, "orders-" + structId + "-" + page, Utils.getEtag(resp));
		}
		return resp.getData();
	}
}
