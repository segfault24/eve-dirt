package atsb.eve.dirt.task;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.MapTables;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.model.Constellation;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseConstellationsConstellationIdOk;

public class ConstellationTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int constellationId;

	public ConstellationTask(int constellationId) {
		this.constellationId = constellationId;
	}

	@Override
	public String getTaskName() {
		return "constellation-" + constellationId;
	}

	@Override
	protected void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		GetUniverseConstellationsConstellationIdOk constellation;
		try {
			constellation = uapiw.getUniverseConstellation(constellationId);
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.fatal("Failed to query constellation info for constellationId " + constellationId + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
			return;
		}

		Constellation c;
		try {
			c = TypeUtil.convert(constellation);
			MapTables.upsert(getDb(), c);
		} catch (SQLException e) {
			log.fatal("Failed to upsert constellation info for constellationId " + constellationId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}

		for (Integer system : constellation.getSystems()) {
			getDaemon().addTask(new SolarSystemTask(system, c.getRegionId()));
		}
	}

}
