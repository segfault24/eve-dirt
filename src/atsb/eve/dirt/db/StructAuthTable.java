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

}
