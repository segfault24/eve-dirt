package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.ApiAuthTable;
import atsb.eve.db.MapTables;
import atsb.eve.db.StructAuthTable;
import atsb.eve.db.StructureTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.dirt.esi.auth.OAuthUtil;
import atsb.eve.model.OAuthUser;
import atsb.eve.model.Structure;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;

/**
 * Task to retrieve information about a structure.
 * 
 * @author austin
 */
public class StructureTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private long structId;
	private int keyId = -1;

	public StructureTask(long structId) {
		this.structId = structId;
	}

	public StructureTask(long structId, int keyId) {
		this.structId = structId;
		this.keyId = keyId;
	}

	@Override
	public String getTaskName() {
		return "structure-" + structId;
	}

	@Override
	protected void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		if (keyId == -1) {
			// get auth keys that are authorized to read the structure's information
			List<Integer> keys;
			try {
				keys = StructAuthTable.getAuthKeyByStruct(getDb(), structId);
			} catch (SQLException e) {
				log.fatal("Failed to search for auth keys for structure " + structId + ": " + e.getLocalizedMessage());
				log.debug(e);
				return;
			}
			if (!keys.isEmpty()) {
				keyId = keys.get(0);
			} else {
				log.fatal("Failed to find any auth keys for structure " + structId);
				return;
			}
		}

		OAuthUser auth;
		try {
			auth = ApiAuthTable.getUserByKeyId(getDb(), keyId);
			if (auth == null) {
				log.fatal("No auth details found for key=" + keyId);
				return;
			}
		} catch (Exception e) {
			log.fatal("Failed to get auth details for key=" + keyId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}

		log.debug("Querying structure information for structId=" + structId);
		try {
			GetUniverseStructuresStructureIdOk info = uapiw.getUniverseStructuresStructureId(structId, OAuthUtil.getAuthToken(getDb(), auth));
			Structure s = TypeUtil.convert(info);
			s.setStructId(structId);
			s.setRegionId(MapTables.findRegionBySystem(getDb(), s.getSystemId()).getReigonId());
			StructureTable.insert(getDb(), s);
			log.debug("Inserted structure information for structId=" + structId);
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.error("Failed to retrieve info for structure " + structId + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
		} catch (SQLException e) {
			log.error("Failed to insert info for structure " + structId + ": " + e.getLocalizedMessage());
			log.debug(e);
		}
	}

}
