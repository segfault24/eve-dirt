package atsb.eve.dirt.model;

import java.sql.Date;

import net.evetech.esi.models.GetMarketsRegionIdHistory200Ok;

public class MarketHistoryEntry {

	private int typeId;
	private int regionId;
	private Date date;
	private double highest;
	private double average;
	private double lowest;
	private long volume;
	private long orderCount;

	public MarketHistoryEntry(GetMarketsRegionIdHistory200Ok h) {
		setDate(Date.valueOf(h.getDate().toString()));
		setHighest(h.getHighest());
		setAverage(h.getAverage());
		setLowest(h.getLowest());
		setOrderCount(h.getOrderCount());
		setVolume(h.getVolume());
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getHighest() {
		return highest;
	}

	public void setHighest(double highest) {
		this.highest = highest;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public double getLowest() {
		return lowest;
	}

	public void setLowest(double lowest) {
		this.lowest = lowest;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(long orderCount) {
		this.orderCount = orderCount;
	}
}
