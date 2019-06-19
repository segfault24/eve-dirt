package atsb.eve.dirt.zkill;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Killmail {

	@SerializedName("attackers")
	@Expose
	private List<Attacker> attackers = null;
	@SerializedName("killmail_id")
	@Expose
	private int killmailId;
	@SerializedName("killmail_time")
	@Expose
	private String killmailTime;
	@SerializedName("solar_system_id")
	@Expose
	private int solarSystemId;
	@SerializedName("victim")
	@Expose
	private Victim victim;

	public List<Attacker> getAttackers() {
		return attackers;
	}

	public void setAttackers(List<Attacker> attackers) {
		this.attackers = attackers;
	}

	public int getKillmailId() {
		return killmailId;
	}

	public void setKillmailId(int killmailId) {
		this.killmailId = killmailId;
	}

	public String getKillmailTime() {
		return killmailTime;
	}

	public void setKillmailTime(String killmailTime) {
		this.killmailTime = killmailTime;
	}

	public int getSolarSystemId() {
		return solarSystemId;
	}

	public void setSolarSystemId(int solarSystemId) {
		this.solarSystemId = solarSystemId;
	}

	public Victim getVictim() {
		return victim;
	}

	public void setVictim(Victim victim) {
		this.victim = victim;
	}

}
