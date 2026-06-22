package org.project.mechanic_shop.infrastructure.jpa.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.infrastructure.jpa.entities.StockItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemJpaRepository extends JpaRepository<StockItemJpaEntity, Long> {
	Optional<StockItemJpaEntity> findByCode(String code);
	Optional<StockItemJpaEntity> findByExternalId(UUID externalId);
	boolean existsByCode(String code);
}
