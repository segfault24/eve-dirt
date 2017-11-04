package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import is.ccp.tech.ApiException;
import is.ccp.tech.esi.MarketApi;
import is.ccp.tech.esi.models.GetMarketsRegionIdOrders200Ok;

/**
 * Single threaded scraper for bulk market orders by region. SQL details are
 * read from Config.
 *
 * @author austin
 */
public class MarketOrderScraper {

	private static Logger logger = Logger.getLogger(MarketOrderScraper.class.toString());

	private static final String DELETE_SQL = "DELETE FROM marketOrder WHERE `regionId`=?";
	private static final String INSERT_SQL = "INSERT INTO marketOrder (" + "`issued`,`range`,`isBuyOrder`,`duration`,"
			+ "`orderId`,`volumeRemain`,`minVolume`,`typeId`,"
			+ "`volumeTotal`,`locationId`,`price`,`regionId`,`retrieved`" + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private Connection con;

	public MarketOrderScraper() throws SQLException {
		Config cfg = Config.getInstance();
		con = DriverManager.getConnection(cfg.getDbConnectionString(), cfg.getDbUser(), cfg.getDbPass());
	}

	public void scrape(int region) {
		logger.log(Level.INFO, "Started order scrape for region " + region);

		List<GetMarketsRegionIdOrders200Ok> orders = getPublicOrders(region);
		logger.log(Level.INFO, "Retrieved " + orders.size() + " orders for region " + region);

		Timestamp now = new Timestamp(System.currentTimeMillis());
		try {
			con.setAutoCommit(false);

			PreparedStatement stmt = con.prepareStatement(DELETE_SQL);
			stmt.setInt(1, region);
			stmt.execute();
			stmt.close();

			stmt = con.prepareStatement(INSERT_SQL);
			int count = 0;
			for (GetMarketsRegionIdOrders200Ok o : orders) {
				stmt.setTimestamp(1, new Timestamp(o.getIssued().getMillis()));
				stmt.setString(2, o.getRange().toString());
				stmt.setBoolean(3, o.getIsBuyOrder());
				stmt.setInt(4, o.getDuration());
				stmt.setLong(5, o.getOrderId());
				stmt.setInt(6, o.getVolumeRemain());
				stmt.setInt(7, o.getMinVolume());
				stmt.setInt(8, o.getTypeId());
				stmt.setInt(9, o.getVolumeTotal());
				stmt.setLong(10, o.getLocationId());
				stmt.setFloat(11, o.getPrice());
				stmt.setInt(12, region);
				stmt.setTimestamp(13, now);

				stmt.addBatch();
				count++;

				if (count % 1000 == 0 || count == orders.size()) {
					try {
						stmt.executeBatch();
					} catch(SQLException ex) {
						logger.log(Level.WARNING, "Failed to insert some orders for region " + region);
					}
				}
			}

			con.commit();
			con.setAutoCommit(true);
			logger.log(Level.INFO, "Inserted " + orders.size() + " orders for region " + region);
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Unexpected failure while processing orders for region " + region);
		}
	}

	private List<GetMarketsRegionIdOrders200Ok> getPublicOrders(int region) {
		MarketApi mapi = new MarketApi();
		boolean done = false;
		List<GetMarketsRegionIdOrders200Ok> allOrders = new ArrayList<GetMarketsRegionIdOrders200Ok>();
		int page = 1;
		int retry = 0;
		while (!done) {
			List<GetMarketsRegionIdOrders200Ok> orders;
			try {
				orders = mapi.getMarketsRegionIdOrders("all", region, "tranquility", page, null,
						Config.getInstance().getUserAgent(), null);
				if (orders.isEmpty()) {
					break;
				}
				allOrders.addAll(orders);
				page++;
				retry = 0;
			} catch (ApiException e) {
				if (retry == 3) {
					retry = 0;
					logger.log(Level.WARNING, "Failed to retrieve page " + page + " of orders for region " + region);
					page++;
				} else {
					retry++;
					continue;
				}
			}
		}
		return allOrders;
	}

	//private List<> getPrivateOrders(int region) {
		// get structure ids from sql where not public
		//MarketApi mapi = new MarketApi();
		//mapi.getMarketsStructuresStructureId(structureId, "tranquility", page, token, Config.getInstance().getUserAgent(), null);
	//}

	public static void main(String[] args) {
		if (args.length == 0) {
			logger.log(Level.SEVERE, "Need at least one region argument");
			System.exit(1);
		}

		// parse the region arguments
		List<Integer> regions = new ArrayList<Integer>();
		for (String arg : args) {
			try {
				Integer reg = Integer.parseInt(arg);
				regions.add(reg);
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "The argument '" + arg + "' is not a valid integer");
			}
		}

		try {
			MarketOrderScraper o = new MarketOrderScraper();
			for (Integer region : regions) {
				o.scrape(region);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Failed to initialize scraper", e);
			System.exit(1);
		}

		System.exit(0);
	}
}
