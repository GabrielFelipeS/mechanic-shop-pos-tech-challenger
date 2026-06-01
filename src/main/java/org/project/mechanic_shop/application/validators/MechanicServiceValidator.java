package org.project.mechanic_shop.application.validators;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;
import org.project.mechanic_shop.infrastructure.repositories.MechanicServiceRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MechanicServiceValidator {

	private final MechanicServiceRepository repository;

	public void validate(MechanicService service) {
		if (existsByName(service)) {
			throw new IllegalArgumentException("A Service with this name already exists: " + service.getName());
		}

		if (service.getPrice().signum() < 0) {
			throw new IllegalArgumentException("Service price cannot be negative.");
		}

		if (service.getEstimatedTimeMinutes() < 0) {
			throw new IllegalArgumentException("Estimated time cannot be negative.");
		}
	}

	public void validateUpdateEligibility(MechanicService obj) {
		if (obj.getId() == null) {
			throw new IllegalArgumentException("You cannot update an object without an ID");
		}
	}

	private boolean existsByName(MechanicService service) {
		Optional<MechanicService> result = repository.findByName(service.getName());

		if (service.getId() == null) {
			return result.isPresent();
		}

		return result.isPresent() && !service.getId().equals(result.get().getId());
	}
}
