package atsb.eve.dirt.task;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.util.Utils;

/**
 * Periodically refresh derived tables
 * 
 * @author austin
 */
public class DerivedTableTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private static final String DENTITY_TRUNCATE_SQL = "DELETE FROM dentity;";
	private static final String DENTITY_INSERT_SQL = "INSERT INTO dentity"
			+ " SELECT `character`.`charId` AS `id`,`character`.`charName` AS `name` FROM `character`"
			+ " UNION SELECT `corporation`.`corpId` AS `id`,`corporation`.`corpName` AS `name` FROM `corporation`"
			+ " UNION SELECT  `alliance`.`allianceId` AS `id`, `alliance`.`allianceName` AS `name` FROM `alliance`;";
	private static final String DLOCATION_TRUNCATE_SQL = "DELETE FROM dlocation;";
	private static final String DLOCATION_INSERT_SQL = "INSERT INTO `dlocation`"
			+ " SELECT `stationId` AS locationId, `stationName` AS locationName FROM `station`"
			+ " UNION ALL SELECT `structId` AS locationId, `structName` AS locationName FROM `structure`;";

	@Override
	public String getTaskName() {
		return "derived-tables";
	}

	@Override
	protected void runTask() {
		updateDerivedTable(DENTITY_TRUNCATE_SQL, DENTITY_INSERT_SQL);
		updateDerivedTable(DLOCATION_TRUNCATE_SQL, DLOCATION_INSERT_SQL);
	}

	private void updateDerivedTable(String truncateSql, String insertSql) {
		PreparedStatement t = null;
		PreparedStatement i = null;
		try {
			getDb().setAutoCommit(false);
			t = getDb().prepareStatement(truncateSql);
			t.execute();
			i = getDb().prepareStatement(insertSql);
			i.execute();
			getDb().commit();
			getDb().setAutoCommit(true);
		} catch (SQLException e) {
			log.error("Failed to update derived table" + ": " + e.getLocalizedMessage());
			log.debug(e);
		} finally {
			Utils.closeQuietly(i);
			Utils.closeQuietly(t);
		}
	}
}
