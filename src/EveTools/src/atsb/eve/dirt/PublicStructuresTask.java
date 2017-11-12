package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import is.ccp.tech.ApiException;
import is.ccp.tech.esi.UniverseApi;
import is.ccp.tech.esi.models.GetUniverseStructuresStructureIdOk;

/**
 * Task to retrieve all public structures.
 * 
 * @author austin
 */
public class PublicStructuresTask implements Runnable {

	private static Logger logger = Logger.getLogger(PublicStructuresTask.class
			.toString());

	private static final String DELETE_SQL = "";
	private static final String INSERT_SQL = "";

	private Config config;
	private int keyId;
	private boolean purge;

	private Connection con;
	private UniverseApi uapi;
	private OAuthUser auth;

	public PublicStructuresTask(Config cfg, int keyId, boolean purge) {
		this.config = cfg;
		this.keyId = keyId;
		this.purge = purge;
	}

	@Override
	public void run() {
		uapi = new UniverseApi();

		try {
			con = DriverManager.getConnection(config.getDbConnectionString(),
					config.getDbUser(), config.getDbPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: "
					+ e.getLocalizedMessage());
			return;
		}

		try {
			auth = OAuthUser.getApiAuth(config, keyId);
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"Failed to get auth details: " + e.getLocalizedMessage());
			return;
		}

		logger.log(Level.INFO, "Started public structure scrape");

		List<Long> structIds = getPublicStructIds();

		logger.log(Level.INFO, "Retrieved " + structIds.size()
				+ " public structute ids");

		for (Long structId : structIds) {
			try {
				GetUniverseStructuresStructureIdOk struct = getPublicStructure(structId);
				logger.log(Level.INFO, structId + " : " + struct.getName());
			} catch (ApiException e) {
				if (e.getCode() != 401) {
					logger.log(
							Level.WARNING,
							"Failed to retrieve info for structure " + structId,
							e);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "", e);
			}
		}

		Utils.closeQuietly(con);
	}

	private List<Long> getPublicStructIds() {
		List<Long> structIds = new ArrayList<Long>();
		try {
			structIds = uapi.getUniverseStructures("tranquility",
					config.getUserAgent(), null);
		} catch (ApiException e) {
			logger.log(Level.WARNING,
					"Failed to retrieve list of public structure ids", e);
		}
		return structIds;
	}

	private GetUniverseStructuresStructureIdOk getPublicStructure(Long structId)
			throws Exception {
		return uapi.getUniverseStructuresStructureId(structId, "tranquility",
				auth.getAuthToken(), config.getUserAgent(), null);
	}
}
