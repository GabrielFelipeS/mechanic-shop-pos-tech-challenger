package org.project.mechanic_shop.services;

import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServiceOrderService {

    ServiceOrder createServiceOrder(ServiceOrderCreateDto dto);

    ServiceOrder updateQuote(UUID externalId, ServiceOrderQuoteDto dto);

    ServiceOrder updateStatus(UUID externalId, ServiceOrderStatusEnum newStatus);

    ServiceOrder findByExternalId(UUID externalId);

    Page<ServiceOrder> search(String licensePlate, ServiceOrderStatusEnum status, Pageable pageable);

}