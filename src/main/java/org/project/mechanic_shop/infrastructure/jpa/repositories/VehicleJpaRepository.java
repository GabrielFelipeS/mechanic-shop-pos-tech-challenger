package org.project.mechanic_shop.infrastructure.jpa.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.infrastructure.jpa.entities.VehicleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, Long> {
	Optional<VehicleJpaEntity> findByLicensePlate(String licensePlate);
	Optional<VehicleJpaEntity> findByExternalId(UUID externalId);
	boolean existsByLicensePlate(String licensePlate);
}
