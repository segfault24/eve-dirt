package atsb.eve.dirt;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import atsb.eve.model.Character;
import atsb.eve.model.Constellation;
import atsb.eve.model.Contract;
import atsb.eve.model.Corporation;
import atsb.eve.model.InsurancePrice;
import atsb.eve.model.MarketGroup;
import atsb.eve.model.InvType;
import atsb.eve.model.MarketHistoryEntry;
import atsb.eve.model.MarketOrder;
import atsb.eve.model.Structure;
import atsb.eve.model.WalletJournalEntry;
import atsb.eve.model.WalletTransaction;
import atsb.eve.model.MarketOrder.Source;
import atsb.eve.model.Region;
import atsb.eve.model.SolarSystem;
import atsb.eve.model.Station;
import net.evetech.esi.models.GetCharactersCharacterIdContracts200Ok;
import net.evetech.esi.models.GetCharactersCharacterIdOk;
import net.evetech.esi.models.GetCharactersCharacterIdOrders200Ok;
import net.evetech.esi.models.GetCharactersCharacterIdWalletJournal200Ok;
import net.evetech.esi.models.GetCharactersCharacterIdWalletTransactions200Ok;
import net.evetech.esi.models.GetCorporationsCorporationIdOk;
import net.evetech.esi.models.GetInsurancePrices200Ok;
import net.evetech.esi.models.GetInsurancePricesLevel;
import net.evetech.esi.models.GetMarketsGroupsMarketGroupIdOk;
import net.evetech.esi.models.GetMarketsRegionIdHistory200Ok;
import net.evetech.esi.models.GetMarketsRegionIdOrders200Ok;
import net.evetech.esi.models.GetMarketsStructuresStructureId200Ok;
import net.evetech.esi.models.GetUniverseConstellationsConstellationIdOk;
import net.evetech.esi.models.GetUniverseRegionsRegionIdOk;
import net.evetech.esi.models.GetUniverseStationsStationIdOk;
import net.evetech.esi.models.GetUniverseStructuresStructureIdOk;
import net.evetech.esi.models.GetUniverseSystemsSystemIdOk;
import net.evetech.esi.models.GetUniverseTypesTypeIdOk;

public interface TypeUtil {

	public static Contract convert(GetCharactersCharacterIdContracts200Ok c) {
		Contract contract = new Contract();
		contract.setContractId(c.getContractId());
		contract.setIssuerId(c.getIssuerId());
		contract.setIssuerCorpId(c.getIssuerCorporationId());
		contract.setAssigneeId(c.getAssigneeId());
		contract.setAcceptorId(c.getAcceptorId());
		contract.setAvailability(c.getAvailability().toString());
		contract.setDateIssued(new Timestamp(c.getDateIssued().getMillis()));
		contract.setDateExpired(new Timestamp(c.getDateExpired().getMillis()));
		contract.setStatus(c.getStatus().toString());
		contract.setType(c.getType().toString());
		return contract;
	}

	public static InvType convert(GetUniverseTypesTypeIdOk t) {
		InvType type = new InvType();
		if (t.getTypeId() != null)
			type.setTypeId(t.getTypeId());
		if (t.getGroupId() != null)
			type.setGroupId(t.getGroupId());
		if (t.getName() != null)
			type.setTypeName(t.getName());
		if (t.getDescription() != null)
			type.setDescription(t.getDescription());
		if (t.getMass() != null)
			type.setMass(t.getMass());
		if (t.getVolume() != null)
			type.setVolume(t.getVolume());
		if (t.getPublished() != null)
			type.setPublished(t.getPublished());
		if (t.getMarketGroupId() != null)
			type.setMarketGroupId(t.getMarketGroupId());
		return type;
	}

	public static MarketGroup convert(GetMarketsGroupsMarketGroupIdOk g) {
		MarketGroup group = new MarketGroup();
		group.setMarketGroupId(g.getMarketGroupId());
		if (g.getParentGroupId() != null)
			group.setParentGroupId(g.getParentGroupId());
		group.setMarketGroupName(g.getName());
		group.setDescription(g.getDescription());
		group.setHasTypes(!g.getTypes().isEmpty());
		return group;
	}

