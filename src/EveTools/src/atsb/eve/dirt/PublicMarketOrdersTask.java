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

import atsb.eve.dirt.util.Utils;

import is.ccp.tech.ApiClient;
import is.ccp.tech.ApiException;
import is.ccp.tech.esi.MarketApi;
import is.ccp.tech.esi.models.GetMarketsRegionIdOrders200Ok;

/**
 * Task to get bulk market orders by region.
 * 
 * @author austin
 */
public class PublicMarketOrdersTask implements Runnable {

	private static Logger logger = Logger
			.getLogger(PublicMarketOrdersTask.class.toString());

	private static final String DELETE_SQL = "DELETE FROM marketOrder WHERE `regionId`=?";
	private static final String INSERT_SQL = "INSERT INTO marketOrder ("
			+ "`issued`,`range`,`isBuyOrder`,`duration`,"
			+ "`orderId`,`volumeRemain`,`minVolume`,`typeId`,"
			+ "`volumeTotal`,`locationId`,`price`,`regionId`,`retrieved`"
			+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private int region;
	private DaemonProperties config;
	private Connection con;

	public PublicMarketOrdersTask(DaemonProperties cfg, int region) {
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

		logger.log(Level.INFO, "Started order scrape for region " + region);
		List<GetMarketsRegionIdOrders200Ok> orders = getPublicOrders(region);
		logger.log(Level.INFO, "Retrieved " + orders.size()
				+ " orders for region " + region);

		Timestamp now = new Timestamp(System.currentTimeMillis());
		try {
			con.setAutoCommit(false);

			PreparedStatement stmt = con.prepareStatement(DELETE_SQL);
			stmt.setInt(1, region);
			stmt.execute();
			Utils.closeQuietly(stmt);

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
				stmt.setDouble(11, o.getPrice());
				stmt.setInt(12, region);
				stmt.setTimestamp(13, now);

				stmt.addBatch();
				count++;

				if (count % 1000 == 0 || count == orders.size()) {
					try {
						stmt.executeBatch();
					} catch (SQLException ex) {
						logger.log(Level.WARNING,
								"Failed to insert some orders for region "
										+ region);
					}
				}
			}

			con.commit();
			con.setAutoCommit(true);
			logger.log(Level.INFO, "Inserted " + orders.size()
					+ " orders for region " + region);
		} catch (SQLException e) {
			logger.log(Level.WARNING,
					"Unexpected failure while processing region " + region);
		}

		Utils.closeQuietly(con);
	}

	private List<GetMarketsRegionIdOrders200Ok> getPublicOrders(int region) {
		MarketApi mapi = new MarketApi(new ApiClient());
		boolean done = false;
		List<GetMarketsRegionIdOrders200Ok> allOrders = new ArrayList<GetMarketsRegionIdOrders200Ok>();

		int page = 1;
		int retry = 0;
		boolean failure = false;
		while (!done) {
			List<GetMarketsRegionIdOrders200Ok> orders;
			try {
				orders = mapi.getMarketsRegionIdOrders("all", region,
						"tranquility", null, page, null);
				if (orders.isEmpty()) {
					break;
				}
				allOrders.addAll(orders);
				page++;
				retry = 0;
				failure = false;
			} catch (ApiException e) {
				if (retry == 3) {
					retry = 0;
					logger.log(Level.WARNING, "Failed to retrieve page " + page
							+ " of orders for region " + region);
					page++;
					if (failure) {
						// stop pulling pages when we hit two bad pages in a row
						break;
					} else {
						failure = true;
					}
				} else {
					retry++;
					continue;
				}
			}
		}
		return allOrders;
	}
}
