package org.project.mechanic_shop.repositories;

import org.project.mechanic_shop.models.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    Optional<StockItem> findByCode(String code);

    Optional<StockItem> findByExternalId(UUID externalId);

    boolean existsByCode(String code);
}