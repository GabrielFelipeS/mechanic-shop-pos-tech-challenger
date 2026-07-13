package org.project.mechanic_shop.application.ports;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockItemRepositoryPort {
	StockItem save(StockItem item);
	Optional<StockItem> findByCode(String code);
	Optional<StockItem> findByExternalId(UUID externalId);
	Optional<StockItem> findWithLockByExternalId(UUID externalId);
	Page<StockItem> search(String code, String name, Pageable pageable);
}
