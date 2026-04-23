package org.project.mechanic_shop.services;

import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ServiceOrderService {

    ServiceOrder createServiceOrder(ServiceOrderCreateDto dto);

    ServiceOrder updateQuote(UUID externalId, ServiceOrderQuoteDto dto);

    ServiceOrder finishService(UUID externalId);

    ServiceOrder deliverVehicle(UUID externalId);

    ServiceOrder findByExternalId(UUID externalId);

    Page<ServiceOrder> search(String licensePlate, ServiceOrderStatusEnum status, Pageable pageable, User mechanic);

    ServiceOrder requestCustomerApproval(UUID externalId);

    ServiceOrder processBudgetResponse(UUID externalId, boolean approved);
}