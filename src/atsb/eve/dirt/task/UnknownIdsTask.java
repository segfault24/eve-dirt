package atsb.eve.dirt.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.esi.UniverseApiWrapper;
import net.evetech.ApiException;
import net.evetech.esi.models.PostUniverseNames200Ok;

/**
 * Gets a list of character and corporation ids from wallet entries, contracts,
 * structures, etc and compares it to known ids to find any unresolved ids.
 * Those are then resolved via ESI and added to the database.
 * 
 * @author austin
 */
public class UnknownIdsTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private static final String SELECT_IDS_KNOWN_SQL = "SELECT `charId` FROM `character` "
			+ "UNION SELECT `corpId` FROM corporation";
	private static final String SELECT_IDS_UNKNOWN_SQL = "SELECT issuerId FROM contract "
			+ "UNION SELECT acceptorId FROM contract "
			+ "UNION SELECT assigneeId FROM contract "
			+ "UNION SELECT issuerCorpId FROM contract "
			+ "UNION SELECT corpId FROM structure "
			+ "UNION SELECT firstPartyId FROM walletJournal "
			+ "UNION SELECT secondPartyId FROM walletJournal "
			+ "UNION SELECT clientId FROM walletTransaction";

	private static final int BATCH_SIZE = 500;

	@Override
	public String getTaskName() {
		return "unknown-ids";
	}

	@Override
	protected void runTask() {
		// get list of all known corp + char ids in the db
		List<Integer> knowns;
		List<Integer> unknowns;
		try {
			knowns = getKnownIds(getDb());
			unknowns = getUnknownIds(getDb());
		} catch (SQLException e) {
			log.fatal("Failed to scan database for char and corp ids", e);
			return;
		}
		log.debug("Found " + knowns.size() + " known char and corp ids");
		log.debug("Found " + unknowns.size() + " unknown char and corp ids");

		// remove knowns from the unknowns
		HashSet<Integer> unresolved = new HashSet<Integer>(unknowns);
		unresolved.removeAll(knowns);
		unresolved.remove(0);
		List<Integer> search = new ArrayList<Integer>(unresolved);
		log.debug("There are " + search.size() + " ids to be resolved");

		// pump through ESI's id resolver
		UniverseApiWrapper uapiw = new UniverseApiWrapper(getDb());

		int batches = (search.size() - 1) / BATCH_SIZE + 1;
		for (int i=0; i<batches; i++) {
			int startIdx = i * BATCH_SIZE;
			int endIdx = Math.min(startIdx + BATCH_SIZE, search.size());
			List<Integer> sublist = search.subList(startIdx, endIdx);

			List<PostUniverseNames200Ok> names;
			try {
				names = uapiw.postUniverseNames(sublist);
			} catch (ApiException e) {
				log.fatal("Failed to query ESI for ids: " + e.getResponseBody(), e);
				return;
			}
	
			// spawn tasks
			for (PostUniverseNames200Ok name : names) {
				switch (name.getCategory()) {
				case CHARACTER:
					getDaemon().addTask(new CharacterTask(name.getId()));
					break;
				case CORPORATION:
					getDaemon().addTask(new CorporationTask(name.getId()));
					break;
				default:
					log.debug("Not a char or corp id: " + name.getId() + " " + name.getCategory().toString());
					break;
				}
			}
		}
	}

	private List<Integer> getKnownIds(Connection db) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(SELECT_IDS_KNOWN_SQL);
		ResultSet rs = stmt.executeQuery();
		List<Integer> ids = new ArrayList<Integer>();
		while (rs.next()) {
			ids.add(rs.getInt(1));
		}
		return ids;
	}

	private List<Integer> getUnknownIds(Connection db) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(SELECT_IDS_UNKNOWN_SQL);
		ResultSet rs = stmt.executeQuery();
		List<Integer> ids = new ArrayList<Integer>();
		while (rs.next()) {
			ids.add(rs.getInt(1));
		}
		return ids;
	}

}