	public static Character convert(GetCharactersCharacterIdOk c) {
		Character character = new Character();
		character.setCharName(c.getName());
		character.setCorpId(c.getCorporationId());
		if (c.getAllianceId() != null)
			character.setAllianceId(c.getAllianceId());
		character.setBirthday(new Date(c.getBirthday().getMillis()));
		return character;
	}

	public static Corporation convert(GetCorporationsCorporationIdOk c) {
		Corporation corp = new Corporation();
		corp.setCorpName(c.getName());
		corp.setTicker(c.getTicker());
		if (c.getAllianceId() != null)
			corp.setAllianceId(c.getAllianceId());
		corp.setCeoId(c.getCeoId());
		corp.setCreatorId(c.getCreatorId());
		if (c.getDateFounded() != null)
			corp.setCreationDate(new Date(c.getDateFounded().getMillis()));
		corp.setMemberCount(c.getMemberCount());
		corp.setTaxRate(c.getTaxRate());
		corp.setUrl(c.getUrl());
		return corp;
	}

	public static Structure convert(GetUniverseStructuresStructureIdOk s) {
		Structure struct = new Structure();
		struct.setStructName(s.getName());
		struct.setCorpId(s.getOwnerId());
		struct.setSystemId(s.getSolarSystemId());
		struct.setTypeId(s.getTypeId());
		return struct;
	}

	public static MarketOrder convert(GetMarketsRegionIdOrders200Ok o) {
		MarketOrder order = new MarketOrder();
		order.setIssued(new Timestamp(o.getIssued().getMillis()));
		order.setRange(o.getRange().toString());
		order.setBuyOrder(o.getIsBuyOrder());
		order.setDuration(o.getDuration());
		order.setOrderId(o.getOrderId());
		order.setVolumeRemain(o.getVolumeRemain());
		order.setMinVolume(o.getMinVolume());
		order.setTypeId(o.getTypeId());
		order.setVolumeTotal(o.getVolumeTotal());
		order.setLocationId(o.getLocationId());
		order.setPrice(o.getPrice());
		order.setSource(Source.PUBLIC);
		return order;
	}

	public static MarketOrder convert(GetMarketsStructuresStructureId200Ok o) {
		MarketOrder order = new MarketOrder();
		order.setIssued(new Timestamp(o.getIssued().getMillis()));
		order.setRange(o.getRange().toString());
		order.setBuyOrder(o.getIsBuyOrder());
		order.setDuration(o.getDuration());
		order.setOrderId(o.getOrderId());
		order.setVolumeRemain(o.getVolumeRemain());
		order.setMinVolume(o.getMinVolume());
		order.setTypeId(o.getTypeId());
		order.setVolumeTotal(o.getVolumeTotal());
		order.setLocationId(o.getLocationId());
		order.setPrice(o.getPrice());
		order.setSource(Source.STRUCTURE);
		return order;
	}

	public static MarketOrder convert(GetCharactersCharacterIdOrders200Ok o) {
		MarketOrder order = new MarketOrder();
		if (o.getIssued() != null)
			order.setIssued(new Timestamp(o.getIssued().getMillis()));
		if (o.getRange() != null)
			order.setRange(o.getRange().toString());
		if (o.getIsBuyOrder() != null)
			order.setBuyOrder(o.getIsBuyOrder());
		if (o.getDuration() != null)
			order.setDuration(o.getDuration());
		if (o.getOrderId() != null)
			order.setOrderId(o.getOrderId());
		if (o.getVolumeRemain() != null)
			order.setVolumeRemain(o.getVolumeRemain());
		if (o.getMinVolume() != null)
			order.setMinVolume(o.getMinVolume());
		if (o.getTypeId() != null)
			order.setTypeId(o.getTypeId());
		if (o.getVolumeTotal() != null)
			order.setVolumeTotal(o.getVolumeTotal());
		if (o.getLocationId() != null)
			order.setLocationId(o.getLocationId());
		if (o.getPrice() != null)
			order.setPrice(o.getPrice());
		if (o.getRegionId() != null)
			order.setRegion(o.getRegionId());
		order.setSource(Source.CHARACTER);
		return order;
	}

