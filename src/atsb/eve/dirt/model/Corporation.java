package atsb.eve.dirt.model;

import java.sql.Date;

import net.evetech.esi.models.GetCorporationsCorporationIdOk;

public class Corporation {

	private int corpId;
	private String corpName;
	private String ticker;
	private int allianceId;
	private int ceoId;
	private int creatorId;
	private Date creationDate;
	private int memberCount;
	private double taxRate;
	private String url;

	public Corporation(GetCorporationsCorporationIdOk c) {
		setCorpName(c.getName());
		setTicker(c.getTicker());
		setAllianceId(c.getAllianceId());
		setCeoId(c.getCeoId());
		setCreatorId(c.getCreatorId());
		setCreationDate(Date.valueOf(c.getDateFounded().toString()));
		setMemberCount(c.getMemberCount());
		setTaxRate(c.getTaxRate());
		setUrl(c.getUrl());
	}

	public int getCorpId() {
		return corpId;
	}

	public void setCorpId(int corpId) {
		this.corpId = corpId;
	}

	public String getCorpName() {
		return corpName;
	}

	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public int getAllianceId() {
		return allianceId;
	}

	public void setAllianceId(int allianceId) {
		this.allianceId = allianceId;
	}

	public int getCeoId() {
		return ceoId;
	}

	public void setCeoId(int ceoId) {
		this.ceoId = ceoId;
	}

	public int getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
