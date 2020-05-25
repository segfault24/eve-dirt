package atsb.eve.dirt.task;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.CorpContractItemTable;
import atsb.eve.db.CorpContractTable;
import atsb.eve.db.DoctrineTable;
import atsb.eve.db.ListItemTable;
import atsb.eve.model.Contract;
import atsb.eve.model.ContractItem;
import atsb.eve.model.Doctrine;

/**
 * Task to scan alliance/corp contracts and determine doctrine stock quantities
 *
 * @author austin
 */
public class DoctrineTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	@Override
	public String getTaskName() {
		return "doctrines";
	}

	@Override
	protected void runTask() {
		// load all the doctrines and their contents
		List<Doctrine> doctrines;
		try {
			doctrines = DoctrineTable.getAllDoctrines(getDb());
		} catch (SQLException e) {
			log.fatal("Failed to retrieve list of doctrines");
			return;
		}
		log.debug("Found " + doctrines.size() + " doctrines");
		Map<Integer, Map<Integer, Integer>> dItemsMap = new HashMap<Integer, Map<Integer, Integer>>();
		for (Doctrine d : doctrines) {
			Map<Integer, Integer> doctrineItems;
			try {
				doctrineItems = ListItemTable.getListItems(getDb(), d.getListId());
				dItemsMap.put(d.getDoctrineId(), doctrineItems);
			} catch (SQLException e) {
				log.error("Failed to retrieve items for doctrine " + d.getDoctrineId() + " (listId=" + d.getListId()
						+ ")");
				continue;
			}
		}
		log.debug("Loaded contents for " + dItemsMap.size() + " doctrines");

		// reset doctrine counts
		for (Doctrine d : doctrines) {
			d.setQuantity(0);
		}

		// scan contracts one by one
		List<Contract> contracts;
		try {
			contracts = CorpContractTable.selectOutstandingExchange(getDb());
		} catch (SQLException e1) {
			log.fatal(e1);
			log.fatal("Failed to retrieve outstanding item exchange contracts");
			return;
		}
		log.debug("Found " + contracts.size() + " contracts");
		for (Contract c : contracts) {
			List<ContractItem> contractItems;
			try {
				contractItems = CorpContractItemTable.selectByContractId(getDb(), c.getContractId());
			} catch (SQLException e) {
				log.error("Failed to retrieve items for contract " + c.getContractId());
				continue;
			}
			// check if the contract meets each doctrine
			// note that contracts can meet multiple doctrines!!
			for (Doctrine d : doctrines) {
				// check location first since that's fast
				if (c.getStartLocationId() != d.getLocationId()) {
					continue;
				}
				// grab the doctrine's items from the map we loaded before
				Map<Integer, Integer> doctrineItems = dItemsMap.get(d.getDoctrineId());
				if (doctrineItems == null) {
					continue;
				}
				// go through all the doctrine's items and see if the contract has enough of them
				boolean meets = true;
				for (int type : doctrineItems.keySet()) {
					int count = 0;
					for (ContractItem i : contractItems) {
						if (i.getTypeId() == type) {
							count += i.getQuantity();
						}
					}
					if (count < doctrineItems.get(type)) {
						// quantity of the item in the contract is less than the doctrine
						// requires, so continue on to the next doctrine
						meets = false;
						break;
					}
				}
				if (meets) {
					log.debug("Contract " + c.getContractId() + " meets doctrine " + d.getDoctrineId());
					d.incrementQuantity();
				}
			}
		}

		// update database with doctrine counts
		for (Doctrine d : doctrines) {
			log.debug("Doctrine " + d.getDoctrineId() + ": " + d.getQuantity());
			try {
				DoctrineTable.upsert(getDb(), d);
			} catch (SQLException e) {
				log.error("Failed to update doctrine " + d.getDoctrineId());
			}
		}

	}

}
