package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import atsb.eve.dirt.model.MarketOrder;

public class MarketOrderDb {

	private static final String INSERT_SQL = "INSERT INTO marketOrder (" + "`issued`,`range`,`isBuyOrder`,`duration`,"
			+ "`orderId`,`volumeRemain`,`minVolume`,`typeId`,"
			+ "`volumeTotal`,`locationId`,`price`,`regionId`,`retrieved`" + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_REGION_SQL = "DELETE FROM marketOrder WHERE `regionId`=?";

	private static final int BATCH_SIZE = 1000;

	public static void insert(Connection db, MarketOrder o) throws SQLException {
		insertMany(db, Arrays.asList(o));
	}

	public static void insertMany(Connection db, Collection<MarketOrder> os) throws SQLException {
		db.setAutoCommit(false);
		PreparedStatement stmt = db.prepareStatement(INSERT_SQL);
		int count = 0;
		for (MarketOrder o : os) {
			stmt.setTimestamp(1, o.getIssued());
			stmt.setString(2, o.getRange());
			stmt.setBoolean(3, o.isBuyOrder());
			stmt.setInt(4, o.getDuration());
			stmt.setLong(5, o.getOrderId());
			stmt.setInt(6, o.getVolumeRemain());
			stmt.setInt(7, o.getMinVolume());
			stmt.setInt(8, o.getTypeId());
			stmt.setInt(9, o.getVolumeTotal());
			stmt.setLong(10, o.getLocationId());
			stmt.setDouble(11, o.getPrice());
			stmt.setInt(12, o.getRegion());
			stmt.setTimestamp(13, o.getRetrieved());
			stmt.addBatch();
			count++;
			if (count % BATCH_SIZE == 0 || count == os.size()) {
				stmt.executeBatch();
			}
		}
		db.commit();
		db.setAutoCommit(true);
	}

	public static void deleteRegion(Connection db, int region) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(DELETE_REGION_SQL);
		stmt.setInt(1, region);
		stmt.execute();
	}

}
