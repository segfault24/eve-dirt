package atsb.eve.dirt.model;

import java.sql.Timestamp;

import net.evetech.esi.models.GetCharactersCharacterIdOrders200Ok;
import net.evetech.esi.models.GetMarketsRegionIdOrders200Ok;
import net.evetech.esi.models.GetMarketsStructuresStructureId200Ok;

public class MarketOrder {

	private Timestamp issued;
	private String range;
	private boolean buyOrder;
	private int duration;
	private long orderId;
	private int volumeRemain;
	private int minVolume;
	private int typeId;
	private int volumeTotal;
	private long locationId;
	private double price;
	private int region;
	private Timestamp retrieved;
	private Source source;
	private int charId;

	public MarketOrder(GetMarketsRegionIdOrders200Ok o) {
		setIssued(new Timestamp(o.getIssued().getMillis()));
		setRange(o.getRange().toString());
		setBuyOrder(o.getIsBuyOrder());
		setDuration(o.getDuration());
		setOrderId(o.getOrderId());
		setVolumeRemain(o.getVolumeRemain());
		setMinVolume(o.getMinVolume());
		setTypeId(o.getTypeId());
		setVolumeTotal(o.getVolumeTotal());
		setLocationId(o.getLocationId());
		setPrice(o.getPrice());
		setSource(Source.PUBLIC);
	}

	public MarketOrder(GetMarketsStructuresStructureId200Ok o) {
		setIssued(new Timestamp(o.getIssued().getMillis()));
		setRange(o.getRange().toString());
		setBuyOrder(o.getIsBuyOrder());
		setDuration(o.getDuration());
		setOrderId(o.getOrderId());
		setVolumeRemain(o.getVolumeRemain());
		setMinVolume(o.getMinVolume());
		setTypeId(o.getTypeId());
		setVolumeTotal(o.getVolumeTotal());
		setLocationId(o.getLocationId());
		setPrice(o.getPrice());
		setSource(Source.STRUCTURE);
	}

	public MarketOrder(GetCharactersCharacterIdOrders200Ok o) {
		if (o.getIssued() != null)
			setIssued(new Timestamp(o.getIssued().getMillis()));
		if (o.getRange() != null)
			setRange(o.getRange().toString());
		if (o.getIsBuyOrder() != null)
			setBuyOrder(o.getIsBuyOrder());
		if (o.getDuration() != null)
			setDuration(o.getDuration());
		if (o.getOrderId() != null)
			setOrderId(o.getOrderId());
		if (o.getVolumeRemain() != null)
			setVolumeRemain(o.getVolumeRemain());
		if (o.getMinVolume() != null)
			setMinVolume(o.getMinVolume());
		if (o.getTypeId() != null)
			setTypeId(o.getTypeId());
		if (o.getVolumeTotal() != null)
			setVolumeTotal(o.getVolumeTotal());
		if (o.getLocationId() != null)
			setLocationId(o.getLocationId());
		if (o.getPrice() != null)
			setPrice(o.getPrice());
		if (o.getRegionId() != null)
			setRegion(o.getRegionId());
		setSource(Source.CHARACTER);
	}

	public Timestamp getIssued() {
		return issued;
	}

	public void setIssued(Timestamp issued) {
		this.issued = issued;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public boolean isBuyOrder() {
		return buyOrder;
	}

	public void setBuyOrder(boolean buyOrder) {
		this.buyOrder = buyOrder;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public int getVolumeRemain() {
		return volumeRemain;
	}

	public void setVolumeRemain(int volumeRemain) {
		this.volumeRemain = volumeRemain;
	}

	public int getMinVolume() {
		return minVolume;
	}

	public void setMinVolume(int minVolume) {
		this.minVolume = minVolume;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getVolumeTotal() {
		return volumeTotal;
	}

	public void setVolumeTotal(int volumeTotal) {
		this.volumeTotal = volumeTotal;
	}

	public long getLocationId() {
		return locationId;
	}

	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getRegion() {
		return region;
	}

	public void setRegion(int region) {
		this.region = region;
	}

	public Timestamp getRetrieved() {
		return retrieved;
	}

	public void setRetrieved(Timestamp retrieved) {
		this.retrieved = retrieved;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public int getCharId() {
		return charId;
	}

	public void setCharId(int charId) {
		this.charId = charId;
	}

	public enum Source {
		PUBLIC(0), STRUCTURE(1), CHARACTER(2);

		private final int value;

		private Source(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

}
