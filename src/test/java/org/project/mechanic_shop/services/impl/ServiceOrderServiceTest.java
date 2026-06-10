package org.project.mechanic_shop.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.application.services.impl.ServiceOrderServiceImpl;
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
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderStockItemManDto;
import org.project.mechanic_shop.domain.events.NewServiceOrderEvent;
import org.project.mechanic_shop.domain.events.ServiceOrderStatusChangedEvent;
import org.project.mechanic_shop.domain.enums.BudgetStatusEnum;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.domain.enums.StockItemTypeEnum;
import org.project.mechanic_shop.infrastructure.repositories.ServiceOrderRepository;
import org.project.mechanic_shop.application.services.MechanicServiceService;
import org.project.mechanic_shop.application.services.ServiceOrderService;
import org.project.mechanic_shop.application.services.StockItemService;
import org.project.mechanic_shop.application.services.UserService;
import org.project.mechanic_shop.application.services.VehicleService;
import org.project.mechanic_shop.utils.MechanicServiceHelper;
import org.project.mechanic_shop.utils.UserHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

	private ServiceOrderService service;

	@Mock
	private ServiceOrderRepository repository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private VehicleService vehicleService;

	@Mock
	private UserService userService;

	@Mock
	private StockItemService stockItemService;

	@Mock
	private MechanicServiceService mechanicServiceCatalog;

	@BeforeEach
	void setup() {
		service = new ServiceOrderServiceImpl(
			repository,
			eventPublisher,
			vehicleService,
			userService,
			stockItemService,
			mechanicServiceCatalog
		);
	}

	@Nested
	class CreateServiceOrder {

		@Test
		void shouldCreateServiceOrderWithoutMechanic() {
			UUID vehicleId = UUID.randomUUID();
			var vehicle = vehicle();
			var dto = new ServiceOrderCreateDto(vehicleId, "Barulho no freio", 125000, null, null, null);

			when(vehicleService.findByExternalId(vehicleId)).thenReturn(vehicle);
			when(repository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

			var created = service.createServiceOrder(dto);

			assertThat(created.getVehicle()).isSameAs(vehicle);
			assertThat(created.getCustomerComplaint()).isEqualTo("Barulho no freio");
			assertThat(created.getOdometerReading()).isEqualTo(125000);
			assertThat(created.getStatus()).isEqualTo(ServiceOrderStatusEnum.RECEIVED);
			assertThat(created.getBudget()).isNotNull();
			assertThat(created.getBudget().getTotalAmount()).isEqualByComparingTo("0");
			assertThat(created.getBudget().getStatus()).isEqualTo(BudgetStatusEnum.OPEN);
			assertThat(created.getResponsibleMechanic()).isNull();

			verify(eventPublisher, never()).publishEvent(any(NewServiceOrderEvent.class));
		}

		@Test
		void shouldCreateServiceOrderAndPublishEventWhenMechanicIsAssigned() {
			UUID vehicleId = UUID.randomUUID();
			UUID mechanicId = UUID.randomUUID();
			var vehicle = vehicle();
			var mechanic = mechanicUser();
			var dto = new ServiceOrderCreateDto(vehicleId, "Trocar embreagem", 90000, mechanicId, null, null);
			ArgumentCaptor<NewServiceOrderEvent> eventCaptor = ArgumentCaptor.forClass(NewServiceOrderEvent.class);

			when(vehicleService.findByExternalId(vehicleId)).thenReturn(vehicle);
			when(userService.findByExternalId(mechanicId)).thenReturn(mechanic);
			when(repository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

			var created = service.createServiceOrder(dto);

			assertThat(created.getResponsibleMechanic()).isSameAs(mechanic);
			verify(eventPublisher).publishEvent(eventCaptor.capture());
			assertThat(eventCaptor.getValue().serviceOrder()).isSameAs(created);
		}
	}

	@Nested
	class UpdateQuote {

		@Test
		void shouldUpdateQuoteWithLaborsAndParts() {
			UUID externalId = UUID.randomUUID();
			UUID laborId = UUID.randomUUID();
			UUID partId = UUID.randomUUID();
			var order = serviceOrder();
			order.getLabors().add(new ServiceOrderLabor());
			order.getStockItems().add(new ServiceOrderStockItem());

			var mechanicService = MechanicServiceHelper.generateMechanicService();
			mechanicService.setPrice(new BigDecimal("150.00"));

			var part = stockItem();
			part.setSalePrice(new BigDecimal("40.00"));

			var dto = new ServiceOrderQuoteDto(
				"Troca de componentes",
				List.of(new ServiceOrderStockItemManDto(partId, 2)),
				List.of(new ServiceOrderLaborManDto(laborId, 3))
			);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));
			when(mechanicServiceCatalog.findByExternalId(laborId)).thenReturn(mechanicService);
			when(stockItemService.findByExternalId(partId)).thenReturn(part);
			when(repository.save(order)).thenReturn(order);

			var updated = service.updateQuote(externalId, dto);

			assertThat(updated.getMechanicDiagnosis()).isEqualTo("Troca de componentes");
			assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatusEnum.DIAGNOSIS);
			assertThat(updated.getLabors()).hasSize(1);
			assertThat(updated.getStockItems()).hasSize(1);
			assertThat(updated.getBudget().getTotalAmount()).isEqualByComparingTo("530.00");
			assertThat(updated.getBudget().getStatus()).isEqualTo(BudgetStatusEnum.OPEN);

			ServiceOrderLabor labor = updated.getLabors().getFirst();
			assertThat(labor.getMechanicService()).isSameAs(mechanicService);
			assertThat(labor.getQuantity()).isEqualTo(3);
			assertThat(labor.getUnitPrice()).isEqualByComparingTo("150.00");
			assertThat(labor.getTotalPrice()).isEqualByComparingTo("450.00");
			assertThat(labor.getServiceOrder()).isSameAs(updated);

			ServiceOrderStockItem orderPart = updated.getStockItems().getFirst();
			assertThat(orderPart.getStockItem()).isSameAs(part);
			assertThat(orderPart.getQuantity()).isEqualTo(2);
			assertThat(orderPart.getStockItemType()).isEqualTo(part.getType());
			assertThat(orderPart.getUnitPrice()).isEqualByComparingTo("40.00");
			assertThat(orderPart.getTotalPrice()).isEqualByComparingTo("80.00");
			assertThat(orderPart.getServiceOrder()).isSameAs(updated);
		}

		@Test
		void shouldRejectQuoteUpdateForFinalStatuses() {
			UUID externalId = UUID.randomUUID();
			var order = serviceOrder();
			order.setStatus(ServiceOrderStatusEnum.PENDING_APPROVAL);
			order.getBudget().setStatus(BudgetStatusEnum.SENT);
			var serviceOrderQuoteDto = new ServiceOrderQuoteDto("Diag", List.of(), List.of());

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));

			assertThatThrownBy(() -> service.updateQuote(externalId, serviceOrderQuoteDto))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Cannot update items for an Order that is already PENDING_APPROVAL & SENT");

			verify(repository, never()).save(any(ServiceOrder.class));
		}
	}

	@Nested
	class RequestCustomerApproval {

		@Test
		void shouldRequestCustomerApproval() {
			UUID externalId = UUID.randomUUID();
			var order = serviceOrder();
			order.setStatus(ServiceOrderStatusEnum.DIAGNOSIS);
			order.addLabor(serviceOrderLabor(MechanicServiceHelper.generateMechanicService(), 2));
			ArgumentCaptor<ServiceOrderStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(
				ServiceOrderStatusChangedEvent.class
			);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));
			when(repository.save(order)).thenReturn(order);

			var updated = service.requestCustomerApproval(externalId);

			assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatusEnum.PENDING_APPROVAL);
			assertThat(updated.getBudget().getStatus()).isEqualTo(BudgetStatusEnum.SENT);
			assertThat(updated.getEstimatedCompletionDate()).isNotNull();
			assertThat(updated.getEstimatedCompletionDays()).isEqualTo(1);
			assertThat(updated.getApprovalDate()).isNotNull();

			verify(eventPublisher).publishEvent(eventCaptor.capture());

			var event = eventCaptor.getValue();
			assertThat(event.serviceOrderExternalId()).isEqualTo(updated.getExternalId());
			assertThat(event.oldStatus()).isEqualTo(ServiceOrderStatusEnum.DIAGNOSIS);
			assertThat(event.newStatus()).isEqualTo(ServiceOrderStatusEnum.PENDING_APPROVAL);
		}
	}

	@Nested
	class ProcessBudgetResponse {

		@Test
		void shouldApproveBudgetWithdrawStockAndMoveToInProgress() {
			UUID externalId = UUID.randomUUID();
			var order = serviceOrder();
			order.setStatus(ServiceOrderStatusEnum.PENDING_APPROVAL);
			order.getBudget().setStatus(BudgetStatusEnum.SENT);
			order.addStockItem(serviceOrderStockItem(stockItem(), 2, new BigDecimal("20.00")));
			order.addStockItem(serviceOrderStockItem(stockItem(), 1, new BigDecimal("15.00")));
			ArgumentCaptor<ServiceOrderStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(
				ServiceOrderStatusChangedEvent.class
			);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));
			when(repository.save(order)).thenReturn(order);

			var updated = service.processBudgetResponse(externalId, true);

			assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatusEnum.IN_PROGRESS);
			assertThat(updated.getBudget().getStatus()).isEqualTo(BudgetStatusEnum.APPROVED);
			assertThat(updated.getApprovalDate()).isNotNull();

			verify(stockItemService).withdrawStock(order.getStockItems().get(0).getStockItem().getExternalId(), 2);
			verify(stockItemService).withdrawStock(order.getStockItems().get(1).getStockItem().getExternalId(), 1);
			verify(eventPublisher).publishEvent(eventCaptor.capture());

			var event = eventCaptor.getValue();
			assertThat(event.serviceOrderExternalId()).isEqualTo(updated.getExternalId());
			assertThat(event.oldStatus()).isEqualTo(ServiceOrderStatusEnum.PENDING_APPROVAL);
			assertThat(event.newStatus()).isEqualTo(ServiceOrderStatusEnum.IN_PROGRESS);
		}

		@Test
		void shouldRejectBudgetAndCancelServiceOrder() {
			UUID externalId = UUID.randomUUID();
			var order = serviceOrder();
			order.setStatus(ServiceOrderStatusEnum.PENDING_APPROVAL);
			order.getBudget().setStatus(BudgetStatusEnum.SENT);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));
			when(repository.save(order)).thenReturn(order);

			var updated = service.processBudgetResponse(externalId, false);

			assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatusEnum.CANCELED);
			assertThat(updated.getBudget().getStatus()).isEqualTo(BudgetStatusEnum.REJECTED);
		}
	}

	@Nested
	class FinishService {

		@Test
		void shouldSetActualCompletionDateWhenFinishingService() {
			UUID externalId = UUID.randomUUID();
			var order = serviceOrder();
			order.setStatus(ServiceOrderStatusEnum.IN_PROGRESS);
			order.setApprovalDate(order.getCreatedAt());
			ArgumentCaptor<ServiceOrderStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(
				ServiceOrderStatusChangedEvent.class
			);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));
			when(repository.save(order)).thenReturn(order);

			var updated = service.finishService(externalId);

			assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatusEnum.COMPLETED);
			assertThat(updated.getActualCompletionDate()).isNotNull();
			assertThat(updated.getActualCompletionDays()).isNotNull();

			verify(eventPublisher).publishEvent(eventCaptor.capture());
			assertThat(eventCaptor.getValue().newStatus()).isEqualTo(ServiceOrderStatusEnum.COMPLETED);
		}
	}

	@Nested
	class DeliverVehicle {

		@Test
		void shouldDeliverVehicleWhenServiceOrderIsCompleted() {
			UUID externalId = UUID.randomUUID();
			var order = serviceOrder();
			order.setStatus(ServiceOrderStatusEnum.COMPLETED);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));
			when(repository.save(order)).thenReturn(order);

			var updated = service.deliverVehicle(externalId);

			assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatusEnum.DELIVERED);
		}
	}

	@Nested
	class FindByExternalId {

		@Test
		void shouldFindServiceOrderByExternalId() {
			UUID externalId = UUID.randomUUID();
			var order = serviceOrder();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));

			var found = service.findByExternalId(externalId);

			assertThat(found).usingRecursiveAssertion().isEqualTo(order);
		}

		@Test
		void shouldThrowExceptionWhenServiceOrderNotFound() {
			UUID externalId = UUID.randomUUID();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.findByExternalId(externalId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Service Order not found for External ID: " + externalId);
		}
	}

	@Nested
	class Search {

		@SuppressWarnings("unchecked")
		private ArgumentCaptor<Example<ServiceOrder>> exampleCaptor() {
			return (ArgumentCaptor<Example<ServiceOrder>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Example.class);
		}

		@SuppressWarnings("unchecked")
		private Example<ServiceOrder> anyExample() {
			return any(Example.class);
		}

		@Test
		void shouldReturnServiceOrdersWhenSearchCriteriaIsProvided() {
			var order = serviceOrder();
			var user = UserHelper.generateUser();
			Pageable pageable = PageRequest.of(0, 10);
			Page<ServiceOrder> expectedPage = new PageImpl<>(List.of(order));
			ArgumentCaptor<Example<ServiceOrder>> captor = exampleCaptor();

			when(repository.findAll(anyExample(), eq(pageable))).thenReturn(expectedPage);

			var result = service.search("ABC1234", ServiceOrderStatusEnum.PENDING_APPROVAL, pageable, user);

			assertThat(result).isEqualTo(expectedPage);

			verify(repository).findAll(captor.capture(), eq(pageable));
			ServiceOrder probe = captor.getValue().getProbe();
			assertThat(probe.getStatus()).isEqualTo(ServiceOrderStatusEnum.PENDING_APPROVAL);
			assertThat(probe.getVehicle()).isNotNull();
			assertThat(probe.getVehicle().getLicensePlate()).isEqualTo("ABC1234");
		}
	}

	private ServiceOrder serviceOrder() {
		var order = new ServiceOrder();
		order.setId(1L);
		order.setExternalId(UUID.randomUUID());
		order.setVehicle(vehicle());
		order.setCustomerComplaint("Ruido");
		order.setOdometerReading(100000);
		order.setStatus(ServiceOrderStatusEnum.RECEIVED);
		order.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
		order.setBudget(budget());
		return order;
	}

	private Vehicle vehicle() {
		var vehicle = new Vehicle();
		vehicle.setId(1L);
		vehicle.setExternalId(UUID.randomUUID());
		vehicle.setLicensePlate("ABC1234");
		vehicle.setBrand("Volkswagen");
		vehicle.setModel("Gol");
		vehicle.setYear(2021);
		vehicle.setColor("Prata");
		vehicle.setOwner(UserHelper.generateUser());
		return vehicle;
	}

	private User mechanicUser() {
		var user = UserHelper.generateUser();
		user.setRole("MECHANIC");
		return user;
	}

	private StockItem stockItem() {
		var item = new StockItem();
		item.setId(1L);
		item.setExternalId(UUID.randomUUID());
		item.setCode("PT-01");
		item.setName("Filtro");
		item.setType(StockItemTypeEnum.PART);
		item.setQuantity(10);
		item.setPendingDemand(0);
		item.setCostPrice(new BigDecimal("10.00"));
		item.setSalePrice(new BigDecimal("20.00"));
		return item;
	}

	private ServiceOrderStockItem serviceOrderStockItem(StockItem item, int quantity, BigDecimal totalPrice) {
		var orderItem = new ServiceOrderStockItem();
		orderItem.setStockItem(item);
		orderItem.setStockItemType(item.getType());
		orderItem.setQuantity(quantity);
		orderItem.setUnitPrice(totalPrice);
		orderItem.setTotalPrice(totalPrice);
		return orderItem;
	}

	private ServiceOrderLabor serviceOrderLabor(
		MechanicService mechanicService,
		int quantity
	) {
		var labor = new ServiceOrderLabor();
		labor.setMechanicService(mechanicService);
		labor.setQuantity(quantity);
		labor.setUnitPrice(mechanicService.getPrice());
		labor.setTotalPrice(mechanicService.getPrice().multiply(BigDecimal.valueOf(quantity)));
		return labor;
	}

	private Budget budget() {
		var budget = new Budget();
		budget.setExternalId(UUID.randomUUID());
		budget.setTotalAmount(BigDecimal.ZERO);
		budget.setStatus(BudgetStatusEnum.OPEN);
		return budget;
	}
}
