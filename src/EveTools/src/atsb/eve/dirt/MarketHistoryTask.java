package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import is.ccp.tech.ApiException;
import is.ccp.tech.esi.MarketApi;
import is.ccp.tech.esi.models.GetMarketsRegionIdHistory200Ok;

/**
 * Task to get market history for all items by region.
 * 
 * @author austin
 */
public class MarketHistoryTask implements Runnable {

	private static Logger logger = Logger.getLogger(MarketHistoryTask.class
			.toString());

	private static final String TYPES_SQL = "SELECT typeId FROM invTypes WHERE published=1 AND marketGroupID IS NOT NULL";
	private static final String DELETE_SQL = "DELETE FROM marketHistory WHERE `regionId`=? AND `typeId`=?";
	private static final String INSERT_SQL = "INSERT INTO marketHistory (`typeId`,`regionId`,`date`,`highest`,`average`,`lowest`,`volume`,`orderCount`) VALUES (?,?,?,?,?,?,?,?)";

	private int region;
	private Config config;
	private Connection con;

	public MarketHistoryTask(Config cfg, int region) {
		this.config = cfg;
		this.region = region;
	}

	@Override
	public void run() {
		try {
			con = DriverManager.getConnection(config.getDbConnectionString(),
					config.getDbUser(), config.getDbPass());
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to open database connection: "
					+ e.getLocalizedMessage());
			return;
		}

		List<Integer> types = getMarketableTypes();
		if (!types.isEmpty()) {

			logger.log(Level.INFO, "Started history scrape for " + types.size()
					+ " types for region " + region);

			try {
				con.setAutoCommit(false); // transaction mode

				PreparedStatement stmt;

				for (Integer type : types) {
					List<GetMarketsRegionIdHistory200Ok> history = getHistory(
							region, type);

					try {
						stmt = con.prepareStatement(DELETE_SQL);
						stmt.setInt(1, region);
						stmt.setInt(2, type);
						stmt.execute();

						stmt = con.prepareStatement(INSERT_SQL);
						int count = 0;
						for (GetMarketsRegionIdHistory200Ok e : history) {
							stmt.setInt(1, type);
							stmt.setInt(2, region);
							stmt.setDate(3,
									Date.valueOf(e.getDate().toString()));
							stmt.setFloat(4, e.getHighest());
							stmt.setFloat(5, e.getAverage());
							stmt.setFloat(6, e.getLowest());
							stmt.setLong(7, e.getVolume());
							stmt.setLong(8, e.getOrderCount());

							stmt.addBatch();
							count++;

							if (count % 1000 == 0 || count == history.size()) {
								stmt.executeBatch();
							}
						}
						con.commit();

					} catch (SQLException e) {
						logger.log(Level.WARNING,
								"Failed to insert history for type " + type
										+ " for region " + region);
						con.rollback();
					}
				}

				con.setAutoCommit(true);
				logger.log(Level.INFO, "Inserted history for " + types.size()
						+ " types for region " + region);
			} catch (SQLException e) {
				logger.log(Level.INFO, "Something.. went wrong?", e);
			}
		}

		Utils.closeQuietly(con);
	}

	private List<Integer> getMarketableTypes() {
		List<Integer> typeIds = new ArrayList<Integer>();
		try {
			PreparedStatement stmt = con.prepareStatement(TYPES_SQL);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				typeIds.add(rs.getInt("typeId"));
			}
			Utils.closeQuietly(rs);
			Utils.closeQuietly(stmt);
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Failed to retrieve list of marketable types");
			typeIds.clear();
		}
		return typeIds;
	}

	private List<GetMarketsRegionIdHistory200Ok> getHistory(int region, int type) {
		MarketApi mapi = new MarketApi();
		List<GetMarketsRegionIdHistory200Ok> history = new ArrayList<GetMarketsRegionIdHistory200Ok>();
		int retry = 0;
		while (true) {
			try {
				history = mapi.getMarketsRegionIdHistory(region, type,
						"tranquility", config.getUserAgent(),
						null);
				break;
			} catch (ApiException e) {
				if (retry == 3) {
					logger.log(Level.WARNING,
							"Failed to retrieve history for type " + type
									+ " for region " + region);
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
