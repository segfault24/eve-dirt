package atsb.eve.dirt.task;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.MapTables;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import atsb.eve.model.Region;
import net.evetech.ApiException;
import net.evetech.esi.models.GetUniverseRegionsRegionIdOk;

public class RegionTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int regionId;

	public RegionTask(int regionId) {
		this.regionId = regionId;
	}

	@Override
	public String getTaskName() {
		return "region-" + regionId;
	}

	@Override
	protected void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		GetUniverseRegionsRegionIdOk region;
		try {
			region = uapiw.getUniverseRegion(regionId);
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.fatal("Failed to query region info for regionId " + regionId, e);
			}
			return;
		}

		Region r;
		try {
			r = TypeUtil.convert(region);
			MapTables.upsert(getDb(), r);
		} catch (SQLException e) {
			log.fatal("Failed to upsert region info for regionId " + regionId, e);
			return;
		}

		for (Integer constellation : region.getConstellations()) {
			getDaemon().addTask(new ConstellationTask(constellation));
		}
	}

}
