package org.project.mechanic_shop.services;

import org.project.mechanic_shop.models.MechanicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface MechanicServiceService {
    MechanicService create(MechanicService obj);
    MechanicService findByExternalId(UUID externalId);
    Page<MechanicService> search(String name, Pageable pageable);
    MechanicService update(UUID id, MechanicService update);
}