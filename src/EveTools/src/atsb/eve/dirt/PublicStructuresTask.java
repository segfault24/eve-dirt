package atsb.eve.dirt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import atsb.eve.dirt.util.DbInfo;
import atsb.eve.dirt.util.Utils;

import net.evetech.ApiException;
import net.evetech.esi.UniverseApi;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;


/**
 * Task to retrieve all public structures.
 * 
 * @author austin
 */
public class PublicStructuresTask implements Runnable {

	private static Logger logger = Logger.getLogger(PublicStructuresTask.class
			.toString());
	
	private static final String PROPERTY_SCRAPER_KEY_ID = "scraperkeyid";

	private static final String INSERT_SQL = "INSERT INTO structure ("
			+ "`structId`,`structName`,`systemId`,`typeId`"
			+ ") VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE "
			+ "`structName`=VALUES(`structName`),"
			+ "`systemId`=VALUES(`systemId`),"
			+ "`typeId`=VALUES(`typeId`)";

	private DbInfo dbIni;

	private UniverseApi uapi;
	private OAuthUser auth;

	public PublicStructuresTask(DbInfo cfg) {
		this.dbIni = cfg;
	}

	@Override
	public void run() {
		uapi = new UniverseApi();

		Connection db;
		try {
			db = DriverManager.getConnection(dbIni.getDbConnectionString(),
					dbIni.getUser(), dbIni.getPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: "
					+ e.getLocalizedMessage());
			return;
		}
		
		int keyId = Integer.parseInt(Utils.getProperty(db, PROPERTY_SCRAPER_KEY_ID));

		try {
			auth = OAuthUser.getApiAuth(dbIni, keyId);
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"Failed to get auth details: " + e.getLocalizedMessage());
			return;
		}

		logger.log(Level.INFO, "Started public structure scrape");

		List<Long> structIds = getPublicStructIds();

		logger.log(Level.INFO, "Retrieved " + structIds.size()
				+ " public structute ids");

		int count = 0;
		try {
			PreparedStatement stmt = db.prepareStatement(INSERT_SQL);
			for (Long structId : structIds) {
				try {
					GetUniverseStructuresStructureIdOk info = getPublicStructure(structId);
					
					stmt.setLong(1, structId);
					stmt.setString(2, info.getName());
					stmt.setInt(3, info.getSolarSystemId());
					stmt.setInt(4, info.getTypeId());
					stmt.execute();
					
					count++;
				} catch (Exception e) {
					logger.log(
							Level.WARNING,
							"Failed to retrieve info for structure " + structId + ": " + e.getLocalizedMessage());
				}
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Unexpected failure while processing public structures", e);
		}

		logger.log(Level.INFO, "Inserted " + count + " public structure records");

		Utils.closeQuietly(db);
	}

	private List<Long> getPublicStructIds() {
		List<Long> structIds = new ArrayList<Long>();
		try {
			structIds = uapi.getUniverseStructures("tranquility", null);
		} catch (ApiException e) {
			logger.log(Level.WARNING,
					"Failed to retrieve list of public structure ids", e);
		}
		return structIds;
	}

	private GetUniverseStructuresStructureIdOk getPublicStructure(Long structId)
			throws ApiException, DirtAuthException, SQLException, IOException {
		return uapi.getUniverseStructuresStructureId(structId, "tranquility", null, null);
	}

}
