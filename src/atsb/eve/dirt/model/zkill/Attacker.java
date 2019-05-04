package atsb.eve.dirt.model.zkill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attacker {

	@SerializedName("alliance_id")
	@Expose
	private int allianceId;
	@SerializedName("character_id")
	@Expose
	private int characterId;
	@SerializedName("corporation_id")
	@Expose
	private int corporationId;
	@SerializedName("damage_done")
	@Expose
	private int damageDone;
	@SerializedName("final_blow")
	@Expose
	private boolean finalBlow;
	@SerializedName("security_status")
	@Expose
	private double securityStatus;
	@SerializedName("ship_type_id")
	@Expose
	private int shipTypeId;
	@SerializedName("weapon_type_id")
	@Expose
	private int weaponTypeId;

	public int getAllianceId() {
		return allianceId;
	}

	public void setAllianceId(int allianceId) {
		this.allianceId = allianceId;
	}

	public int getCharacterId() {
		return characterId;
	}

	public void setCharacterId(int characterId) {
		this.characterId = characterId;
	}

	public int getCorporationId() {
		return corporationId;
	}

	public void setCorporationId(int corporationId) {
		this.corporationId = corporationId;
	}

	public int getDamageDone() {
		return damageDone;
	}

	public void setDamageDone(int damageDone) {
		this.damageDone = damageDone;
	}

	public boolean isFinalBlow() {
		return finalBlow;
	}

	public void setFinalBlow(boolean finalBlow) {
		this.finalBlow = finalBlow;
	}

	public double getSecurityStatus() {
		return securityStatus;
	}

	public void setSecurityStatus(double securityStatus) {
		this.securityStatus = securityStatus;
	}

	public int getShipTypeId() {
		return shipTypeId;
	}

	public void setShipTypeId(int shipTypeId) {
		this.shipTypeId = shipTypeId;
	}

	public int getWeaponTypeId() {
		return weaponTypeId;
	}

	public void setWeaponTypeId(int weaponTypeId) {
		this.weaponTypeId = weaponTypeId;
	}

}