package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import atsb.eve.dirt.model.WalletTransaction;

public class WalletTransactionTable {

	private static final String INSERT_IGNORE_SQL = "INSERT IGNORE INTO `walletTransaction` (`transactionId`,`charId`,`clientId`,`date`,`isBuy`,`isPersonal`,`typeId`,`quantity`,`unitPrice`,`locationId`,`journalRefId`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	private static final String LATEST_SELECT_SQL = "SELECT `transactionId` FROM `walletTransaction` WHERE `charId`=? ORDER BY `date` DESC LIMIT 1;";

	public static long getLatestTransactionId(Connection db, int charId) {
		try {
			PreparedStatement stmt;
			stmt = db.prepareStatement(LATEST_SELECT_SQL);
			stmt.setInt(1, charId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getLong(1);
			} else {
				return 0;
			}
		} catch (SQLException e) {
			return 0;
		}
	}

	public static boolean insertMany(Connection db, List<WalletTransaction> ts) {
		boolean result = true;
		try {
			PreparedStatement stmt = db.prepareStatement(INSERT_IGNORE_SQL);
			for (WalletTransaction t : ts) {
				stmt.setLong(1, t.getTransactionId());
				stmt.setInt(2, t.getCharId());
				stmt.setInt(3, t.getClientId());
				stmt.setTimestamp(4, t.getDate());
				stmt.setBoolean(5,  t.isBuy());
				stmt.setBoolean(6, t.isPersonal());
				stmt.setInt(7, t.getTypeId());
				stmt.setInt(8, t.getQuantity());
				stmt.setDouble(9, t.getUnitPrice());
				stmt.setLong(10, t.getLocationId());
				stmt.setLong(11, t.getJournalRefId());
				stmt.addBatch();
			}
			int[] rs = stmt.executeBatch();

			// if any of the inserts returned non-one, it was probably a duplicate
			for (int r : rs) {
				if (r != 1) {
					result = false;
				}
			}
		} catch (SQLException e) {
			result = false;
		}
		return result;
	}

}
