package org.project.mechanic_shop.application.validators;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.infrastructure.repositories.VehicleRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleValidator {

	private final VehicleRepository repository;

	public void validate(Vehicle vehicle) {
		if (existsVehicleByLicensePlate(vehicle)) {
			throw new IllegalArgumentException(
				"A Vehicle with this license plate already exists: " + vehicle.getLicensePlate()
			);
		}
	}

	public void validateUpdateEligibility(Vehicle obj) {
		if (obj.getId() == null) {
			throw new IllegalArgumentException("You cannot update an object without an ID");
		}
	}

	private boolean existsVehicleByLicensePlate(Vehicle vehicle) {
		Optional<Vehicle> result = repository.findByLicensePlate(vehicle.getLicensePlate());

		if (vehicle.getId() == null) {
			return result.isPresent();
		}

		return result.isPresent() && !vehicle.getId().equals(result.get().getId());
	}
}
