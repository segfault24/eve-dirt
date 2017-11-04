package atsb.eve.dirt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TopTypes {

	private static final String SELECT_SQL = 
			"SELECT it.typeId FROM markethistory AS mh JOIN invTypes AS it ON mh.typeId=it.typeId"
			+ " WHERE mh.regionId=10000002 AND it.published=1 AND it.marketGroupID IS NOT NULL"
			+ " GROUP BY it.typeId ORDER BY AVG(mh.`volume`*mh.`average`) DESC LIMIT ?;";

	private Connection con;

	public TopTypes() throws SQLException {
		Config cfg = Config.getInstance();
		con = DriverManager.getConnection(cfg.getDbConnectionString(), cfg.getDbUser(), cfg.getDbPass());
	}

	public List<Integer> calculate(int numTypes) {
		List<Integer> types = new ArrayList<Integer>(numTypes);

		try {
			PreparedStatement stmt = con.prepareStatement(SELECT_SQL);
			stmt.setInt(1, numTypes);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				types.add(rs.getInt("typeId"));
			}

			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return types;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Specify only one argument: the number of typeId's you want for the top X markets in The Forge");
			System.exit(1);
		}

		// parse the argument
		Integer numTypes = 0;
		try {
			numTypes = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("The argument '" + args[0] + "' is not a valid integer");
			System.exit(1);
		}

		List<Integer> types = new ArrayList<Integer>(0);
		try {
			TopTypes o = new TopTypes();
			types = o.calculate(numTypes);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		for (Integer i : types) {
			System.out.println(i);
		}

		System.exit(0);
	}
}
