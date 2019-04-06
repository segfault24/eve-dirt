package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import atsb.eve.dirt.model.WalletJournalEntry;

public class WalletJournalTable {

	private static final String INSERT_IGNORE_SQL = "INSERT IGNORE INTO `walletJournal` (`journalId`,`charId`,`date`,`amount`,`balance`,`tax`,`firstPartyId`,`secondPartyId`,`taxReceiverId`,`description`,`reason`,`refType`,`contextId`,`contextIdType`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static boolean insertMany(Connection db, List<WalletJournalEntry> js) {
		boolean result = true;
		try {
			PreparedStatement stmt = db.prepareStatement(INSERT_IGNORE_SQL);
			for (WalletJournalEntry j : js) {
				stmt.setLong(1, j.getJournalId());
				stmt.setInt(2, j.getCharId());
				stmt.setTimestamp(3, j.getDate());
				stmt.setDouble(4, j.getAmount());
				stmt.setDouble(5, j.getBalance());
				stmt.setDouble(6, j.getTax());
				stmt.setInt(7, j.getFirstPartyId());
				stmt.setInt(8, j.getSecondPartyId());
				stmt.setInt(9, j.getTaxReceiverId());
				stmt.setString(10, j.getDescription());
				stmt.setString(11, j.getReason());
				stmt.setString(12, j.getRefType());
				stmt.setLong(13, j.getContextId());
				stmt.setString(14, j.getContextIdType());
				stmt.addBatch();
			}
			int[] rs = stmt.executeBatch();

			// if any of the inserts returned non-one, it was probably a duplicate
			for (int r: rs) {
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
