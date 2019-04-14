package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StructAuthTable {

	public static List<Integer> findAuthKeyByStruct(Connection db, long structId) throws SQLException {
		PreparedStatement stmt;
		stmt = db.prepareStatement("SELECT keyId FROM dirtStructAuth WHERE structId=?");
		stmt.setLong(1, structId);
		ResultSet rs = stmt.executeQuery();
		ArrayList<Integer> keys = new ArrayList<Integer>();
		while (rs.next()) {
			keys.add(rs.getInt(1));
		}
		return keys;
	}

	public static List<Long> getAllUniqueStructs(Connection db) throws SQLException {
		PreparedStatement stmt;
		stmt = db.prepareStatement("SELECT DISTINCT structId FROM dirtStructAuth");
		ResultSet rs = stmt.executeQuery();
		ArrayList<Long> structs = new ArrayList<Long>();
		while (rs.next()) {
			structs.add(rs.getLong(1));
		}
		return structs;
	}

	public static void insert(Connection db, long structId, int keyId) throws SQLException {
		PreparedStatement stmt = db.prepareStatement("INSERT INTO dirtStructAuth (`structId`,`keyId`) VALUES (?,?)");
		stmt.setLong(1, structId);
		stmt.setInt(2, keyId);
		stmt.executeUpdate();
	}

}
