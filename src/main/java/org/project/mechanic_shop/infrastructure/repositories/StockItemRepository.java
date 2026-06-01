package org.project.mechanic_shop.infrastructure.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
	Optional<StockItem> findByCode(String code);

	Optional<StockItem> findByExternalId(UUID externalId);

	boolean existsByCode(String code);
}
