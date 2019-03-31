package atsb.eve.dirt.task;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.MarketHistoryTable;
import atsb.eve.dirt.esi.MarketApiWrapper;
import atsb.eve.dirt.model.MarketHistoryEntry;
import atsb.eve.dirt.util.Utils;

import net.evetech.ApiException;
import net.evetech.esi.models.GetMarketsRegionIdHistory200Ok;

/**
 * Task to get market history for all items by region.
 * 
 * @author austin
 */
public class MarketHistoryTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private static final String TYPES_SQL = "SELECT typeId FROM invTypes WHERE published=1 AND marketGroupID IS NOT NULL";

	private int region;

	public MarketHistoryTask(int region) {
		this.region = region;
	}

	@Override
	public String getTaskName() {
		return "market-history-" + region;
	}

	@Override
	public void runTask() {
		List<Integer> types = getMarketableTypes();
		if (!types.isEmpty()) {
			for (Integer type : types) {
				List<MarketHistoryEntry> hs = getHistory(region, type);
				try {
					getDb().setAutoCommit(false);
					MarketHistoryTable.insertMany(getDb(), hs);
					getDb().commit();
					getDb().setAutoCommit(true);
					log.debug("Inserted " + hs.size() + " history entries for region " + region + " type " + type);
				} catch (SQLException e) {
					log.error("Failed to insert history for type " + type + " for region " + region);
				}
			}
			log.debug("Inserted history for " + types.size() + " types for region " + region);
		}
	}

	private List<Integer> getMarketableTypes() {
		List<Integer> typeIds = new ArrayList<Integer>();
		try {
			PreparedStatement stmt = getDb().prepareStatement(TYPES_SQL);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				typeIds.add(rs.getInt("typeId"));
			}
			Utils.closeQuietly(rs);
			Utils.closeQuietly(stmt);
		} catch (SQLException e) {
			log.error("Failed to retrieve list of marketable types");
			typeIds.clear();
		}
		return typeIds;
	}

	private List<MarketHistoryEntry> getHistory(int regionId, int typeId) {
		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetMarketsRegionIdHistory200Ok> history = new ArrayList<GetMarketsRegionIdHistory200Ok>();
		int retry = 0;
		while (true) {
			try {
				history = mapiw.getMarketsRegionIdHistory(regionId, typeId);
				break;
			} catch (ApiException e) {
				if (retry == 3) {
					log.error("Failed to retrieve history for type " + typeId + " for region " + regionId);
					break;
				} else {
					retry++;
					continue;
				}
			}
		}
		List<MarketHistoryEntry> entries = new ArrayList<MarketHistoryEntry>(history.size());
		for (GetMarketsRegionIdHistory200Ok h : history) {
			MarketHistoryEntry e = new MarketHistoryEntry(h);
			e.setTypeId(typeId);
			e.setRegionId(regionId);
			entries.add(e);
		}
		return entries;
	}
}
