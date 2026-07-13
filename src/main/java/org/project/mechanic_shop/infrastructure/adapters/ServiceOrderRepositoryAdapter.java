package org.project.mechanic_shop.infrastructure.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.ServiceOrderRepositoryPort;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.infrastructure.jpa.entities.ServiceOrderJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.entities.UserJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.entities.VehicleJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.mappers.ServiceOrderJpaMapper;
import org.project.mechanic_shop.infrastructure.jpa.repositories.ServiceOrderJpaRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ServiceOrderRepositoryAdapter implements ServiceOrderRepositoryPort {

	private final ServiceOrderJpaRepository jpaRepository;
	private final ServiceOrderJpaMapper mapper;

	@Override
	public ServiceOrder save(ServiceOrder order) {
		return mapper.toDomain(jpaRepository.save(mapper.toJpa(order)));
	}

	@Override
	public Optional<ServiceOrder> findByExternalId(UUID externalId) {
		return jpaRepository.findByExternalId(externalId).map(mapper::toDomain);
	}

	@Override
	public Optional<ServiceOrder> findByApprovalToken(String token) {
		return jpaRepository.findByApprovalToken(token).map(mapper::toDomain);
	}

	@Override
	public Double findAverageCompletionDays() {
		return jpaRepository.findAverageCompletionDays();
	}

	@Override
	public Long countCompletedOrders() {
		return jpaRepository.countCompletedOrders();
	}

	@Override
	public Page<ServiceOrder> findActiveOrders(
		List<ServiceOrderStatusEnum> excludedStatuses,
		ServiceOrderStatusEnum inProgress,
		ServiceOrderStatusEnum pendingApproval,
		ServiceOrderStatusEnum diagnosis,
		ServiceOrderStatusEnum received,
		Pageable pageable
	) {
		Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
		return jpaRepository.findActiveOrders(excludedStatuses, inProgress, pendingApproval, diagnosis, received, unsorted)
			.map(mapper::toDomain);
	}

	@Override
	public Page<ServiceOrder> search(
		String licensePlate,
		ServiceOrderStatusEnum status,
		User mechanic,
		User owner,
		Pageable pageable
	) {
		ServiceOrderJpaEntity probe = new ServiceOrderJpaEntity();
		probe.setStatus(status);

		if (mechanic != null) {
			UserJpaEntity mechanicJpa = new UserJpaEntity();
			mechanicJpa.setId(mechanic.getId());
			probe.setResponsibleMechanic(mechanicJpa);
		}

		VehicleJpaEntity vehicleProbe = null;

		if (licensePlate != null && !licensePlate.isBlank()) {
			vehicleProbe = new VehicleJpaEntity();
			vehicleProbe.setLicensePlate(licensePlate);
		}

		if (owner != null) {
			if (vehicleProbe == null) vehicleProbe = new VehicleJpaEntity();
			UserJpaEntity ownerJpa = new UserJpaEntity();
			ownerJpa.setId(owner.getId());
			vehicleProbe.setOwner(ownerJpa);
		}

		if (vehicleProbe != null) probe.setVehicle(vehicleProbe);

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths("id", "externalId", "odometerReading", "totalAmount", "createdAt", "createdFor", "lastUpdatedAt", "lastUpdatedFor")
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		return jpaRepository.findAll(Example.of(probe, matcher), pageable).map(mapper::toDomain);
	}
}
