package org.project.mechanic_shop.infrastructure.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
	Optional<Vehicle> findByLicensePlate(String licensePlate);

	Optional<Vehicle> findByExternalId(UUID externalId);

	boolean existsByLicensePlate(String licensePlate);
}
