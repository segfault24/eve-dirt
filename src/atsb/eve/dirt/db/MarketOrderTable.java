package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

import atsb.eve.dirt.model.MarketOrder;
import atsb.eve.dirt.model.MarketOrder.Source;

public class MarketOrderTable {

	private static final String INSERT_SQL = "INSERT INTO marketOrder ("
			+ "`issued`,`range`,`isBuyOrder`,`duration`,"
			+ "`orderId`,`volumeRemain`,`minVolume`,`typeId`,"
			+ "`volumeTotal`,`locationId`,`price`,`regionId`,`retrieved`,`source`,`charId`"
			+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE "
			+ "`volumeRemain`=VALUES(`volumeRemain`), `price`=VALUES(`price`)";
	private static final String DELETE_REGION_SQL = "DELETE FROM marketOrder WHERE `regionId`=? AND `retrieved`<? AND `source`=" + Source.PUBLIC.getValue();
	private static final String DELETE_STRUCTURE_SQL = "DELETE FROM marketOrder WHERE `locationId`=? AND `retrieved`<? AND `source`=" + Source.STRUCTURE.getValue();
	private static final String DELETE_CHARACTER_SQL = "DELETE FROM marketOrder WHERE `charId`=? AND `retrieved`<? AND `source`=" + Source.CHARACTER.getValue();
	private static final String DELETE_SQL = "DELETE FROM marketOrder WHERE retrieved<?";

	private static final int BATCH_SIZE = 1000;

	public static void insertMany(Connection db, Collection<MarketOrder> os) throws SQLException {
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
			stmt.setInt(14, o.getSource().getValue());
			stmt.setInt(15, o.getCharId());
			stmt.addBatch();
			count++;
			if (count % BATCH_SIZE == 0 || count == os.size()) {
				stmt.executeBatch();
			}
		}
	}

	public static int deleteOldPublicRegionOrders(Connection db, int region, Timestamp olderThan) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(DELETE_REGION_SQL);
		stmt.setInt(1, region);
		stmt.setTimestamp(2, olderThan);
		return stmt.executeUpdate();
	}

	public static int deleteOldStructureOrders(Connection db, long structId, Timestamp olderThan) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(DELETE_STRUCTURE_SQL);
		stmt.setLong(1, structId);
		stmt.setTimestamp(2, olderThan);
		return stmt.executeUpdate();
	}

	public static int deleteOldCharacterOrders(Connection db, long charId, Timestamp olderThan) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(DELETE_CHARACTER_SQL);
		stmt.setLong(1, charId);
		stmt.setTimestamp(2, olderThan);
		return stmt.executeUpdate();
	}

	public static int deleteOldOrders(Connection db, Timestamp olderThan) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(DELETE_SQL);
		stmt.setTimestamp(1, olderThan);
		return stmt.executeUpdate();
	}

}
