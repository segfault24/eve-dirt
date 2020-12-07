package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.ApiAuthTable;
import atsb.eve.db.ContractItemTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.ContractsApiWrapper;
import atsb.eve.dirt.esi.auth.OAuthUtil;
import atsb.eve.model.ContractItem;
import atsb.eve.model.OAuthUser;
import net.evetech.ApiException;
import net.evetech.esi.models.GetCharactersCharacterIdContractsContractIdItems200Ok;

/**
 * Task to retrieve items in a contract.
 * 
 * @author austin
 */
public class ContractItemsTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int contractId;
	private int keyId;

	public ContractItemsTask(int contractId, int keyId) {
		this.contractId = contractId;
		this.keyId = keyId;
	}

	@Override
	public String getTaskName() {
		return "contract-items-" + contractId;
	}

	@Override
	protected void runTask() {
		// get api auth info
		OAuthUser auth;
		try {
			auth = ApiAuthTable.getUserByKeyId(getDb(), keyId);
			if (auth == null) {
				log.fatal("No auth details found for key=" + keyId);
				return;
			}
		} catch (SQLException e) {
			log.fatal("Failed to get auth details for key=" + keyId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}

		ContractsApiWrapper capiw = new ContractsApiWrapper(getDb());
		List<GetCharactersCharacterIdContractsContractIdItems200Ok> items;
		try {
			items = capiw.getCharacterContractItems(auth.getCharId(), contractId,
					OAuthUtil.getAuthToken(getDb(), auth));
		} catch (ApiException e) {
			log.error("Failed to retrieve items for contract " + contractId + ": " + e.getLocalizedMessage());
			log.debug(e);
			return;
		}
		log.debug("Retrieved " + items.size() + " items for contract " + contractId);

		List<ContractItem> l = new ArrayList<ContractItem>(items.size());
		for (GetCharactersCharacterIdContractsContractIdItems200Ok i : items) {
			ContractItem ci = TypeUtil.convert(i);
			ci.setContractId(contractId);
			l.add(ci);
		}

		try {
			getDb().setAutoCommit(false);
			ContractItemTable.deleteByContractId(getDb(), contractId);
			ContractItemTable.insertMany(getDb(), l);
			getDb().commit();
			getDb().setAutoCommit(true);
			log.debug("Inserted " + l.size() + " items for contract " + contractId);
		} catch (SQLException e) {
			log.error("Unexpected failure while processing items for contract " + contractId + ": " + e.getLocalizedMessage());
			log.debug(e);
		}
	}

}
