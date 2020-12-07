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
			log.fatal("Failed to retrieve list of doctrines: " + e.getLocalizedMessage());
			log.debug(e);
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
						+ "): " + e.getLocalizedMessage());
				log.debug(e);
				continue;
			}
		}
		log.debug("Loaded contents for " + dItemsMap.size() + " doctrines");

		// reset doctrine counts
		for (Doctrine d : doctrines) {
			d.setQuantity(0);
			d.setLowestPrice(Double.MAX_VALUE);
		}

		// scan contracts one by one
		List<Contract> contracts;
		try {
			contracts = CorpContractTable.selectOutstandingExchange(getDb());
		} catch (SQLException e) {
			log.fatal("Failed to retrieve outstanding item exchange contracts");
			log.fatal(e);
			return;
		}
		log.debug("Found " + contracts.size() + " contracts");
		for (Contract c : contracts) {
			List<ContractItem> contractItems;
			try {
				contractItems = CorpContractItemTable.selectByContractId(getDb(), c.getContractId());
			} catch (SQLException e) {
				log.error("Failed to retrieve items for contract " + c.getContractId() + ": " + e.getLocalizedMessage());
				log.debug(e);
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
					if (c.getPrice() < d.getLowestPrice()) {
						d.setLowestPrice(c.getPrice());
					}
				}
			}
		}

		// update database with doctrine counts
		for (Doctrine d : doctrines) {
			if (d.getLowestPrice() == Double.MAX_VALUE) {
				d.setLowestPrice(0);
			}
			log.debug("doctrine:" + d.getDoctrineId() + " - qt:" + d.getQuantity() + " - min:" + d.getLowestPrice());
			try {
				DoctrineTable.upsert(getDb(), d);
			} catch (SQLException e) {
				log.error("Failed to update doctrine " + d.getDoctrineId() + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
		}

	}

}
