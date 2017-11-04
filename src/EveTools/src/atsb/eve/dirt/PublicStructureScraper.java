package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import is.ccp.tech.ApiClient;
import is.ccp.tech.ApiException;
import is.ccp.tech.esi.UniverseApi;
import is.ccp.tech.esi.models.GetUniverseStructuresStructureIdOk;

/**
 *
 *
 * @author austin
 */
public class PublicStructureScraper {

	private static Logger logger = Logger.getLogger(PublicStructureScraper.class.toString());

	private static final String DELETE_SQL = "";
	private static final String INSERT_SQL = "";

	private Config cfg;
	private Connection con;
	private OAuthUser auth;
	private UniverseApi uapi;

	public PublicStructureScraper() throws Exception {
		cfg = Config.getInstance();
		uapi = new UniverseApi();
		auth = OAuthUser.getApiAuth(Config.getInstance().getScraperAuthKeyId());
		uapi.getApiClient().setAccessToken(auth.getAuthToken());
		con = DriverManager.getConnection(cfg.getDbConnectionString(), cfg.getDbUser(), cfg.getDbPass());
	}

	public void scrape() {
		logger.log(Level.INFO, "Started public structure scrape");

		List<Long> structIds;
		try {
			structIds = getPublicStructIds();
		} catch (ApiException e) {
			logger.log(Level.WARNING, "Failed to retrieve list of public structure ids", e);
			return;
		}
		logger.log(Level.INFO, "Retrieved " + structIds.size() + " public structute ids");

		for (Long structId : structIds) {
			try {
				GetUniverseStructuresStructureIdOk struct = getPublicStructure(structId);
				logger.log(Level.FINEST, "name: " + struct.getName());
			} catch (ApiException e) {
				if (e.getCode() != 401) {
					logger.log(Level.WARNING, "Failed to retrieve info for structure " + structId, e);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "", e);
			}
		}
	}

	public List<Long> getPublicStructIds() throws ApiException {
		return uapi.getUniverseStructures("tranquility", Config.getInstance().getUserAgent(), null);
	}

	public GetUniverseStructuresStructureIdOk getPublicStructure(Long structId) throws Exception {
		return uapi.getUniverseStructuresStructureId(structId, "tranquility", auth.getAuthToken(), Config.getInstance().getUserAgent(),
				null);
	}

	public static void main(String[] args) {
		if (args.length != 0) {
			logger.log(Level.WARNING, "This program needs no arguments, why did you supply " + args.length + "?");
		}

		PublicStructureScraper o;
		try {
			o = new PublicStructureScraper();
			o.scrape();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to initialize scraper", e);
			System.exit(1);
		}

		System.exit(0);
	}
}
