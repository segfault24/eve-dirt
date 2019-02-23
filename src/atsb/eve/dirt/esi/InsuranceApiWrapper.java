package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import atsb.eve.dirt.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.InsuranceApi;
import net.evetech.esi.models.GetInsurancePrices200Ok;

public class InsuranceApiWrapper {

	private Connection db;
	private InsuranceApi iapi;

	public InsuranceApiWrapper(Connection db) {
		this.db = db;
		iapi = new InsuranceApi();
	}

	public List<GetInsurancePrices200Ok> getInsurancePrices() throws ApiException {
		String etag = Utils.getEtag(db, "insurance-prices");
		ApiResponse<List<GetInsurancePrices200Ok>> resp = iapi.getInsurancePricesWithHttpInfo(Utils.getApiLanguage(),
				Utils.getApiDatasource(), etag, Utils.getApiLanguage());
		Utils.upsertEtag(db, "insurance-prices", Utils.getEtag(resp));
		return resp.getData();
	}
}
