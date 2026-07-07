package org.project.mechanic_shop.application.ports;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleRepositoryPort {
	Vehicle save(Vehicle vehicle);
	Optional<Vehicle> findByLicensePlate(String licensePlate);
	Optional<Vehicle> findByExternalId(UUID externalId);
	Page<Vehicle> search(String licensePlate, String brand, String model, UUID ownerId, Pageable pageable);
}
