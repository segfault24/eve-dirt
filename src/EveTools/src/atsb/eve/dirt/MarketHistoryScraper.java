package atsb.eve.dirt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import is.ccp.tech.ApiException;
import is.ccp.tech.esi.MarketApi;
import is.ccp.tech.esi.models.GetMarketsRegionIdHistory200Ok;

/**
 * Scraper for market history by region. SQL details are read from Config. Type
 * list given as arg is read from the cfg/ directory.
 *
 * @author austin
 */
public class MarketHistoryScraper {

	private static Logger logger = Logger.getLogger(MarketHistoryScraper.class.toString());

	private static final String DELETE_SQL = "DELETE FROM marketHistory WHERE `regionId`=? AND `typeId`=?";
	private static final String INSERT_SQL = "INSERT INTO marketHistory (`typeId`,`regionId`,`date`,`highest`,`average`,`lowest`,`volume`,`orderCount`) VALUES (?,?,?,?,?,?,?,?)";

	private Connection con;

	public MarketHistoryScraper() throws SQLException {
		Config cfg = Config.getInstance();
		con = DriverManager.getConnection(cfg.getDbConnectionString(), cfg.getDbUser(), cfg.getDbPass());
	}

	public void scrape(int region, List<Integer> types) {
		logger.log(Level.INFO, "Started history scrape for " + types.size() + " types for region " + region);

		try {
			con.setAutoCommit(false); // transaction mode

			PreparedStatement stmt;

			for (Integer type : types) {
				List<GetMarketsRegionIdHistory200Ok> history = getHistory(region, type);

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
						stmt.setDate(3, Date.valueOf(e.getDate().toString()));
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
					logger.log(Level.WARNING, "Failed to insert history for type " + type + " for region " + region);
					con.rollback();
				}
			}

			con.setAutoCommit(true);
			logger.log(Level.INFO, "Inserted history for " + types.size() + " types for region " + region);
		} catch (SQLException e) {
			logger.log(Level.INFO, "Something.. went wrong?", e);
		}
	}

	private List<GetMarketsRegionIdHistory200Ok> getHistory(int region, int type) {
		MarketApi mapi = new MarketApi();
		List<GetMarketsRegionIdHistory200Ok> history = new ArrayList<GetMarketsRegionIdHistory200Ok>();
		int retry = 0;
		while (true) {
			try {
				history = mapi.getMarketsRegionIdHistory(region, type, "tranquility",
						Config.getInstance().getUserAgent(), null);
				break;
			} catch (ApiException e) {
				if (retry == 3) {
					logger.log(Level.WARNING, "Failed to retrieve history for type " + type + " for region " + region);
					break;
				} else {
					retry++;
					continue;
				}
			}
		}
		return history;
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Need one region argument and type id file");
			System.exit(1);
		}

		// parse the region argument
		Integer region = 0;
		try {
			region = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println("The argument '" + args[0] + "' is not a valid integer");
			System.exit(1);
		}

		// open the type file, and parse the type ids
		List<Integer> types = new ArrayList<Integer>();
		BufferedReader br = null;
		try {
			File f = new File(args[1]);
			br = new BufferedReader(new FileReader(f));

			String line;
			while ((line = br.readLine()) != null) {
				try {
					types.add(Integer.parseInt(line));
				} catch (NumberFormatException e) {
					logger.log(Level.WARNING, "'" + line + "' is not a valid integer");
				}
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error while reading type file", e);
			System.exit(1);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		try {
			MarketHistoryScraper o = new MarketHistoryScraper();
			o.scrape(region, types);
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to initialize scraper", e);
		}

		System.exit(0);
	}
}
