package org.project.mechanic_shop.application.ports;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MechanicServiceRepositoryPort {
	MechanicService save(MechanicService ms);
	Optional<MechanicService> findByName(String name);
	Optional<MechanicService> findByExternalId(UUID externalId);
	Page<MechanicService> search(String name, Pageable pageable);
}
