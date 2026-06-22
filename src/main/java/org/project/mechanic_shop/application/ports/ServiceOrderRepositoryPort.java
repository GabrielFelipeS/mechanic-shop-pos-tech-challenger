package org.project.mechanic_shop.application.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceOrderRepositoryPort {
	ServiceOrder save(ServiceOrder order);
	Optional<ServiceOrder> findByExternalId(UUID externalId);
	Optional<ServiceOrder> findByApprovalToken(String approvalToken);
	Double findAverageCompletionDays();
	Long countCompletedOrders();
	Page<ServiceOrder> findActiveOrders(
		List<ServiceOrderStatusEnum> excludedStatuses,
		ServiceOrderStatusEnum inProgress,
		ServiceOrderStatusEnum pendingApproval,
		ServiceOrderStatusEnum diagnosis,
		ServiceOrderStatusEnum received,
		Pageable pageable
	);
	Page<ServiceOrder> search(String licensePlate, ServiceOrderStatusEnum status, User mechanic, Pageable pageable);
}
