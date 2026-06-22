package org.project.mechanic_shop.infrastructure.jpa.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.infrastructure.jpa.entities.MechanicServiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MechanicServiceJpaRepository extends JpaRepository<MechanicServiceJpaEntity, Long> {
	Optional<MechanicServiceJpaEntity> findByName(String name);
	Optional<MechanicServiceJpaEntity> findByExternalId(UUID externalId);
	boolean existsByName(String name);
}
