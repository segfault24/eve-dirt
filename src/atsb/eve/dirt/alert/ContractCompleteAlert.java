package atsb.eve.dirt.alert;

import atsb.eve.model.Alert;
import atsb.eve.model.Notification;

public class ContractCompleteAlert extends DirtAlert {

	private Alert alert;

	public ContractCompleteAlert(Alert alert) {
		this.alert = alert;
	}

	@Override
	protected Alert getAlert() {
		return alert;
	}

	@Override
	protected boolean shouldNotify() {
		return true;
	}

	@Override
	protected Notification buildNotification() {
		Notification n = new Notification();
		n.setTitle("testttt title");
		n.setText("testttt text");
		n.setTypeId(2048);
		return n;
	}

}
