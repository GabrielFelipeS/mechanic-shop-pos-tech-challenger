package org.project.mechanic_shop.application.services.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;
import org.project.mechanic_shop.infrastructure.repositories.MechanicServiceRepository;
import org.project.mechanic_shop.application.services.MechanicServiceService;
import org.project.mechanic_shop.application.validators.MechanicServiceValidator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MechanicServiceServiceImpl implements MechanicServiceService {

	private final MechanicServiceRepository repository;
	private final MechanicServiceValidator validator;

	@Override
	@Transactional
	public MechanicService create(MechanicService obj) {
		log.info("Creating new mechanic service: {}", obj.getName());
		validator.validate(obj);
		return repository.save(obj);
	}

	@Override
	@Transactional(readOnly = true)
	public MechanicService findByExternalId(UUID externalId) {
		return repository
			.findByExternalId(externalId)
			.orElseThrow(() -> {
				log.warn("Service not found. Target External ID: {}", externalId);
				return new EntityNotFoundException("Service not found for External ID: " + externalId);
			});
	}

	@Override
	@Transactional(readOnly = true)
	public Page<MechanicService> search(String name, Pageable pageable) {
		log.info("Searching mechanic services with filters - name: {}", name);

		var service = new MechanicService();
		service.setName(name);

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths(
				"id",
				"externalId",
				"estimatedTimeMinutes",
				"price",
				"createdAt",
				"createdFor",
				"lastUpdatedAt",
				"lastUpdatedFor"
			)
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		return repository.findAll(Example.of(service, matcher), pageable);
	}

	@Override
	@Transactional
	public MechanicService update(UUID id, MechanicService update) {
		var obj = repository
			.findByExternalId(id)
			.orElseThrow(() -> new EntityNotFoundException("Service not found with id: " + id));

		log.info("Updating mechanic service with ID: {}", obj.getId());

		validator.validateUpdateEligibility(obj);

		obj.setName(update.getName());
		obj.setDescription(update.getDescription());
		obj.setEstimatedTimeMinutes(update.getEstimatedTimeMinutes());
		obj.setPrice(update.getPrice());

		validator.validate(obj);

		return repository.save(obj);
	}
}
