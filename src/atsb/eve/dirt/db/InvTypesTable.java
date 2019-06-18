package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import atsb.eve.dirt.model.InvType;

public class InvTypesTable {

	private static final String SELECT_ALL_IDS_SQL = "SELECT typeID FROM invTypes";
	private static final String UPSERT_SQL = "INSERT INTO invTypes "
			+ "(`typeID`,`groupID`,`typeName`,`description`,`mass`,`volume`,`capacity`,`portionSize`,`published`,`marketGroupID`,`iconID`) "
			+ "VALUES (?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE "
			+ "`typeID`=VALUES(`typeID`), `groupID`=VALUES(`groupID`), `typeName`=VALUES(`typeName`), `description`=VALUES(`description`), "
			+ "`mass`=VALUES(`mass`), `volume`=VALUES(`volume`), `capacity`=VALUES(`capacity`), `portionSize`=VALUES(`portionSize`), "
			+ "`published`=VALUES(`published`), `marketGroupID`=VALUES(`marketGroupID`), `iconID`=VALUES(`iconID`)";

	private static final String SELECT_MARKETABLE_TYPES_SQL = "SELECT typeId FROM invTypes WHERE published=1 AND marketGroupID IS NOT NULL";

	public static List<Integer> selectAllIds(Connection db) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(SELECT_ALL_IDS_SQL);
		ResultSet rs = stmt.executeQuery();
		List<Integer> types = new ArrayList<Integer>();
		while (rs.next()) {
			types.add(rs.getInt(1));
		}
		return types;
	}

	public static void upsert(Connection db, InvType i) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(UPSERT_SQL);
		stmt.setInt(1, i.getTypeId());
		stmt.setInt(2, i.getGroupId());
		stmt.setString(3, i.getTypeName());
		stmt.setString(4, i.getDescription());
		stmt.setDouble(5, i.getMass());
		stmt.setDouble(6, i.getVolume());
		stmt.setDouble(7, i.getCapacity());
		stmt.setInt(8, i.getPortionSize());
		stmt.setBoolean(9, i.isPublished());
		stmt.setInt(10, i.getMarketGroupId());
		stmt.setInt(11, i.getIconId());
		stmt.execute();
	}

	public static List<Integer> getMarketableTypeIds(Connection db) throws SQLException {
		List<Integer> typeIds = new ArrayList<Integer>();
		PreparedStatement stmt = db.prepareStatement(SELECT_MARKETABLE_TYPES_SQL);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			typeIds.add(rs.getInt("typeId"));
		}
		return typeIds;
	}

}
