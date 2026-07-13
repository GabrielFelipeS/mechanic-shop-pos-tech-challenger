package org.project.mechanic_shop.infrastructure.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.ServiceOrderRepositoryPort;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.infrastructure.jpa.mappers.ServiceOrderJpaMapper;
import org.project.mechanic_shop.infrastructure.jpa.repositories.ServiceOrderJpaRepository;
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
		String licensePlateFilter = (licensePlate != null && !licensePlate.isBlank()) ? licensePlate : null;
		Long mechanicId = mechanic != null ? mechanic.getId() : null;
		Long ownerId = owner != null ? owner.getId() : null;

		return jpaRepository.search(licensePlateFilter, status, mechanicId, ownerId, pageable).map(mapper::toDomain);
	}
}
