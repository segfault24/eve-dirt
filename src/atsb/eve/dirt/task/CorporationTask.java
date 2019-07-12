package atsb.eve.dirt.task;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.CorporationTable;
import atsb.eve.dirt.TypeUtil;
import atsb.eve.dirt.esi.CorporationApiWrapper;
import atsb.eve.model.Corporation;
import net.evetech.ApiException;
import net.evetech.esi.models.GetCorporationsCorporationIdOk;

/**
 * Task to retrieve information about a corporation.
 * 
 * @author austin
 */
public class CorporationTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private int corpId;

	public CorporationTask(int corpId) {
		this.corpId = corpId;
		setSaveStatus(false);
	}

	@Override
	public String getTaskName() {
		return "corporation-" + corpId;
	}

	@Override
	public void runTask() {
		CorporationApiWrapper capiw = new CorporationApiWrapper(getDb());
		log.debug("Querying corporation information for corporation " + corpId);
		try {
			GetCorporationsCorporationIdOk info = capiw.getCorporation(corpId);
			Corporation c = TypeUtil.convert(info);
			c.setCorpId(corpId);
			CorporationTable.upsert(getDb(), c);
		} catch (ApiException e) {
			log.error("Failed to retrieve info for corporation " + corpId, e);
		} catch (SQLException e) {
			log.error("Failed to insert info for corporation " + corpId, e);
		}
		log.debug("Inserted information for corporation " + corpId);
	}

}
