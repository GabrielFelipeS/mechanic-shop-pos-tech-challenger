package org.project.mechanic_shop.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.models.MechanicService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MechanicServiceRepository extends JpaRepository<MechanicService, Long> {
	Optional<MechanicService> findByName(String name);

	Optional<MechanicService> findByExternalId(UUID externalId);

	boolean existsByName(String name);
}
