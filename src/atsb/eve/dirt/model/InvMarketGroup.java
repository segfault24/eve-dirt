package atsb.eve.dirt.model;

import net.evetech.esi.models.GetMarketsGroupsMarketGroupIdOk;

public class InvMarketGroup {

	private int marketGroupId;
	private int parentGroupId;
	private String marketGroupName;
	private String description;
	private boolean hasTypes;

	public InvMarketGroup(GetMarketsGroupsMarketGroupIdOk group) {
		marketGroupId = group.getMarketGroupId();
		parentGroupId = group.getParentGroupId();
		marketGroupName = group.getName();
		description = group.getDescription();
		hasTypes = !group.getTypes().isEmpty();
	}

	public int getMarketGroupId() {
		return marketGroupId;
	}

	public void setMarketGroupId(int marketGroupId) {
		this.marketGroupId = marketGroupId;
	}

	public int getParentGroupId() {
		return parentGroupId;
	}

	public void setParentGroupId(int parentGroupId) {
		this.parentGroupId = parentGroupId;
	}

	public String getMarketGroupName() {
		return marketGroupName;
	}

	public void setMarketGroupName(String marketGroupName) {
		this.marketGroupName = marketGroupName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean hasTypes() {
		return hasTypes;
	}

	public void setHasTypes(boolean hasTypes) {
		this.hasTypes = hasTypes;
	}

}