	public static MarketHistoryEntry convert(GetMarketsRegionIdHistory200Ok h) {
		MarketHistoryEntry entry = new MarketHistoryEntry();
		entry.setDate(Date.valueOf(h.getDate().toString()));
		entry.setHighest(h.getHighest());
		entry.setAverage(h.getAverage());
		entry.setLowest(h.getLowest());
		entry.setOrderCount(h.getOrderCount());
		entry.setVolume(h.getVolume());
		return entry;
	}

	public static WalletTransaction convert(GetCharactersCharacterIdWalletTransactions200Ok t) {
		WalletTransaction transaction = new WalletTransaction();
		transaction.setTransactionId(t.getTransactionId());
		transaction.setClientId(t.getClientId());
		transaction.setDate(new Timestamp(t.getDate().getMillis()));
		transaction.setBuy(t.getIsBuy());
		transaction.setPersonal(t.getIsPersonal());
		transaction.setTypeId(t.getTypeId());
		transaction.setQuantity(t.getQuantity());
		transaction.setUnitPrice(t.getUnitPrice());
		transaction.setLocationId(t.getLocationId());
		transaction.setJournalRefId(t.getJournalRefId());
		return transaction;
	}

	public static WalletJournalEntry convert(GetCharactersCharacterIdWalletJournal200Ok j) {
		WalletJournalEntry entry = new WalletJournalEntry();
		entry.setJournalId(j.getId());
		entry.setDate(new Timestamp(j.getDate().getMillis()));
		entry.setAmount(j.getAmount());
		entry.setBalance(j.getBalance());
		if (j.getTax() != null)
			entry.setTax(j.getTax());
		if (j.getFirstPartyId() != null)
			entry.setFirstPartyId(j.getFirstPartyId());
		if (j.getSecondPartyId() != null)
			entry.setSecondPartyId(j.getSecondPartyId());
		if (j.getTaxReceiverId() != null)
			entry.setTaxReceiverId(j.getTaxReceiverId());
		if (j.getDescription() != null)
			entry.setDescription(j.getDescription());
		if (j.getReason() != null)
			entry.setReason(j.getReason());
		if (j.getRefType() != null)
			entry.setRefType(j.getRefType().toString());
		if (j.getContextId() != null)
			entry.setContextId(j.getContextId());
		if (j.getContextIdType() != null)
			entry.setContextIdType(j.getContextIdType().toString());
		return entry;
	}

	public static InsurancePrice convert(GetInsurancePrices200Ok p) {
		InsurancePrice price = new InsurancePrice();
		price.setTypeId(p.getTypeId());
		List<InsurancePrice.Level> levels = new ArrayList<InsurancePrice.Level>();
		for (GetInsurancePricesLevel l : p.getLevels()) {
			InsurancePrice.Level level = new InsurancePrice.Level();
			level.setName(l.getName());
			level.setCost(l.getCost());
			level.setPayout(l.getPayout());
			levels.add(level);
		}
		price.setLevels(levels);
		return price;
	}

	public static Region convert(GetUniverseRegionsRegionIdOk r) {
		Region region = new Region();
		region.setReigonId(r.getRegionId());
		region.setRegionName(r.getName());
		region.setDescription(r.getDescription());
		return region;
	}

	public static Constellation convert(GetUniverseConstellationsConstellationIdOk c) {
		Constellation constellation = new Constellation();
		constellation.setConstellationId(c.getConstellationId());
		constellation.setConstellationName(c.getName());
		constellation.setRegionId(c.getRegionId());
		return constellation;
	}

	public static SolarSystem convert(GetUniverseSystemsSystemIdOk s) {
		SolarSystem system = new SolarSystem();
		system.setSolarSystemId(s.getSystemId());
		system.setSolarSystemName(s.getName());
		system.setConstellationId(s.getConstellationId());
		system.setX(s.getPosition().getX());
		system.setY(s.getPosition().getY());
		system.setZ(s.getPosition().getZ());
		system.setSecurity(s.getSecurityStatus());
		return system;
	}

	public static Station convert(GetUniverseStationsStationIdOk s) {
		Station station = new Station();
		station.setStationId(s.getStationId());
		station.setStationName(s.getName());
		station.setSolarSystemId(s.getSystemId());
		return station;
	}

}
