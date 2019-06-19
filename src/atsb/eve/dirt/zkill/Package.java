package atsb.eve.dirt.zkill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Package {

	@SerializedName("killID")
	@Expose
	private int killID;
	@SerializedName("killmail")
	@Expose
	private Killmail killmail;
	@SerializedName("zkb")
	@Expose
	private Zkb zkb;

	public int getKillID() {
		return killID;
	}

	public void setKillID(int killID) {
		this.killID = killID;
	}

	public Killmail getKillmail() {
		return killmail;
	}

	public void setKillmail(Killmail killmail) {
		this.killmail = killmail;
	}

	public Zkb getZkb() {
		return zkb;
	}

	public void setZkb(Zkb zkb) {
		this.zkb = zkb;
	}

}