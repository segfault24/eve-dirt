package atsb.eve.dirt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import atsb.eve.dirt.model.Contract;

public class ContractTable {

	private static final String INSERT_SQL = "INSERT INTO contract ("
			+ "`contractId`,`issuerId`,`issuerCorpId`,`assigneeId`,`acceptorId`,"
			+ "`availability`,`dateIssued`,`dateExpired`,`status`,`type`"
			+ ") VALUES (?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE "
			+ "`acceptorId`=VALUES(`acceptorId`), `status`=VALUES(`status`)";

	private static final int BATCH_SIZE = 1000;

	public static void insertMany(Connection db, List<Contract> l) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(INSERT_SQL);
		int count = 0;
		for (Contract c : l) {
			stmt.setInt(1, c.getContractId());
			stmt.setInt(2, c.getIssuerId());
			stmt.setInt(3,  c.getIssuerCorpId());
			stmt.setInt(4,  c.getAssigneeId());
			stmt.setInt(5,  c.getAcceptorId());
			stmt.setString(6, c.getAvailability());
			stmt.setTimestamp(7, c.getDateIssued());
			stmt.setTimestamp(8, c.getDateExpired());
			stmt.setString(9, c.getStatus());
			stmt.setString(10, c.getType());
			stmt.addBatch();
			count++;
			if (count % BATCH_SIZE == 0 || count == l.size()) {
				stmt.executeBatch();
			}
		}
	}

}
