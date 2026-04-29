package org.project.mechanic_shop.services;

import java.util.UUID;
import org.project.mechanic_shop.models.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleService {
	Vehicle create(Vehicle obj, UUID ownerId);

	Vehicle findByExternalId(UUID externalId);

	Page<Vehicle> search(String licensePlate, String brand, String model, UUID ownerId, Pageable pageable);

	Vehicle update(UUID id, Vehicle update, UUID ownerId);
}
