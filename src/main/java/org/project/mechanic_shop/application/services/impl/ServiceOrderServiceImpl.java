package org.project.mechanic_shop.application.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.application.services.*;
import org.project.mechanic_shop.domain.entities.budget.Budget;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;

import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.service_order_labor.ServiceOrderLabor;
import org.project.mechanic_shop.domain.entities.service_order_stock_item.ServiceOrderStockItem;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderLaborManDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderMetricsDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderStockItemManDto;
import org.project.mechanic_shop.domain.events.NewServiceOrderEvent;
import org.project.mechanic_shop.domain.events.ServiceOrderStatusChangedEvent;
import org.project.mechanic_shop.domain.enums.BudgetStatusEnum;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.infrastructure.repositories.ServiceOrderRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

	private static final String SERVICE_ORDER_NOT_FOUND_MSG = "Service Order not found";

	@Override
	@Transactional
	public ServiceOrder createServiceOrder(ServiceOrderCreateDto dto) {
		log.info("Creating new Service Order for vehicle: {}", dto.vehicleExternalId());

		ServiceOrder serviceOrder = new ServiceOrder();
		serviceOrder.setVehicle(vehicleService.findByExternalId(dto.vehicleExternalId()));
		serviceOrder.setCustomerComplaint(dto.customerComplaint());
		serviceOrder.setOdometerReading(dto.odometerReading());
		serviceOrder.setStatus(ServiceOrderStatusEnum.RECEIVED);
		serviceOrder.setBudget(new Budget());

		if (dto.mechanicExternalId() != null) {
			serviceOrder.setResponsibleMechanic(userService.findByExternalId(dto.mechanicExternalId()));
		}

		boolean hasItems = applyInitialItems(serviceOrder, dto);

		ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);

		if (savedOrder.getResponsibleMechanic() != null) {
			eventPublisher.publishEvent(new NewServiceOrderEvent(savedOrder));
		}

		if (hasItems) {
			eventPublisher.publishEvent(new ServiceOrderStatusChangedEvent(
				savedOrder.getExternalId(),
				ServiceOrderStatusEnum.RECEIVED,
				ServiceOrderStatusEnum.DIAGNOSIS
			));
		}

		log.info("Service Order created successfully. ID: {}", savedOrder.getId());
		return savedOrder;
	}

	private boolean applyInitialItems(ServiceOrder order, ServiceOrderCreateDto dto) {
		boolean hasLabors = dto.labors() != null && !dto.labors().isEmpty();
		boolean hasParts  = dto.parts()  != null && !dto.parts().isEmpty();

		if (!hasLabors && !hasParts) return false;

		BigDecimal total = BigDecimal.ZERO;

		if (hasLabors) total = total.add(addLabors(order, dto.labors()));
		if (hasParts)  total = total.add(addParts(order, dto.parts()));

		order.getBudget().setTotalAmount(total);
		order.setStatus(ServiceOrderStatusEnum.DIAGNOSIS);
		log.info("Initial quote provided at creation — status set to DIAGNOSIS.");
		return true;
	}

	private BigDecimal addLabors(ServiceOrder order, List<ServiceOrderLaborManDto> labors) {
		BigDecimal total = BigDecimal.ZERO;
		for (ServiceOrderLaborManDto laborDto : labors) {
			MechanicService mechanicService = mechanicServiceCatalog.findByExternalId(laborDto.mechanicServiceExternalId());
			BigDecimal unitPrice  = mechanicService.getPrice();
			BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(laborDto.quantity()));

			ServiceOrderLabor labor = new ServiceOrderLabor();
			labor.setMechanicService(mechanicService);
			labor.setQuantity(laborDto.quantity());
			labor.setUnitPrice(unitPrice);
			labor.setTotalPrice(totalPrice);

			order.addLabor(labor);
			total = total.add(totalPrice);
		}
		return total;
	}

	private BigDecimal addParts(ServiceOrder order, List<ServiceOrderStockItemManDto> parts) {
		BigDecimal total = BigDecimal.ZERO;
		for (ServiceOrderStockItemManDto partDto : parts) {
			StockItem stockItem = stockItemService.findByExternalId(partDto.partExternalId());
			BigDecimal unitPrice  = stockItem.getSalePrice();
			BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(partDto.quantity()));

			ServiceOrderStockItem orderPart = new ServiceOrderStockItem();
			orderPart.setStockItem(stockItem);
			orderPart.setStockItemType(stockItem.getType());
			orderPart.setQuantity(partDto.quantity());
			orderPart.setUnitPrice(unitPrice);
			orderPart.setTotalPrice(totalPrice);

			order.addStockItem(orderPart);
			total = total.add(totalPrice);
		}
		return total;
	}

	@Override
	@Transactional
	public ServiceOrder updateQuote(UUID externalId, ServiceOrderQuoteDto dto) {
		log.info("Updating quote for Service Order: {}", externalId);

		ServiceOrder order = serviceOrderRepository
			.findByExternalId(externalId)
			.orElseThrow(() -> new EntityNotFoundException(SERVICE_ORDER_NOT_FOUND_MSG));

		if (
			order.getStatus() == ServiceOrderStatusEnum.PENDING_APPROVAL ||
			order.getStatus() == ServiceOrderStatusEnum.COMPLETED ||
			order.getBudget().getStatus() == BudgetStatusEnum.REJECTED ||
			order.getBudget().getStatus() == BudgetStatusEnum.APPROVED ||
			order.getBudget().getStatus() == BudgetStatusEnum.SENT
		) {
			throw new IllegalStateException(
				"Cannot update items for an Order that is already " +
					order.getStatus() +
					" & " +
					order.getBudget().getStatus()
			);
		}

		order.setMechanicDiagnosis(dto.mechanicDiagnosis());
		BigDecimal totalAmount = BigDecimal.ZERO;

		order.getLabors().clear();
		order.getStockItems().clear();

		if (dto.labors() != null) {
			for (ServiceOrderLaborManDto laborDto : dto.labors()) {
				MechanicService mechanicService = mechanicServiceCatalog.findByExternalId(
					laborDto.mechanicServiceExternalId()
				);
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

		if (order.getStatus() == ServiceOrderStatusEnum.RECEIVED) {
			order.setStatus(ServiceOrderStatusEnum.DIAGNOSIS);
			log.info("Auto-updating status: RECEIVED -> DIAGNOSIS");
			eventPublisher.publishEvent(new ServiceOrderStatusChangedEvent(
					order.getExternalId(),
					ServiceOrderStatusEnum.RECEIVED,
					ServiceOrderStatusEnum.DIAGNOSIS));
		}

		order.getBudget().setTotalAmount(totalAmount);

		ServiceOrder updatedOrder = serviceOrderRepository.save(order);



		log.info("Quote updated successfully. New Total: R$ {}", totalAmount);
		return updatedOrder;
	}

	@Transactional
	public ServiceOrder requestCustomerApproval(UUID externalId) {
		ServiceOrder order = serviceOrderRepository
			.findByExternalId(externalId)
			.orElseThrow(() -> new EntityNotFoundException(SERVICE_ORDER_NOT_FOUND_MSG));

		if (order.getStatus() != ServiceOrderStatusEnum.DIAGNOSIS) {
			throw new IllegalStateException("Only orders IN DIAGNOSIS can be sent for approval.");
		}

		ServiceOrderStatusEnum oldStatus = order.getStatus();
		order.setStatus(ServiceOrderStatusEnum.PENDING_APPROVAL);

		order.getBudget().setStatus(BudgetStatusEnum.SENT);
		order.setApprovalToken(UUID.randomUUID().toString());

		setEstimatedDeadline(order);

		ServiceOrder updatedOrder = serviceOrderRepository.save(order);

		log.info("Diagnosis finished. Status auto-updated: DIAGNOSIS -> PENDING_APPROVAL");

		eventPublisher.publishEvent(
			new ServiceOrderStatusChangedEvent(
				updatedOrder.getExternalId(),
				oldStatus,
				ServiceOrderStatusEnum.PENDING_APPROVAL
			)
		);

		return updatedOrder;
	}

	@Override
	@Transactional
	public ServiceOrder processBudgetResponse(UUID externalId, boolean isApproved) {
		log.info("Processing budget response for OS: {}. Approved: {}", externalId, isApproved);

		ServiceOrder order = serviceOrderRepository
			.findByExternalId(externalId)
			.orElseThrow(() -> new EntityNotFoundException(SERVICE_ORDER_NOT_FOUND_MSG));

		if (order.getStatus() != ServiceOrderStatusEnum.PENDING_APPROVAL) {
			throw new IllegalStateException("Cannot process budget response. Order is currently: " + order.getStatus());
		}

		ServiceOrderStatusEnum oldStatus = order.getStatus();

		if (isApproved) {
			order.getBudget().setStatus(BudgetStatusEnum.APPROVED);

			log.info("Budget Approved! Triggering stock withdrawal for {} items", order.getStockItems().size());
			for (ServiceOrderStockItem item : order.getStockItems()) {
				stockItemService.withdrawStock(item.getStockItem().getExternalId(), item.getQuantity());
			}

			order.setApprovalDate(LocalDateTime.now());
			order.setStatus(ServiceOrderStatusEnum.IN_PROGRESS);
		} else {
			order.getBudget().setStatus(BudgetStatusEnum.REJECTED);
			order.setStatus(ServiceOrderStatusEnum.CANCELED);
			log.info("Budget Rejected. Service Order Canceled.");
		}

		ServiceOrder updatedOrder = serviceOrderRepository.save(order);
		eventPublisher.publishEvent(
			new ServiceOrderStatusChangedEvent(updatedOrder.getExternalId(), oldStatus, order.getStatus())
		);

		return updatedOrder;
	}

	@Transactional
	@Override
	public ServiceOrder finishService(UUID externalId) {
		log.info("Action triggered: Finishing service for OS {}", externalId);

		ServiceOrder order = serviceOrderRepository
			.findByExternalId(externalId)
			.orElseThrow(() -> new EntityNotFoundException(SERVICE_ORDER_NOT_FOUND_MSG));

		if (order.getStatus() != ServiceOrderStatusEnum.IN_PROGRESS) {
			throw new IllegalStateException("Only orders IN PROGRESS can be finished.");
		}

		ServiceOrderStatusEnum oldStatus = order.getStatus();

		order.setStatus(ServiceOrderStatusEnum.COMPLETED);

		recordActualFinish(order);

		ServiceOrder updatedOrder = serviceOrderRepository.save(order);

		eventPublisher.publishEvent(
			new ServiceOrderStatusChangedEvent(
				updatedOrder.getExternalId(),
				oldStatus,
				ServiceOrderStatusEnum.COMPLETED
			)
		);

		return updatedOrder;
	}

	@Transactional
	@Override
	public ServiceOrder deliverVehicle(UUID externalId) {
		log.info("Action triggered: Delivering vehicle for OS {}", externalId);

		ServiceOrder order = serviceOrderRepository
			.findByExternalId(externalId)
			.orElseThrow(() -> new EntityNotFoundException(SERVICE_ORDER_NOT_FOUND_MSG));

		if (order.getStatus() != ServiceOrderStatusEnum.COMPLETED) {
			throw new IllegalStateException("Only COMPLETED orders can be delivered to the customer.");
		}

		ServiceOrderStatusEnum oldStatus = order.getStatus();

		order.setStatus(ServiceOrderStatusEnum.DELIVERED);

		ServiceOrder updatedOrder = serviceOrderRepository.save(order);

		eventPublisher.publishEvent(
			new ServiceOrderStatusChangedEvent(
				updatedOrder.getExternalId(),
				oldStatus,
				ServiceOrderStatusEnum.DELIVERED
			)
		);

		return updatedOrder;
	}

	@Override
	@Transactional(readOnly = true)
	public ServiceOrder findByExternalId(UUID externalId) {
		return serviceOrderRepository
			.findByExternalId(externalId)
			.orElseThrow(() -> {
				log.warn("Service Order not found. Target External ID: {}", externalId);
				return new EntityNotFoundException("Service Order not found for External ID: " + externalId);
			});
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ServiceOrder> search(
		String licensePlate,
		ServiceOrderStatusEnum status,
		Pageable pageable,
		User mechanic
	) {
		log.info("Searching service orders with filters - licensePlate: {}, status: {}", licensePlate, status);

		ServiceOrder probe = new ServiceOrder();
		probe.setStatus(status);
		probe.setResponsibleMechanic(mechanic);

		if (licensePlate != null && !licensePlate.isBlank()) {
			Vehicle vehicleProbe = new Vehicle();
			vehicleProbe.setLicensePlate(licensePlate);
			probe.setVehicle(vehicleProbe);
		}

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths(
				"id",
				"externalId",
				"odometerReading",
				"totalAmount",
				"createdAt",
				"createdFor",
				"lastUpdatedAt",
				"lastUpdatedFor"
			)
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		return serviceOrderRepository.findAll(Example.of(probe, matcher), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ServiceOrder> listActiveOrders(Pageable pageable) {
		log.info("Listing active service orders with business ordering");

		List<ServiceOrderStatusEnum> excluded = List.of(
			ServiceOrderStatusEnum.COMPLETED,
			ServiceOrderStatusEnum.DELIVERED,
			ServiceOrderStatusEnum.CANCELED
		);

		Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

		return serviceOrderRepository.findActiveOrders(
			excluded,
			ServiceOrderStatusEnum.IN_PROGRESS,
			ServiceOrderStatusEnum.PENDING_APPROVAL,
			ServiceOrderStatusEnum.DIAGNOSIS,
			ServiceOrderStatusEnum.RECEIVED,
			unsorted
		);
	}

	@Override
	@Transactional
	public ServiceOrder processBudgetResponseByToken(String token, boolean approved) {
		log.info("Processing budget response via email token. Approved: {}", approved);

		ServiceOrder order = serviceOrderRepository
			.findByApprovalToken(token)
			.orElseThrow(() -> new EntityNotFoundException("Token de aprovação inválido ou já utilizado."));

		if (order.getStatus() != ServiceOrderStatusEnum.PENDING_APPROVAL) {
			throw new IllegalStateException("Esta OS não está mais aguardando aprovação: " + order.getStatus());
		}

		order.setApprovalToken(null);

		ServiceOrderStatusEnum oldStatus = order.getStatus();

		if (approved) {
			order.getBudget().setStatus(BudgetStatusEnum.APPROVED);
			log.info("Budget approved via email! Withdrawing stock for {} items.", order.getStockItems().size());
			for (ServiceOrderStockItem item : order.getStockItems()) {
				stockItemService.withdrawStock(item.getStockItem().getExternalId(), item.getQuantity());
			}
			order.setApprovalDate(LocalDateTime.now());
			order.setStatus(ServiceOrderStatusEnum.IN_PROGRESS);
		} else {
			order.getBudget().setStatus(BudgetStatusEnum.REJECTED);
			order.setStatus(ServiceOrderStatusEnum.CANCELED);
			log.info("Budget rejected via email. Service Order canceled.");
		}

		ServiceOrder updatedOrder = serviceOrderRepository.save(order);
		eventPublisher.publishEvent(
			new ServiceOrderStatusChangedEvent(updatedOrder.getExternalId(), oldStatus, order.getStatus())
		);

		return updatedOrder;
	}

	@Override
	@Transactional(readOnly = true)
	public ServiceOrderMetricsDto getMetrics() {
		return new ServiceOrderMetricsDto(
			serviceOrderRepository.findAverageCompletionDays(),
			serviceOrderRepository.countCompletedOrders()
		);
	}

	public void setEstimatedDeadline(ServiceOrder order) {
		long totalServiceMinutes = order
			.getLabors()
			.stream()
			.mapToLong(l -> (long) l.getMechanicService().getEstimatedTimeMinutes() * l.getQuantity())
			.sum();

		boolean isPartMissing = order
				.getStockItems()
				.stream()
				.anyMatch(item -> item.getQuantity() > item.getStockItem().getQuantity());

		int baseDays = (int) Math.ceil(totalServiceMinutes / 480.0);
		int totalEstimatedDays = baseDays + (isPartMissing ? 3 : 0);

		order.setEstimatedCompletionDays(totalEstimatedDays);
		order.setEstimatedCompletionDate(LocalDateTime.now().plusDays(totalEstimatedDays));
		order.setApprovalDate(LocalDateTime.now());
	}

	public void recordActualFinish(ServiceOrder order) {
		LocalDateTime now = LocalDateTime.now();
		order.setActualCompletionDate(now);

		long daysTaken = ChronoUnit.DAYS.between(order.getApprovalDate(), now);

		order.setActualCompletionDays((int) daysTaken);
	}
}
