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
import atsb.eve.model.Contract.ContractStatus;
import atsb.eve.model.Contract.ContractType;
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

		// get the user's other characters too
		List<Integer> otherCharIds;
		try {
			otherCharIds = ApiAuthTable.getCharsByUserId(getDb(), auth.getUserId());
			if (otherCharIds == null) {
				log.fatal("No auth details found for user=" + auth.getUserId());
				return;
			}
		} catch (SQLException e) {
			log.fatal("Failed to get auth details for user=" + auth.getUserId());
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
				checkNotifications(auth, contract, otherCharIds);
			}

			// check if contracts haven't ever been seen, so we can get the items
			// but don't queue the retrieval tasks until after we insert the contracts
			List<DirtTask> tasks = checkContractItems(auth, l);

			try {
				ContractTable.upsertMany(getDb(), l);
				log.debug("Inserted " + contracts.size() + " contracts for character " + charId + " page " + page);
			} catch (SQLException e) {
				log.error("Unexpected failure while processing page " + page + " for character " + charId, e);
			}

			// queue explicitly after contract insert because of foreign key constraint
			getDaemon().addTasks(tasks);
		} while (contracts.size() > 0);

		log.debug("Inserted " + totalContracts + " total contracts for character " + charId);
	}

	private List<DirtTask> checkContractItems(OAuthUser auth, List<Contract> contracts) {
		List<DirtTask> tasks = new ArrayList<DirtTask>();
		for (Contract contract : contracts) {
			if (contract.getType() == ContractType.ITEM_EXCHANGE) {
				try {
					Contract c = ContractTable.selectById(getDb(), contract.getContractId());
					if (c == null) {
						// we haven't seen this contract before, get the items
						tasks.add(new ContractItemsTask(contract.getContractId(), auth.getKeyId()));
					}
				} catch (SQLException e) {
					log.error("Failed to search for contract", e);
				}
			}
		}
		return tasks;
	}

	private void checkNotifications (OAuthUser auth, Contract contract, List<Integer> otherCharIds) {
		// TODO: make these configurable by the user
		boolean notifyOnInProgress = true;
		boolean notifyOnFinished = true;
		boolean notifyOnRejected = true;
		boolean notifyOnFailed = true;
		boolean ignoreFromAlts = true;

		if (contract.getIssuerId() == charId) {
			Contract dbContract;
			try {
				dbContract = ContractTable.selectById(getDb(), contract.getContractId());
			} catch (SQLException e) {
				log.error("Failed to query for contract information " + contract.getContractId(), e);
				return;
			}
			// skip when we already have a record of this contract and the status
			// is the same, that way, we only notify on previously unseen contracts
			// and all status changes
			if (dbContract != null && dbContract.getStatus() == contract.getStatus()) {
				return;
			}

			// skip when trading between alts
			for (int otherCharId : otherCharIds) {
				if (ignoreFromAlts && contract.getAcceptorId() == otherCharId) {
					return;
				}
			}

			Notification n = new Notification();
			n.setUserId(auth.getUserId());
			n.setTime(new Timestamp(System.currentTimeMillis()));
			boolean notify = true;
			if (notifyOnFailed && contract.getStatus() == ContractStatus.FAILED) {
				n.setTitle("Contract Failed");
				n.setText("Contract " + contract.getContractId() + " was Failed");
			} else if (notifyOnRejected && contract.getStatus() == ContractStatus.REJECTED) {
				n.setTitle("Contract Rejected");
				n.setText("Contract " + contract.getContractId() + " was Rejected");
			} else if (notifyOnFinished && contract.getStatus() == ContractStatus.FINISHED) {
				n.setTitle("Contract Completed");
				n.setText("Contract " + contract.getContractId() + " was Completed");
			} else if (notifyOnInProgress && contract.getStatus() == ContractStatus.IN_PROGRESS) {
				n.setTitle("Contract In Progress");
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

}
