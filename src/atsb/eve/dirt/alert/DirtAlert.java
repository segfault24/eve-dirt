package atsb.eve.dirt.alert;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.db.NotificationTable;
import atsb.eve.dirt.task.DirtTask;
import atsb.eve.model.Alert;
import atsb.eve.model.Notification;

public abstract class DirtAlert extends DirtTask {

	private static Logger log = LogManager.getLogger();

	/**
	 * @return subclass should provide the base alert
	 */
	protected abstract Alert getAlert();

	/**
	 * @return if notification is necessary
	 */
	protected abstract boolean shouldNotify();

	/**
	 * @return new notification
	 */
	protected abstract Notification buildNotification();

	@Override
	public String getTaskName() {
		return "alert-" + getAlert().getAlertId();
	}

	@Override
	protected void runTask() {
		setSaveStatus(false);

		if (getAlert() == null) {
			log.fatal("Alert details were not provided");
			return;
		}

		// check if should run
		List<Notification> notifs;
		try {
			notifs = NotificationTable.getNotificationsByAlert(getDb(), getAlert().getAlertId());
		} catch (SQLException e) {
			log.fatal("Failed to query for existing notifications for alert " + getAlert().getAlertId(), e);
			return;
		}
		for (Notification n : notifs) {
			// check if there's an unacknowledged notification for this alert already
			if (!n.isAcknowledged()) {
				log.debug("An unacked notification already exists for this alert");
				return;
			}
		}

		// run alert and create notification if necessary
		if (shouldNotify()) {
			log.debug("Creating new notification for alert " + getAlert().getAlertId());
			Notification n = buildNotification();
			if (n == null) {
				log.fatal("Failed to build notification");
				return;
			}
			n.setTime(new Timestamp(System.currentTimeMillis()));
			n.setAlertId(getAlert().getAlertId());
			n.setUserId(getAlert().getUserId());
			n.setAcknowledged(false);
			n.setSent(false);
			try {
				NotificationTable.insert(getDb(), n);
			} catch (SQLException e) {
				log.fatal("Failed to create new notification", e);
			}
		}
	}

}
