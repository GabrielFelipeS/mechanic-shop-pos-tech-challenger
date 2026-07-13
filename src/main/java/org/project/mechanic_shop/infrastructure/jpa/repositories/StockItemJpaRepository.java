package org.project.mechanic_shop.infrastructure.jpa.repositories;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.infrastructure.jpa.entities.StockItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockItemJpaRepository extends JpaRepository<StockItemJpaEntity, Long> {
	Optional<StockItemJpaEntity> findByCode(String code);
	Optional<StockItemJpaEntity> findByExternalId(UUID externalId);
	boolean existsByCode(String code);

	/**
	 * Locks the row for the duration of the caller's transaction so concurrent stock
	 * withdrawals for the same part are serialized instead of racing on a lost update.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from StockItemJpaEntity s where s.externalId = :externalId")
	Optional<StockItemJpaEntity> findWithLockByExternalId(@Param("externalId") UUID externalId);
}
