package org.project.mechanic_shop.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderLaborManDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderStockItemManDto;
import org.project.mechanic_shop.events.NewServiceOrderEvent;
import org.project.mechanic_shop.events.ServiceOrderStatusChangedEvent;
import org.project.mechanic_shop.models.*;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.repositories.ServiceOrderRepository;
import org.project.mechanic_shop.services.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceOrderServiceImpl implements ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final StockItemService stockItemService;
    private final MechanicServiceService mechanicServiceCatalog;

    @Override
    @Transactional
    public ServiceOrder createServiceOrder(ServiceOrderCreateDto dto) {
        log.info("🛠️ Creating new Service Order for vehicle: {}", dto.vehicleExternalId());

        ServiceOrder serviceOrder = new ServiceOrder();
        Vehicle vehicle = vehicleService.findByExternalId(dto.vehicleExternalId());

        serviceOrder.setVehicle(vehicle);
        serviceOrder.setCustomerComplaint(dto.customerComplaint());
        serviceOrder.setOdometerReading(dto.odometerReading());
        serviceOrder.setStatus(ServiceOrderStatusEnum.RECEIVED);
        serviceOrder.setTotalAmount(BigDecimal.ZERO);

        if (dto.mechanicExternalId() != null) {
            User mechanic = userService.findByExternalId(dto.mechanicExternalId());
            serviceOrder.setResponsibleMechanic(mechanic);
        }

        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);

        if (savedOrder.getResponsibleMechanic() != null) {
            eventPublisher.publishEvent(new NewServiceOrderEvent(savedOrder));
        }

        log.info("Service Order created successfully. ID: {}", savedOrder.getId());
        return savedOrder;
    }

    @Override
    @Transactional
    public ServiceOrder updateQuote(UUID externalId, ServiceOrderQuoteDto dto) {
        log.info("Updating quote for Service Order: {}", externalId);

        ServiceOrder order = serviceOrderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Service Order not found"));

        if (order.getStatus() == ServiceOrderStatusEnum.APPROVED ||
                order.getStatus() == ServiceOrderStatusEnum.COMPLETED ||
                order.getStatus() == ServiceOrderStatusEnum.REJECTED) {
            throw new IllegalStateException("Cannot update items for an Order that is already " + order.getStatus());
        }

        order.setMechanicDiagnosis(dto.mechanicDiagnosis());
        BigDecimal totalAmount = BigDecimal.ZERO;

        order.getLabors().clear();
        order.getStockItems().clear();

        if (dto.labors() != null) {
            for (ServiceOrderLaborManDto laborDto : dto.labors()) {
                MechanicService mechanicService = mechanicServiceCatalog.findByExternalId(laborDto.mechanicServiceExternalId());
                BigDecimal unitPrice = mechanicService.getPrice();
                BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(laborDto.quantity()));

                ServiceOrderLabor labor = new ServiceOrderLabor();
                labor.setMechanicService(mechanicService);
                labor.setQuantity(laborDto.quantity());
                labor.setUnitPrice(unitPrice);
                labor.setTotalPrice(totalPrice);

                order.addLabor(labor);
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        if (dto.parts() != null) {
            for (ServiceOrderStockItemManDto partDto : dto.parts()) {
                StockItem stockItem = stockItemService.findByExternalId(partDto.partExternalId());
                BigDecimal unitPrice = stockItem.getSalePrice();
                BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(partDto.quantity()));

                ServiceOrderStockItem orderPart = new ServiceOrderStockItem();
                orderPart.setStockItem(stockItem);
                orderPart.setStockItemType(stockItem.getType());
                orderPart.setQuantity(partDto.quantity());
                orderPart.setUnitPrice(unitPrice);
                orderPart.setTotalPrice(totalPrice);

                order.addStockItem(orderPart);
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        if(order.getStatus() == ServiceOrderStatusEnum.RECEIVED){
            order.setStatus(ServiceOrderStatusEnum.DIAGNOSIS);
            log.info("Auto-updating status: RECEIVED -> DIAGNOSIS");
        }

        order.setTotalAmount(totalAmount);
        ServiceOrder updatedOrder = serviceOrderRepository.save(order);

        log.info("Quote updated successfully. New Total: R$ {}", totalAmount);
        return updatedOrder;
    }

    @Override
    @Transactional
    public ServiceOrder updateStatus(UUID externalId, ServiceOrderStatusEnum newStatus) {
        ServiceOrder order = serviceOrderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("Service Order not found"));

        ServiceOrderStatusEnum oldStatus = order.getStatus();

        log.info("Updating Service Order {} status: {} -> {}", externalId, oldStatus, newStatus);

        if (newStatus == ServiceOrderStatusEnum.APPROVED && oldStatus != ServiceOrderStatusEnum.APPROVED) {
            log.info("OS Approved! Triggering stock withdrawal for {} items", order.getStockItems().size());
            for (ServiceOrderStockItem item : order.getStockItems()) {
                stockItemService.withdrawStock(item.getStockItem().getExternalId(), item.getQuantity());
            }
            order.setApprovalDate(LocalDateTime.now());
        }

        if (newStatus == ServiceOrderStatusEnum.COMPLETED) {
            order.setCompletionDate(LocalDateTime.now());
        }

        order.setStatus(newStatus);
        ServiceOrder updatedOrder = serviceOrderRepository.save(order);

        eventPublisher.publishEvent(new ServiceOrderStatusChangedEvent(updatedOrder, oldStatus, newStatus));

        return updatedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOrder findByExternalId(UUID externalId) {
        return serviceOrderRepository.findByExternalId(externalId).orElseThrow(() -> {
            log.warn("Service Order not found. Target External ID: {}", externalId);
            return new EntityNotFoundException("Service Order not found for External ID: " + externalId);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceOrder> search(String licensePlate, ServiceOrderStatusEnum status, Pageable pageable) {
        log.info("Searching service orders with filters - licensePlate: {}, status: {}", licensePlate, status);

        ServiceOrder probe = new ServiceOrder();
        probe.setStatus(status);

        if (licensePlate != null && !licensePlate.isBlank()) {
            Vehicle vehicleProbe = new Vehicle();
            vehicleProbe.setLicensePlate(licensePlate);
            probe.setVehicle(vehicleProbe);
        }

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "externalId", "odometerReading", "totalAmount", "createdAt", "createdFor", "lastUpdatedAt", "lastUpdatedFor")
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        return serviceOrderRepository.findAll(Example.of(probe, matcher), pageable);
    }
}