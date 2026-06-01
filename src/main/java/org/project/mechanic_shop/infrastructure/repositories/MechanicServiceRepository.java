package org.project.mechanic_shop.infrastructure.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MechanicServiceRepository extends JpaRepository<MechanicService, Long> {
	Optional<MechanicService> findByName(String name);

	Optional<MechanicService> findByExternalId(UUID externalId);

	boolean existsByName(String name);
}
