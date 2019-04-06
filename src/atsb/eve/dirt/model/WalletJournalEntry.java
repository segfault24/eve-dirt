package atsb.eve.dirt.model;

import java.sql.Timestamp;

import net.evetech.esi.models.GetCharactersCharacterIdWalletJournal200Ok;

public class WalletJournalEntry {

	private long journalId;
	private int charId;
	private Timestamp date;
	private double amount;
	private double balance;
	private double tax;
	private int firstPartyId;
	private int secondPartyId;
	private int taxReceiverId;
	private String description;
	private String reason;
	private String refType;
	private long contextId;
	private String contextIdType;

	public WalletJournalEntry(GetCharactersCharacterIdWalletJournal200Ok j) {
		setJournalId(j.getId());
		setDate(new Timestamp(j.getDate().getMillis()));
		setAmount(j.getAmount());
		setBalance(j.getBalance());
		if (j.getTax() != null)
			setTax(j.getTax());
		if (j.getFirstPartyId() != null)
			setFirstPartyId(j.getFirstPartyId());
		if (j.getSecondPartyId() != null)
			setSecondPartyId(j.getSecondPartyId());
		if (j.getTaxReceiverId() != null)
			setTaxReceiverId(j.getTaxReceiverId());
		if (j.getDescription() != null)
			setDescription(j.getDescription());
		if (j.getReason() != null)
			setReason(j.getReason());
		if (j.getRefType() != null)
			setRefType(j.getRefType().toString());
		if (j.getContextId() != null)
			setContextId(j.getContextId());
		if (j.getContextIdType() != null)
			setContextIdType(j.getContextIdType().toString());
	}

	public long getJournalId() {
		return journalId;
	}

	public void setJournalId(long journalId) {
		this.journalId = journalId;
	}

	public int getCharId() {
		return charId;
	}

	public void setCharId(int charId) {
		this.charId = charId;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public int getFirstPartyId() {
		return firstPartyId;
	}

	public void setFirstPartyId(int firstPartyId) {
		this.firstPartyId = firstPartyId;
	}

	public int getSecondPartyId() {
		return secondPartyId;
	}

	public void setSecondPartyId(int secondPartyId) {
		this.secondPartyId = secondPartyId;
	}

	public int getTaxReceiverId() {
		return taxReceiverId;
	}

	public void setTaxReceiverId(int taxReceiverId) {
		this.taxReceiverId = taxReceiverId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getRefType() {
		return refType;
	}

	public void setRefType(String refType) {
		this.refType = refType;
	}

	public long getContextId() {
		return contextId;
	}

	public void setContextId(long contextId) {
		this.contextId = contextId;
	}

	public String getContextIdType() {
		return contextIdType;
	}

	public void setContextIdType(String contextIdType) {
		this.contextIdType = contextIdType;
	}

}
