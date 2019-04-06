package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import atsb.eve.dirt.model.Corporation;

public class CorporationTable {

	private static final String INSERT_SQL = "INSERT INTO corporation (`corpId`,`corpName`,`ticker`,`allianceId`,`ceo`,`creator`,`creationDate`,`memberCount`,`taxRate`,`url`)"
			+ " VALUES (?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE `allianceId`=VALUES(`allianceId`),`ceo`=VALUES(`ceo`),`memberCount`=VALUES(`memberCount`),`taxRate`=VALUES(`taxRate`),`url`=VALUES(`url`)";

	public static void insert(Connection db, Corporation s) throws SQLException {
		PreparedStatement stmt;
		stmt = db.prepareStatement(INSERT_SQL);
		stmt.setInt(1, s.getCorpId());
		stmt.setString(2, s.getCorpName());
		stmt.setString(3, s.getTicker());
		stmt.setInt(4, s.getAllianceId());
		stmt.setInt(5, s.getCeoId());
		stmt.setInt(6, s.getCreatorId());
		stmt.setDate(7, s.getCreationDate());
		stmt.setInt(8, s.getMemberCount());
		stmt.setDouble(9, s.getTaxRate());
		stmt.setString(10, s.getUrl());
		stmt.execute();
	}

}
