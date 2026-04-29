package org.project.mechanic_shop.services;

import java.util.UUID;
import org.project.mechanic_shop.models.MechanicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MechanicServiceService {
	MechanicService create(MechanicService obj);
	MechanicService findByExternalId(UUID externalId);
	Page<MechanicService> search(String name, Pageable pageable);
	MechanicService update(UUID id, MechanicService update);
}
