package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import atsb.eve.dirt.model.InvMarketGroup;

public class InvMarketGroupsTable {

	private static final String SELECT_ALL_IDS_SQL = "SELECT marketGroupID FROM invMarketGroups";
	private static final String UPSERT_SQL = "INSERT INTO invMarketGroups "
			+ "(`marketGroupID`,`parentGroupID`,`marketGroupName`,`description`,`hasTypes`) "
			+ "VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE "
			+ "`marketGroupID`=VALUES(`marketGroupID`), `parentGroupID`=VALUES(`parentGroupID`), `marketGroupName`=VALUES(`marketGroupName`), "
			+ "`description`=VALUES(`description`), `hasTypes`=VALUES(`hasTypes`)";

	public static List<Integer> selectAllIds(Connection db) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(SELECT_ALL_IDS_SQL);
		ResultSet rs = stmt.executeQuery();
		List<Integer> types = new ArrayList<Integer>();
		while (rs.next()) {
			types.add(rs.getInt(1));
		}
		return types;
	}

	public static void upsert(Connection db, InvMarketGroup g) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(UPSERT_SQL);
		stmt.setInt(1, g.getMarketGroupId());
		stmt.setInt(2, g.getParentGroupId());
		stmt.setString(3, g.getMarketGroupName());
		stmt.setString(4, g.getDescription());
		stmt.setBoolean(5, g.hasTypes());
		stmt.execute();
	}

}
