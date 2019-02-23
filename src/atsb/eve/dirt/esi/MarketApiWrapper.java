package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import atsb.eve.dirt.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.MarketApi;
import net.evetech.esi.models.GetMarketsRegionIdHistory200Ok;
import net.evetech.esi.models.GetMarketsRegionIdOrders200Ok;
import net.evetech.esi.models.GetMarketsStructuresStructureId200Ok;

public class MarketApiWrapper {

	private Connection db;
	private MarketApi mapi;

	public MarketApiWrapper(Connection db) {
		this.db = db;
		mapi = new MarketApi();
	}

	public List<GetMarketsRegionIdOrders200Ok> getMarketsRegionIdOrders(int regionId, int page) throws ApiException {
		String etag = Utils.getEtag(db, "orders-" + regionId + "-" + page);
		ApiResponse<List<GetMarketsRegionIdOrders200Ok>> resp = mapi.getMarketsRegionIdOrdersWithHttpInfo("all",
				regionId, Utils.getApiDatasource(), etag, page, null);
		Utils.upsertEtag(db, "orders-" + regionId + "-" + page, Utils.getEtag(resp));
		return resp.getData();
	}

	public List<GetMarketsRegionIdHistory200Ok> getMarketsRegionIdHistory(int regionId, int typeId)
			throws ApiException {
		String etag = Utils.getEtag(db, "history-" + regionId + "-" + typeId);
		ApiResponse<List<GetMarketsRegionIdHistory200Ok>> resp = mapi.getMarketsRegionIdHistoryWithHttpInfo(regionId,
				typeId, Utils.getApiDatasource(), etag);
		Utils.upsertEtag(db, "history-" + regionId + "-" + typeId, Utils.getEtag(resp));
		return resp.getData();
	}

	public List<GetMarketsStructuresStructureId200Ok> get(long structId, int page, String token) throws ApiException {
		String etag = Utils.getEtag(db, "orders-" + structId + "-" + page);
		ApiResponse<List<GetMarketsStructuresStructureId200Ok>> resp = mapi.getMarketsStructuresStructureIdWithHttpInfo(structId, Utils.getApiDatasource(), etag, page, token);
		Utils.upsertEtag(db, "orders-" + structId + "-" + page, Utils.getEtag(resp));
		return resp.getData();
	}
}
