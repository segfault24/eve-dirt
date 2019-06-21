package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.ApiAuthTable;
import atsb.eve.db.MapTables;
import atsb.eve.db.StructureTable;
import atsb.eve.dirt.DirtConstants;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.dirt.esi.auth.OAuthUtil;
import atsb.eve.model.OAuthUser;
import atsb.eve.model.Structure;
import atsb.eve.util.Utils;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;

/**
 * Task to retrieve all public structures.
 * 
 * @author austin
 */
public class PublicStructuresTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	public PublicStructuresTask() {
	}

	@Override
	public String getTaskName() {
		return "public-structures";
	}

	@Override
	public void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		int keyId = Integer.parseInt(Utils.getProperty(getDb(), DirtConstants.PROPERTY_SCRAPER_KEY_ID));

		OAuthUser auth;
		try {
			auth = ApiAuthTable.getUserByKeyId(getDb(), keyId);
			if (auth == null) {
				log.fatal("No auth details found for key=" + keyId);
				return;
			}
		} catch (Exception e) {
			log.fatal("Failed to get auth details for key=" + keyId, e);
			return;
		}

		List<Long> structIds = new ArrayList<Long>();
		try {
			structIds = uapiw.getUniverseStructures();
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.fatal("Failed to retrieve list of public structure ids", e);
			}
			return;
		}
		log.debug("Retrieved " + structIds.size() + " public structure ids");

		for (Long structId : structIds) {
			try {
				GetUniverseStructuresStructureIdOk info = uapiw.getUniverseStructuresStructureId(structId, OAuthUtil.getAuthToken(getDb(), auth));
				Structure s = TypeUtil.convert(info);
				s.setStructId(structId);
				s.setRegionId(MapTables.findRegionBySystem(getDb(), s.getSystemId()).getReigonId());
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

}
