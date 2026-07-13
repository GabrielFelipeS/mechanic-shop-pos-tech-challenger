package org.project.mechanic_shop.infrastructure.adapters;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.VehicleRepositoryPort;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.infrastructure.jpa.entities.UserJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.mappers.VehicleJpaMapper;
import org.project.mechanic_shop.infrastructure.jpa.repositories.UserJpaRepository;
import org.project.mechanic_shop.infrastructure.jpa.repositories.VehicleJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VehicleRepositoryAdapter implements VehicleRepositoryPort {

	private final VehicleJpaRepository jpaRepository;
	private final UserJpaRepository userJpaRepository;
	private final VehicleJpaMapper mapper;

	@Override
	public Vehicle save(Vehicle vehicle) {
		return mapper.toDomain(jpaRepository.save(mapper.toJpa(vehicle)));
	}

	@Override
	public Optional<Vehicle> findByLicensePlate(String licensePlate) {
		return jpaRepository.findByLicensePlate(licensePlate).map(mapper::toDomain);
	}

	@Override
	public Optional<Vehicle> findByExternalId(UUID externalId) {
		return jpaRepository.findByExternalId(externalId).map(mapper::toDomain);
	}

	@Override
	public Page<Vehicle> search(String licensePlate, String brand, String model, UUID ownerId, Pageable pageable) {
		Long ownerJpaId = null;

		if (ownerId != null) {
			Optional<UserJpaEntity> ownerOpt = userJpaRepository.findByExternalId(ownerId);
			if (ownerOpt.isEmpty()) return Page.empty(pageable);
			ownerJpaId = ownerOpt.get().getId();
		}

		String licensePlateFilter = (licensePlate != null && !licensePlate.isBlank()) ? licensePlate : null;
		String brandFilter = (brand != null && !brand.isBlank()) ? brand : null;
		String modelFilter = (model != null && !model.isBlank()) ? model : null;

		return jpaRepository
			.search(licensePlateFilter, brandFilter, modelFilter, ownerJpaId, pageable)
			.map(mapper::toDomain);
	}
}
