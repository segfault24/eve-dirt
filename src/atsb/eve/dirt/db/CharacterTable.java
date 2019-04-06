package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import atsb.eve.dirt.model.Character;

public class CharacterTable {

	private static final String INSERT_SQL = "";

	public static void insert(Connection db, Character c) throws SQLException {
		PreparedStatement stmt;
		stmt = db.prepareStatement(INSERT_SQL);
		stmt.execute();
	}
}
