package org.project.mechanic_shop.application.services;

import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderMetricsDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ServiceOrderService {
	ServiceOrder createServiceOrder(ServiceOrderCreateDto dto);

	ServiceOrder updateQuote(UUID externalId, ServiceOrderQuoteDto dto);

	ServiceOrder finishService(UUID externalId);

	ServiceOrder deliverVehicle(UUID externalId);

	ServiceOrder findByExternalId(UUID externalId);

	Page<ServiceOrder> search(String licensePlate, ServiceOrderStatusEnum status, Pageable pageable, User mechanic);

	Page<ServiceOrder> listActiveOrders(Pageable pageable);

	ServiceOrder requestCustomerApproval(UUID externalId);

	ServiceOrder processBudgetResponse(UUID externalId, boolean approved);

	ServiceOrder processBudgetResponseByToken(String token, boolean approved);

	ServiceOrderMetricsDto getMetrics();
}
