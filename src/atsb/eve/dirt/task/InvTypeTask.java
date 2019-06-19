package atsb.eve.dirt.task;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.InvTypesTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseTypesTypeIdOk;

public class InvTypeTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int typeId;

	public InvTypeTask(int typeId) {
		this.typeId = typeId;
		this.setSaveStatus(false);
	}

	@Override
	public String getTaskName() {
		return "inv-type-" + typeId;
	}

	@Override
	protected void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		GetUniverseTypesTypeIdOk type;
		try {
			type = uapiw.getUniverseType(typeId);
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.fatal("Failed to query type info for typeId " + typeId, e);
			}
			return;
		}
		try {
			InvTypesTable.upsert(getDb(), TypeUtil.convert(type));
		} catch (SQLException e) {
			log.fatal("Failed to upsert type info for typeId " + typeId, e);
		}
	}

}
