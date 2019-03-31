package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import atsb.eve.dirt.model.MarketHistoryEntry;

public class MarketHistoryTable {

	private static final String INSERT_SQL = "INSERT INTO marketHistory (`typeId`,`regionId`,`date`,`highest`,`average`,`lowest`,`volume`,`orderCount`) "
			+ "VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE "
			+ "`highest`=VALUES(`highest`), `average`=VALUES(`average`), `lowest`=VALUES(`lowest`), `volume`=VALUES(`volume`), `orderCount`=VALUES(`orderCount`)";

	private static final int BATCH_SIZE = 1000;

	public static void insertMany(Connection db, Collection<MarketHistoryEntry> oh) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(INSERT_SQL);
		int count = 0;
		for (MarketHistoryEntry h : oh) {
			stmt.setInt(1, h.getTypeId());
			stmt.setInt(2, h.getRegionId());
			stmt.setDate(3, h.getDate());
			stmt.setDouble(4, h.getHighest());
			stmt.setDouble(5, h.getAverage());
			stmt.setDouble(6, h.getLowest());
			stmt.setLong(7, h.getVolume());
			stmt.setLong(8, h.getOrderCount());
			stmt.addBatch();
			count++;
			if (count % BATCH_SIZE == 0 || count == oh.size()) {
				stmt.executeBatch();
			}
		}
	}

}
