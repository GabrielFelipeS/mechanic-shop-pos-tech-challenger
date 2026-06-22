package org.project.mechanic_shop.infrastructure.adapters;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.VehicleRepositoryPort;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.infrastructure.jpa.entities.VehicleJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.mappers.VehicleJpaMapper;
import org.project.mechanic_shop.infrastructure.jpa.repositories.VehicleJpaRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VehicleRepositoryAdapter implements VehicleRepositoryPort {

	private final VehicleJpaRepository jpaRepository;
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
		VehicleJpaEntity probe = new VehicleJpaEntity();
		probe.setLicensePlate(licensePlate);
		probe.setBrand(brand);
		probe.setModel(model);

		if (ownerId != null) {
			Optional<VehicleJpaEntity> ownerProbeOpt = jpaRepository.findByExternalId(ownerId);
			if (ownerProbeOpt.isEmpty()) return Page.empty(pageable);
			VehicleJpaEntity ownerProbe = new VehicleJpaEntity();
			ownerProbe.setId(ownerProbeOpt.get().getOwner() != null ? ownerProbeOpt.get().getOwner().getId() : null);
		}

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths("id", "externalId", "year", "color", "createdAt", "createdFor", "lastUpdatedAt", "lastUpdatedFor")
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		return jpaRepository.findAll(Example.of(probe, matcher), pageable).map(mapper::toDomain);
	}
}
