package atsb.eve.dirt.task;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.SolarSystemTable;
import atsb.eve.dirt.db.StructureTable;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.dirt.model.OAuthUser;
import atsb.eve.dirt.model.Structure;
import atsb.eve.dirt.util.OAuthUtils;
import atsb.eve.dirt.util.Utils;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;

/**
 * @author austin
 *
 */
public class StructureTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private long structId;

	public StructureTask(long structId) {
		this.structId = structId;
	}

	@Override
	public String getTaskName() {
		return "structure-" + structId;
	}

	@Override
	protected void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		int keyId = Integer.parseInt(Utils.getProperty(getDb(), Utils.PROPERTY_SCRAPER_KEY_ID));

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

		log.debug("Querying structure information for structId=" + structId);
		try {
			String authToken = OAuthUtils.getAuthToken(getDb(), auth);
			GetUniverseStructuresStructureIdOk info = uapiw.getUniverseStructuresStructureId(structId, authToken);
			Structure s = new Structure(info);
			s.setStructId(structId);
			s.setRegionId(SolarSystemTable.findRegionBySystem(getDb(), s.getSystemId()));
			StructureTable.insert(getDb(), s);
			log.debug("Inserted structure information for structId=" + structId);
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.error("Failed to retrieve info for structure " + structId, e);
			}
		} catch (SQLException e) {
			log.error("Failed to insert info for structure " + structId, e);
		}
	}

}
