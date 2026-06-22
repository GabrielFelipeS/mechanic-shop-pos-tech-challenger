package org.project.mechanic_shop.infrastructure.jpa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.infrastructure.jpa.entities.ServiceOrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderJpaEntity, Long> {
	Optional<ServiceOrderJpaEntity> findByExternalId(UUID externalId);
	Optional<ServiceOrderJpaEntity> findByApprovalToken(String approvalToken);
@Query("SELECT AVG(so.actualCompletionDays) FROM ServiceOrderJpaEntity so WHERE so.actualCompletionDays IS NOT NULL")
	Double findAverageCompletionDays();

	@Query("SELECT COUNT(so) FROM ServiceOrderJpaEntity so WHERE so.actualCompletionDays IS NOT NULL")
	Long countCompletedOrders();

	@Query(value = "SELECT so FROM ServiceOrderJpaEntity so " +
				   "WHERE so.status NOT IN :excludedStatuses " +
				   "ORDER BY " +
				   "CASE WHEN so.status = :inProgress THEN 1 " +
				   "WHEN so.status = :pendingApproval THEN 2 " +
				   "WHEN so.status = :diagnosis THEN 3 " +
				   "WHEN so.status = :received THEN 4 ELSE 5 END ASC, " +
				   "so.createdAt ASC",
			countQuery = "SELECT COUNT(so) FROM ServiceOrderJpaEntity so WHERE so.status NOT IN :excludedStatuses")
	Page<ServiceOrderJpaEntity> findActiveOrders(
		@Param("excludedStatuses") List<ServiceOrderStatusEnum> excludedStatuses,
		@Param("inProgress") ServiceOrderStatusEnum inProgress,
		@Param("pendingApproval") ServiceOrderStatusEnum pendingApproval,
		@Param("diagnosis") ServiceOrderStatusEnum diagnosis,
		@Param("received") ServiceOrderStatusEnum received,
		Pageable pageable
	);
}
