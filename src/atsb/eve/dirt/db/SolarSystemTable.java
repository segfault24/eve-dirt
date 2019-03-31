package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SolarSystemTable {

	public static int findRegionBySystem(Connection db, int systemId) throws SQLException {
		PreparedStatement stmt;
		stmt = db.prepareStatement("SELECT regionID FROM mapSolarSystems WHERE solarSystemID=?");
		stmt.setInt(1, systemId);
		ResultSet rs = stmt.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

}
