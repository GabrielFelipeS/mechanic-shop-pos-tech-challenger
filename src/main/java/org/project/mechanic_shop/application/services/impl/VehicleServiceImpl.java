package org.project.mechanic_shop.application.services.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.application.ports.VehicleRepositoryPort;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.application.services.VehicleService;
import org.project.mechanic_shop.application.validators.VehicleValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

	private final VehicleRepositoryPort repository;
	private final UserRepositoryPort userRepository;
	private final VehicleValidator validator;

	@Override
	@Transactional
	public Vehicle create(Vehicle obj, UUID ownerId) {
		log.info("Creating new vehicle with license plate: {}", obj.getLicensePlate());

		var owner = userRepository
			.findByExternalId(ownerId)
			.orElseThrow(() -> new EntityNotFoundException("Owner not found for External ID: " + ownerId));

		obj.setOwner(owner);

		validator.validate(obj);

		return repository.save(obj);
	}

	@Override
	public Vehicle findByExternalId(UUID externalId) {
		return repository
			.findByExternalId(externalId)
			.orElseThrow(() -> {
				log.warn("Vehicle not found. Action: GET | Target External ID: {}", externalId);
				return new EntityNotFoundException("Vehicle not found for External ID: " + externalId);
			});
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Vehicle> search(String licensePlate, String brand, String model, UUID ownerId, Pageable pageable) {
		log.info(
			"Searching vehicles with filters - plate: {}, brand: {}, model: {}, ownerId: {}",
			licensePlate,
			brand,
			model,
			ownerId
		);

		return repository.search(licensePlate, brand, model, ownerId, pageable);
	}

	@Override
	@Transactional
	public Vehicle update(UUID id, Vehicle update, UUID ownerId) {
		var obj = repository
			.findByExternalId(id)
			.orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

		log.info("Updating vehicle with ID: {}", obj.getId());

		validator.validateUpdateEligibility(obj);

		if (ownerId != null) {
			var owner = userRepository
				.findByExternalId(ownerId)
				.orElseThrow(() -> new EntityNotFoundException("Owner not found for External ID: " + ownerId));
			obj.setOwner(owner);
		}

		obj.setLicensePlate(update.getLicensePlate());
		obj.setBrand(update.getBrand());
		obj.setModel(update.getModel());
		obj.setYear(update.getYear());
		obj.setColor(update.getColor());

		validator.validate(obj);

		return repository.save(obj);
	}
}
