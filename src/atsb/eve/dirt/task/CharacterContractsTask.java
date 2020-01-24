package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.ApiAuthTable;
import atsb.eve.db.ContractTable;
import atsb.eve.db.NotificationTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.ContractsApiWrapper;
import atsb.eve.dirt.esi.auth.OAuthUtil;
import atsb.eve.model.Contract;
import atsb.eve.model.Notification;
import atsb.eve.model.OAuthUser;
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

		// TODO: make these configurable by the user
		boolean notifyOnInProgress = true;
		boolean notifyOnFinished = true;
		boolean notifyOnRejected = true;
		boolean notifyOnFailed = true;

		// iterate through the pages
		ContractsApiWrapper capiw = new ContractsApiWrapper(getDb());
		List<GetCharactersCharacterIdContracts200Ok> contracts = new ArrayList<>();
		int page = 0;
		int totalContracts = 0;
		do {
			page++;
			try {
				contracts = capiw.getCharacterContracts(charId, page, OAuthUtil.getAuthToken(getDb(), auth));
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
				Contract c = TypeUtil.convert(gc);
				l.add(c);
			}

			// check for notable conditions on contracts issued by this character
			for (Contract contract : l) {
				if (contract.getIssuerId() == charId) {
					Contract dbContract;
					try {
						dbContract = ContractTable.selectById(getDb(), contract.getContractId());
					} catch (SQLException e) {
						log.error("Failed to query for contract information " + contract.getContractId(), e);
						continue;
					}
					// skip when we already have a record of this contract and the status
					// is the same, that way, we only notify on previously unseen contracts
					// and all status changes
					if (dbContract != null && dbContract.getStatus().equalsIgnoreCase(contract.getStatus())) {
						continue;
					}
					Notification n = new Notification();
					n.setTitle("Contract Update");
					n.setUserId(auth.getUserId());
					n.setTime(new Timestamp(System.currentTimeMillis()));

					boolean notify = true;
					if (notifyOnFailed && contract.getStatus().equalsIgnoreCase(Contract.STATUS_FAILED)) {
						n.setText("Contract " + contract.getContractId() + " was Failed");
					} else if (notifyOnRejected
							&& contract.getStatus().equalsIgnoreCase(Contract.STATUS_REJECTED)) {
						n.setText("Contract " + contract.getContractId() + " was Rejected");
					} else if (notifyOnFinished
							&& contract.getStatus().equalsIgnoreCase(Contract.STATUS_FINISHED)) {
						n.setText("Contract " + contract.getContractId() + " was Completed");
					} else if (notifyOnInProgress
							&& contract.getStatus().equalsIgnoreCase(Contract.STATUS_IN_PROGRESS)) {
						n.setText("Contract " + contract.getContractId() + " is now In Progress");
					} else {
						notify = false;
					}

					if (notify) {
						try {
							NotificationTable.insert(getDb(), n);
						} catch (Exception e) {
							log.error("Failed to insert notification for contract status change", e);
						}
					}
				}
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
