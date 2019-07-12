package atsb.eve.dirt.alert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.NotificationTable;
import atsb.eve.dirt.task.DirtTask;
import atsb.eve.model.Alert;
import atsb.eve.model.Notification;

// When [param1=sell price/buy price/sell volume/buy volume]
// of [param2=typeId] in [param3=locationId/regionId]
// is [param4=gte/lte] [param5=number]
public class PriceVolumeAlert extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private Alert alert;

	public PriceVolumeAlert(Alert a) {
		setSaveStatus(false);
		this.alert = a;
	}

	@Override
	public String getTaskName() {
		return "alert-" + alert.getAlertId();
	}

	@Override
	protected void runTask() {
		if (alert == null || !alert.isEnabled()) {
			return;
		}

		List<Notification> notifs;
		try {
			notifs = NotificationTable.getNotificationsByAlert(getDb(), alert.getAlertId());
		} catch (SQLException e) {
			log.fatal("Failed to query for existing notifications for alert " + alert.getAlertId(), e);
			return;
		}
		for (Notification n : notifs) {
			// check if there's an unacknowledged notification for this alert already
			if (!n.isAcknowledged()) {
				return;
			}
		}

		// generate the appropriate SQL query from the params
		PreparedStatement stmt;
		try {
			stmt = parseParams();
		} catch (SQLException | IllegalArgumentException e) {
			log.fatal("Failed to parse alert parameters for alert " + alert.getAlertId(), e);
			return;
		}

		// execute the query and create a notification if necessary
		try {
			ResultSet rs = stmt.executeQuery();
			if (rs.next() && rs.getInt(1) == 1) {
				log.info("Generating notification for alert " + alert.getAlertId());
				Notification n = new Notification();
				n.setAlertId(alert.getAlertId());
				n.setUserId(alert.getUserId());
				n.setTime(new Timestamp(System.currentTimeMillis()));
				n.setTitle("testtitle");
				n.setText("testtext 123");
				n.setAcknowledged(false);
				NotificationTable.insert(getDb(), n);
			}
		} catch (SQLException e) {
			log.fatal("Failed to run query for alert " + alert.getAlertId(), e);
			return;
		}
	}

	private PreparedStatement parseParams() throws IllegalArgumentException, SQLException {
		boolean buy = false;
		String sql = "SELECT ";
		switch (alert.getParam1()) {
		case "sell price":
			sql += "MIN(`price`) ";
			break;
		case "buy price":
			sql += "MAX(`price`) ";
			buy = true;
			break;
		case "sell volume":
		case "buy volume":
			sql += "SUM(`volumeRemain`) ";
			buy = true;
			break;
		default:
			throw new IllegalArgumentException();
		}
		switch (alert.getParam4()) {
		case "gte":
			sql += ">= ? ";
			break;
		case "lte":
			sql += "<= ? ";
			break;
		default:
			throw new IllegalArgumentException();
		}
		sql += "FROM `marketOrder` WHERE `typeId`=? AND `isBuyOrder`=? ";
		sql += "AND (`locationId`=? OR `regionId`=?)";

		PreparedStatement stmt = getDb().prepareStatement(sql);
		stmt.setDouble(1, Double.parseDouble(alert.getParam5()));
		stmt.setInt(2, Integer.parseInt(alert.getParam2()));
		stmt.setBoolean(3, buy);
		long loc = Long.parseLong(alert.getParam3());
		stmt.setLong(4, loc);
		stmt.setLong(5, loc);

		return stmt;
	}

}
