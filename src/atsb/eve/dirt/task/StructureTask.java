package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.ApiAuthTable;
import atsb.eve.dirt.db.SolarSystemTable;
import atsb.eve.dirt.db.StructAuthTable;
import atsb.eve.dirt.db.StructureTable;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.dirt.model.OAuthUser;
import atsb.eve.dirt.model.Structure;
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

		// get auth keys that are authorized to read the structure's information
		List<Integer> keys;
		try {
			keys = StructAuthTable.findAuthKeyByStruct(getDb(), structId);
		} catch (SQLException e1) {
			log.fatal("Failed to search for auth keys for structure " + structId);
			return;
		}
		int keyId;
		if (!keys.isEmpty()) {
			keyId = keys.get(0);
		} else {
			log.fatal("Failed to find any auth keys for structure " + structId);
			return;
		}

		OAuthUser auth;
		try {
			auth = ApiAuthTable.getUser(getDb(), keyId);
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
			GetUniverseStructuresStructureIdOk info = uapiw.getUniverseStructuresStructureId(structId, auth.getAuthToken());
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
