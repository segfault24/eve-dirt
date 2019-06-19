package atsb.eve.dirt.zkill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Zkb {

	@SerializedName("locationID")
	@Expose
	private int locationID;
	@SerializedName("hash")
	@Expose
	private String hash;
	@SerializedName("fittedValue")
	@Expose
	private double fittedValue;
	@SerializedName("totalValue")
	@Expose
	private double totalValue;
	@SerializedName("points")
	@Expose
	private int points;
	@SerializedName("npc")
	@Expose
	private boolean npc;
	@SerializedName("solo")
	@Expose
	private boolean solo;
	@SerializedName("awox")
	@Expose
	private boolean awox;
	@SerializedName("href")
	@Expose
	private String href;

	public int getLocationID() {
		return locationID;
	}

	public void setLocationID(int locationID) {
		this.locationID = locationID;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public double getFittedValue() {
		return fittedValue;
	}

	public void setFittedValue(double fittedValue) {
		this.fittedValue = fittedValue;
	}

	public double getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public boolean isNpc() {
		return npc;
	}

	public void setNpc(boolean npc) {
		this.npc = npc;
	}

	public boolean isSolo() {
		return solo;
	}

	public void setSolo(boolean solo) {
		this.solo = solo;
	}

	public boolean isAwox() {
		return awox;
	}

	public void setAwox(boolean awox) {
		this.awox = awox;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

}