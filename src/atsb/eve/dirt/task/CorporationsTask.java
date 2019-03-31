package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.dirt.model.OAuthUser;
import atsb.eve.dirt.util.OAuthUtils;
import atsb.eve.dirt.util.Utils;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;

/**
 * Task to retrieve all corporations.
 * 
 * @author austin
 */
public class CorporationsTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private static final String PROPERTY_SCRAPER_KEY_ID = "scraperkeyid";
	private static final String INSERT_SQL = "";

	private int corpId;

	public CorporationsTask(int corpId) {
		this.corpId = corpId;
	}

	@Override
	public String getTaskName() {
		return "corporation" + corpId;
	}

	@Override
	public void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		int keyId = Integer.parseInt(Utils.getProperty(getDb(), PROPERTY_SCRAPER_KEY_ID));

		OAuthUser auth;
		try {
			auth = OAuthUtils.loadFromSql(getDb(), keyId);
			if (auth == null) {
				log.fatal("No auth details found for key=" + keyId);
				return;
			}
		} catch (Exception e) {
			log.fatal("Failed to get auth details for key=" + keyId, e);
			return;
		}

		// list of corps
		//   npc query
		//   corp table
		//   struct table
		//   alliance sublist
		
		List<Long> structIds = new ArrayList<Long>();
		try {
			structIds = uapiw.getUniverseStructures();
		} catch (ApiException e) {
			log.fatal("Failed to retrieve list of public structure ids", e);
			return;
		}
		log.debug("Retrieved " + structIds.size() + " public structute ids");

		PreparedStatement stmt;
		int count = 0;
		try {
			stmt = getDb().prepareStatement(INSERT_SQL);
		} catch (SQLException e) {
			log.fatal("Unexpected failure while processing public structures", e);
			return;
		}
		for (Long structId : structIds) {
			log.debug("Querying for structure information structId=" + structId);
			try {
				String authToken = OAuthUtils.getAuthToken(getDb(), auth);
				GetUniverseStructuresStructureIdOk info = uapiw.getUniverseStructuresStructureId(structId, authToken);

				stmt.setLong(1, structId);
				stmt.setString(2, info.getName());
				stmt.setInt(3, info.getOwnerId());
				stmt.setInt(4, info.getSolarSystemId());
				stmt.setInt(5, info.getTypeId());
				stmt.execute();

				count++;
			} catch (ApiException e) {
				log.error("Failed to retrieve info for structure " + structId, e);
			} catch (SQLException e) {
				log.error("Failed to insert info for structure " + structId, e);
			}
		}
		log.debug("Inserted " + count + " public structure records");
	}

}