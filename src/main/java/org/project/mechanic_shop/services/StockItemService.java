package org.project.mechanic_shop.services;

import java.util.UUID;
import org.project.mechanic_shop.models.StockItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockItemService {
	StockItem create(StockItem obj);

	StockItem findByExternalId(UUID externalId);

	Page<StockItem> search(String code, String name, Pageable pageable);

	StockItem update(UUID id, StockItem update);

	void withdrawStock(UUID externalId, Integer requestedQuantity);
}
