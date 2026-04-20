package org.project.mechanic_shop.repositories;

import org.project.mechanic_shop.models.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    Optional<ServiceOrder> findByExternalId(UUID externalId);
}