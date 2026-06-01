package org.project.mechanic_shop.infrastructure.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
	Optional<ServiceOrder> findByExternalId(UUID externalId);
}
