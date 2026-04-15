package org.project.mechanic_shop.repositories;

import org.project.mechanic_shop.models.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PartRepository extends JpaRepository<Part, Long> {

    Optional<Part> findByCode(String code);

    Optional<Part> findByExternalId(UUID externalId);

    boolean existsByCode(String code);
}