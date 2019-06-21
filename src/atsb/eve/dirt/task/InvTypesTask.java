package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.InvTypeTable;
import atsb.eve.dirt.esi.UniverseApiWrapper;
import net.evetech.ApiException;

/**
 * Task to retrieve/update type info
 * 
 * @author austin
 */
public class InvTypesTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	@Override
	public String getTaskName() {
		return "inv-types";
	}

	@Override
	protected void runTask() {
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		List<Integer> dbTypes;
		try {
			dbTypes = InvTypeTable.selectAllIds(getDb());
		} catch(SQLException e) {
			log.fatal("Failed to get existing typeIds from database", e);
			return;
		}

		List<Integer> types = new ArrayList<Integer>();
		int page = 0;
		do {
			page++;
			try {
				types = uapiw.getUniverseTypes(page);
			} catch(ApiException e) {
				if (e.getCode() != 304) {
					log.fatal("Failed to retrieve page " + page + " of type ids", e);
				}
				return;
			}
			
			for (Integer apiType : types) {
				if (!dbTypes.contains(apiType)) {
					getDaemon().addTask(new InvTypeTask(apiType));
				}
			}
		} while(types.size() > 0);
	}

}
