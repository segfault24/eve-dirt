package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.MapTables;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.model.SolarSystem;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseSystemsSystemIdOk;

public class SolarSystemTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int solarSystemId;
	private int regionId;

	public SolarSystemTask(int solarSystemId, int regionId) {
		this.solarSystemId = solarSystemId;
		this.regionId = regionId;
	}

	@Override
	public String getTaskName() {
		return "solar-system-" + solarSystemId;
	}

	@Override
	protected void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		GetUniverseSystemsSystemIdOk system;
		try {
			system = uapiw.getUniverseSolarSystem(solarSystemId);
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.fatal("Failed to query solar system info for solarSystemId " + solarSystemId + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
			return;
		}

		SolarSystem s;
		try {
			s = TypeUtil.convert(system);
			s.setRegionId(regionId);
			MapTables.upsert(getDb(), s);
		} catch (SQLException e) {
			log.fatal("Failed to upsert solar system info for solarSystemId " + solarSystemId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}

		List<Integer> stations = system.getStations();
		for (Integer station : stations) {
			getDaemon().addTask(new StationTask(station, s.getConstellationId(), regionId));
		}
	}

}
