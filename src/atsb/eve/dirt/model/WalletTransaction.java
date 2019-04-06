package atsb.eve.dirt.model;

import java.sql.Timestamp;

import net.evetech.esi.models.GetCharactersCharacterIdWalletTransactions200Ok;

public class WalletTransaction {

	private long transactionId;
	private int charId;
	private int clientId;
	private Timestamp date;
	private boolean isBuy;
	private boolean isPersonal;
	private int typeId;
	private int quantity;
	private double unitPrice;
	private long locationId;
	private long journalRefId;

	public WalletTransaction(GetCharactersCharacterIdWalletTransactions200Ok t) {
		setTransactionId(t.getTransactionId());
		setClientId(t.getClientId());
		setDate(new Timestamp(t.getDate().getMillis()));
		setBuy(t.getIsBuy());
		setPersonal(t.getIsPersonal());
		setTypeId(t.getTypeId());
		setQuantity(t.getQuantity());
		setUnitPrice(t.getUnitPrice());
		setLocationId(t.getLocationId());
		setJournalRefId(t.getJournalRefId());
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public int getCharId() {
		return charId;
	}

	public void setCharId(int charId) {
		this.charId = charId;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public boolean isBuy() {
		return isBuy;
	}

	public void setBuy(boolean isBuy) {
		this.isBuy = isBuy;
	}

	public boolean isPersonal() {
		return isPersonal;
	}

	public void setPersonal(boolean isPersonal) {
		this.isPersonal = isPersonal;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public long getLocationId() {
		return locationId;
	}

	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	public long getJournalRefId() {
		return journalRefId;
	}

	public void setJournalRefId(long journalRefId) {
		this.journalRefId = journalRefId;
	}

}
