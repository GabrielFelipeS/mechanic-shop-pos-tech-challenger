package org.project.mechanic_shop.application.services.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.infrastructure.repositories.UserRepository;
import org.project.mechanic_shop.infrastructure.repositories.VehicleRepository;
import org.project.mechanic_shop.application.services.VehicleService;
import org.project.mechanic_shop.application.validators.VehicleValidator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

	private final VehicleRepository repository;
	private final UserRepository userRepository;
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

		var vehicle = new Vehicle();
		vehicle.setLicensePlate(licensePlate);
		vehicle.setBrand(brand);
		vehicle.setModel(model);

		if (ownerId != null) {
			var ownerOpt = userRepository.findByExternalId(ownerId);
			if (ownerOpt.isEmpty()) {
				return Page.empty(pageable);
			}
			vehicle.setOwner(ownerOpt.get());
		}

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths(
				"id",
				"externalId",
				"year",
				"color",
				"createdAt",
				"createdFor",
				"lastUpdatedAt",
				"lastUpdatedFor"
			)
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		Example<Vehicle> example = Example.of(vehicle, matcher);

		return repository.findAll(example, pageable);
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
