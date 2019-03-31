package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import atsb.eve.dirt.model.Structure;

public class StructureTable {

	private static final String INSERT_SQL = "INSERT INTO structure (" + "`structId`,`structName`,`corpId`,`systemId`,`regionId`,`typeId`"
			+ ") VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " + "`structName`=VALUES(`structName`)," + "`corpId`=VALUES(`corpId`),"
			+ "`systemId`=VALUES(`systemId`)," + "`regionId`=VALUES(`regionId`)," + "`typeId`=VALUES(`typeId`)";
	private static final String SELECT_SQL = "SELECT `structId`,`structName`,`corpId`,`systemId`,`regionId`,`typeId` FROM structure WHERE structId=?";

	public static void insert(Connection db, Structure s) throws SQLException {
		PreparedStatement stmt;
		stmt = db.prepareStatement(INSERT_SQL);
		stmt.setLong(1, s.getStructId());
		stmt.setString(2, s.getStructName());
		stmt.setInt(3, s.getCorpId());
		stmt.setInt(4, s.getSystemId());
		stmt.setInt(5, s.getRegionId());
		stmt.setInt(6, s.getTypeId());
		stmt.execute();
	}

	public static Structure find(Connection db, long structId) {
		try {
			PreparedStatement stmt = db.prepareStatement(SELECT_SQL);
			stmt.setLong(1, structId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Structure s = new Structure();
				s.setStructId(structId);
				s.setStructName(rs.getString(2));
				s.setCorpId(rs.getInt(3));
				s.setSystemId(rs.getInt(4));
				s.setRegionId(rs.getInt(5));
				s.setTypeId(rs.getInt(6));
				return s;
			} else {
				return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}

}
