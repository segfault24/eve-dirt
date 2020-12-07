package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.MarketGroupTable;
import atsb.eve.dirt.esi.MarketApiWrapper;
import net.evetech.ApiException;

/**
 * Task to retrieve/update market group info
 * 
 * @author austin
 */
public class InvMarketGroupsTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private boolean force = false;

	@Override
	public String getTaskName() {
		return "inv-market-groups";
	}

	@Override
	protected void runTask() {
		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());

		List<Integer> dbGroups;
		try {
			dbGroups = MarketGroupTable.selectAllIds(getDb());
		} catch (SQLException e) {
			log.fatal("Failed to get existing marketGroupIds from database: " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}

		List<Integer> groups;
		try {
			groups = mapiw.getMarketGroupIds();
		} catch (ApiException e) {
			if (e.getCode() != 304) {
				log.fatal("Failed to retrieve market group ids: " + e.getLocalizedMessage());
				log.debug(e);
			}
			return;
		}

		for (Integer apiGroup : groups) {
			// queue task if we don't have the info for the group, or we're forcing an update
			if (!dbGroups.contains(apiGroup) || force) {
				getDaemon().addTask(new InvMarketGroupTask(apiGroup));
			}
		}
	}

	public void forceUpdate(boolean force) {
		this.force = force;
	}

}
