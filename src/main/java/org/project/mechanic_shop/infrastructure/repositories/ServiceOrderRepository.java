package org.project.mechanic_shop.infrastructure.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

	Optional<ServiceOrder> findByExternalId(UUID externalId);

	Optional<ServiceOrder> findByApprovalToken(String approvalToken);

	@Query(value = "SELECT so FROM ServiceOrder so " +
				   "WHERE so.status NOT IN :excludedStatuses " +
				   "ORDER BY " +
				   "CASE WHEN so.status = :inProgress THEN 1 " +
				   "WHEN so.status = :pendingApproval THEN 2 " +
				   "WHEN so.status = :diagnosis THEN 3 " +
				   "WHEN so.status = :received THEN 4 ELSE 5 END ASC, " +
				   "so.createdAt ASC",
			countQuery = "SELECT COUNT(so) FROM ServiceOrder so WHERE so.status NOT IN :excludedStatuses")
	Page<ServiceOrder> findActiveOrders(
		@Param("excludedStatuses") List<ServiceOrderStatusEnum> excludedStatuses,
		@Param("inProgress") ServiceOrderStatusEnum inProgress,
		@Param("pendingApproval") ServiceOrderStatusEnum pendingApproval,
		@Param("diagnosis") ServiceOrderStatusEnum diagnosis,
		@Param("received") ServiceOrderStatusEnum received,
		Pageable pageable
	);
}
