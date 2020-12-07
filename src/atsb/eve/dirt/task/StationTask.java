package atsb.eve.dirt.task;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.MapTables;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.model.Station;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseStationsStationIdOk;

public class StationTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int stationId;
	private int constellationId;
	private int regionId;

	public StationTask(int stationId, int constellationId, int regionId) {
		this.stationId = stationId;
		this.constellationId = constellationId;
		this.regionId = regionId;
	}

	@Override
	public String getTaskName() {
		return "station-" + stationId;
	}

	@Override
	protected void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		GetUniverseStationsStationIdOk station;
		try {
			station = uapiw.getUniverseStation(stationId);
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.fatal("Failed to query station info for stationId " + stationId + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
			return;
		}

		Station s;
		try {
			s = TypeUtil.convert(station);
			s.setConstellationId(constellationId);
			s.setRegionId(regionId);
			MapTables.upsert(getDb(), s);
		} catch (SQLException e) {
			log.fatal("Failed to upsert station info for stationId " + stationId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}
	}

}
