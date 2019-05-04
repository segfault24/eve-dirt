package atsb.eve.dirt.model.zkill;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Victim {

	@SerializedName("alliance_id")
	@Expose
	private int allianceId;
	@SerializedName("character_id")
	@Expose
	private int characterId;
	@SerializedName("corporation_id")
	@Expose
	private int corporationId;
	@SerializedName("damage_taken")
	@Expose
	private int damageTaken;
	@SerializedName("items")
	@Expose
	private List<Item> items = null;
	@SerializedName("position")
	@Expose
	private Position position;
	@SerializedName("ship_type_id")
	@Expose
	private int shipTypeId;

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

	public int getDamageTaken() {
		return damageTaken;
	}

	public void setDamageTaken(int damageTaken) {
		this.damageTaken = damageTaken;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public int getShipTypeId() {
		return shipTypeId;
	}

	public void setShipTypeId(int shipTypeId) {
		this.shipTypeId = shipTypeId;
	}

}