package org.project.mechanic_shop.repositories;

import org.project.mechanic_shop.models.MechanicService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MechanicServiceRepository extends JpaRepository<MechanicService, Long> {

    Optional<MechanicService> findByName(String name);

    Optional<MechanicService> findByExternalId(UUID externalId);

    boolean existsByName(String name);
}