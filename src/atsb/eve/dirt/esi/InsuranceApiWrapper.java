package atsb.eve.dirt.esi;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.util.Utils;
import net.evetech.ApiException;
import net.evetech.ApiResponse;
import net.evetech.esi.InsuranceApi;
import net.evetech.esi.models.GetInsurancePrices200Ok;

public class InsuranceApiWrapper {

	private static Logger log = LogManager.getLogger();

	private Connection db;
	private InsuranceApi iapi;

	public InsuranceApiWrapper(Connection db) {
		this.db = db;
		iapi = new InsuranceApi();
	}

	public List<GetInsurancePrices200Ok> getInsurancePrices() throws ApiException {
		String etag = Utils.getEtag(db, "insurance-prices");
		log.trace("Executing API query getInsurancePrices()");
		ApiResponse<List<GetInsurancePrices200Ok>> resp = iapi.getInsurancePricesWithHttpInfo(Utils.getApiLanguage(),
				Utils.getApiDatasource(), etag, Utils.getApiLanguage());
		log.trace("API query returned status code " + resp.getStatusCode());
		if (!resp.getData().isEmpty()) {
			Utils.upsertEtag(db, "insurance-prices", Utils.getEtag(resp.getHeaders()));
		}
		return resp.getData();
	}
}
