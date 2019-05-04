package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.db.ApiAuthTable;
import atsb.eve.dirt.db.ContractTable;
import atsb.eve.dirt.esi.ContractsApiWrapper;
import atsb.eve.dirt.model.Contract;
import atsb.eve.dirt.model.OAuthUser;
import net.evetech.ApiException;
import net.evetech.esi.models.GetCharactersCharacterIdContracts200Ok;

/**
 * Task to retrieve contracts for a character.
 * 
 * @author austin
 */
public class CharacterContractsTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int charId;

	public CharacterContractsTask(int charId) {
		this.charId = charId;
	}

	@Override
	public String getTaskName() {
		return "character-contracts-" + charId;
	}

	@Override
	protected void runTask() {
		// get api auth info
		OAuthUser auth;
		try {
			auth = ApiAuthTable.getUserByCharId(getDb(), charId);
			if (auth == null) {
				log.fatal("No auth details found for char=" + charId);
				return;
			}
		} catch (SQLException e) {
			log.fatal("Failed to get auth details for char=" + charId);
			return;
		}

		// iterate through the pages
		ContractsApiWrapper capiw = new ContractsApiWrapper(getDb());
		List<GetCharactersCharacterIdContracts200Ok> contracts = new ArrayList<>();
		int page = 0;
		int totalContracts = 0;
		do {
			page++;
			try {
				capiw.getCharacterContracts(charId, page, auth.getAuthToken());
			} catch (ApiException e) {
				if (e.getCode() == 304) {
					continue;
				} else {
					log.error("Failed to retrieve page " + page + " of contracts for character " + charId, e);
					break;
				}
			}
			log.debug("Retrieved " + contracts.size() + " contracts for character " + charId + " page " + page);
			if (contracts.isEmpty()) {
				break;
			}

			totalContracts += contracts.size();
			List<Contract> l = new ArrayList<Contract>(contracts.size());
			for (GetCharactersCharacterIdContracts200Ok gc : contracts) {
				Contract c = new Contract(gc);
				l.add(c);
			}
			try {
				getDb().setAutoCommit(false);
				ContractTable.insertMany(getDb(), l);
				getDb().commit();
				getDb().setAutoCommit(true);
				log.debug("Inserted " + contracts.size() + " contracts for character " + charId + " page " + page);
			} catch (SQLException e) {
				log.error("Unexpected failure while processing page " + page + " for character " + charId, e);
			}
		} while (contracts.size() > 0);

		log.debug("Inserted " + totalContracts + " total contracts for character " + charId);
	}

}
