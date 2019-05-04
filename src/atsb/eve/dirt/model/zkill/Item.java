package atsb.eve.dirt.model.zkill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {

	@SerializedName("flag")
	@Expose
	private int flag;
	@SerializedName("item_type_id")
	@Expose
	private int itemTypeId;
	@SerializedName("quantity_destroyed")
	@Expose
	private int quantityDestroyed;
	@SerializedName("singleton")
	@Expose
	private int singleton;
	@SerializedName("quantity_dropped")
	@Expose
	private int quantityDropped;

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getItemTypeId() {
		return itemTypeId;
	}

	public void setItemTypeId(int itemTypeId) {
		this.itemTypeId = itemTypeId;
	}

	public int getQuantityDestroyed() {
		return quantityDestroyed;
	}

	public void setQuantityDestroyed(int quantityDestroyed) {
		this.quantityDestroyed = quantityDestroyed;
	}

	public int getSingleton() {
		return singleton;
	}

	public void setSingleton(int singleton) {
		this.singleton = singleton;
	}

	public int getQuantityDropped() {
		return quantityDropped;
	}

	public void setQuantityDropped(int quantityDropped) {
		this.quantityDropped = quantityDropped;
	}

}