package atsb.eve.dirt.task;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.esi.MarketApiWrapper;
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
	private static final String DELETE_SQL = "DELETE FROM marketHistory WHERE `regionId`=? AND `typeId`=?";
	private static final String INSERT_SQL = "INSERT INTO marketHistory (`typeId`,`regionId`,`date`,`highest`,`average`,`lowest`,`volume`,`orderCount`) VALUES (?,?,?,?,?,?,?,?)";

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

			log.debug("Started history scrape for " + types.size() + " types for region " + region);

			try {
				getDb().setAutoCommit(false); // transaction mode

				PreparedStatement stmt;

				for (Integer type : types) {
					List<GetMarketsRegionIdHistory200Ok> history = getHistory(region, type);

					try {
						stmt = getDb().prepareStatement(DELETE_SQL);
						stmt.setInt(1, region);
						stmt.setInt(2, type);
						stmt.execute();

						stmt = getDb().prepareStatement(INSERT_SQL);
						int count = 0;
						for (GetMarketsRegionIdHistory200Ok e : history) {
							stmt.setInt(1, type);
							stmt.setInt(2, region);
							stmt.setDate(3, Date.valueOf(e.getDate().toString()));
							stmt.setDouble(4, e.getHighest());
							stmt.setDouble(5, e.getAverage());
							stmt.setDouble(6, e.getLowest());
							stmt.setLong(7, e.getVolume());
							stmt.setLong(8, e.getOrderCount());

							stmt.addBatch();
							count++;

							if (count % 1000 == 0 || count == history.size()) {
								stmt.executeBatch();
							}
						}
						getDb().commit();

					} catch (SQLException e) {
						log.error("Failed to insert history for type " + type + " for region " + region);
						getDb().rollback();
					}
				}

				getDb().setAutoCommit(true);
				log.debug("Inserted history for " + types.size() + " types for region " + region);
			} catch (SQLException e) {
				log.error("Something.. went wrong?", e);
			}
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

	private List<GetMarketsRegionIdHistory200Ok> getHistory(int region, int type) {
		MarketApiWrapper mapiw = new MarketApiWrapper(getDb());
		List<GetMarketsRegionIdHistory200Ok> history = new ArrayList<GetMarketsRegionIdHistory200Ok>();
		int retry = 0;
		while (true) {
			try {
				history = mapiw.getMarketsRegionIdHistory(region, type);
				break;
			} catch (ApiException e) {
				if (retry == 3) {
					log.error("Failed to retrieve history for type " + type + " for region " + region);
					break;
				} else {
					retry++;
					continue;
				}
			}
		}
		return history;
	}
}
