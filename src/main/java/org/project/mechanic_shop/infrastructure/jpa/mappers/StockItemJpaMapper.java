package org.project.mechanic_shop.infrastructure.jpa.mappers;

import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.project.mechanic_shop.infrastructure.jpa.entities.StockItemJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class StockItemJpaMapper {

	public StockItem toDomain(StockItemJpaEntity jpa) {
		if (jpa == null) return null;
		StockItem item = new StockItem();
		item.setExternalId(jpa.getExternalId());
		item.setCreatedAt(jpa.getCreatedAt());
		item.setCreatedFor(jpa.getCreatedFor());
		item.setLastUpdatedAt(jpa.getLastUpdatedAt());
		item.setLastUpdatedFor(jpa.getLastUpdatedFor());
		item.setId(jpa.getId());
		item.setCode(jpa.getCode());
		item.setName(jpa.getName());
		item.setType(jpa.getType());
		item.setDescription(jpa.getDescription());
		item.setQuantity(jpa.getQuantity());
		item.setPendingDemand(jpa.getPendingDemand());
		item.setCostPrice(jpa.getCostPrice());
		item.setSalePrice(jpa.getSalePrice());
		return item;
	}

	public StockItemJpaEntity toJpa(StockItem item) {
		if (item == null) return null;
		StockItemJpaEntity jpa = new StockItemJpaEntity();
		jpa.setExternalId(item.getExternalId());
		jpa.setCreatedAt(item.getCreatedAt());
		jpa.setCreatedFor(item.getCreatedFor());
		jpa.setLastUpdatedAt(item.getLastUpdatedAt());
		jpa.setLastUpdatedFor(item.getLastUpdatedFor());
		jpa.setId(item.getId());
		jpa.setCode(item.getCode());
		jpa.setName(item.getName());
		jpa.setType(item.getType());
		jpa.setDescription(item.getDescription());
		jpa.setQuantity(item.getQuantity());
		jpa.setPendingDemand(item.getPendingDemand());
		jpa.setCostPrice(item.getCostPrice());
		jpa.setSalePrice(item.getSalePrice());
		return jpa;
	}
}
