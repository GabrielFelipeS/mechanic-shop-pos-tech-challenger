package org.project.mechanic_shop.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderLaborManDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderStockItemManDto;
import org.project.mechanic_shop.events.NewServiceOrderEvent;
import org.project.mechanic_shop.events.ServiceOrderStatusChangedEvent;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.ServiceOrderLabor;
import org.project.mechanic_shop.models.ServiceOrderStockItem;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.Vehicle;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;
import org.project.mechanic_shop.repositories.ServiceOrderRepository;
import org.project.mechanic_shop.services.MechanicServiceService;
import org.project.mechanic_shop.services.ServiceOrderService;
import org.project.mechanic_shop.services.StockItemService;
import org.project.mechanic_shop.services.UserService;
import org.project.mechanic_shop.services.VehicleService;
import org.project.mechanic_shop.utils.MechanicServiceHelper;
import org.project.mechanic_shop.utils.UserHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
            var dto = new ServiceOrderCreateDto(vehicleId, "Barulho no freio", 125000, null);

            when(vehicleService.findByExternalId(vehicleId)).thenReturn(vehicle);
            when(repository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var created = service.createServiceOrder(dto);

            assertThat(created.getVehicle()).isSameAs(vehicle);
            assertThat(created.getCustomerComplaint()).isEqualTo("Barulho no freio");
            assertThat(created.getOdometerReading()).isEqualTo(125000);
            assertThat(created.getStatus()).isEqualTo(ServiceOrderStatusEnum.RECEIVED);
            assertThat(created.getTotalAmount()).isEqualByComparingTo("0");
            assertThat(created.getResponsibleMechanic()).isNull();

            verify(eventPublisher, never()).publishEvent(any(NewServiceOrderEvent.class));
        }

        @Test
        void shouldCreateServiceOrderAndPublishEventWhenMechanicIsAssigned() {
            UUID vehicleId = UUID.randomUUID();
            UUID mechanicId = UUID.randomUUID();
            var vehicle = vehicle();
            var mechanic = mechanicUser();
            var dto = new ServiceOrderCreateDto(vehicleId, "Trocar embreagem", 90000, mechanicId);
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
            assertThat(updated.getLabors()).hasSize(1);
            assertThat(updated.getStockItems()).hasSize(1);
            assertThat(updated.getTotalAmount()).isEqualByComparingTo("530.00");

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
            order.setStatus(ServiceOrderStatusEnum.APPROVED);
            var serviceOrderQutodeDto = new ServiceOrderQuoteDto("Diag", List.of(), List.of());

            when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> service.updateQuote(externalId,serviceOrderQutodeDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot update items for an Order that is already APPROVED");

            verify(repository, never()).save(any(ServiceOrder.class));
        }
    }

    @Nested
    class UpdateStatus {
        @Test
        void shouldApproveOrderWithdrawStockAndPublishEvent() {
            UUID externalId = UUID.randomUUID();
            var order = serviceOrder();
            order.setStatus(ServiceOrderStatusEnum.RECEIVED);
            order.addStockItem(serviceOrderStockItem(stockItem(), 2, new BigDecimal("20.00")));
            order.addStockItem(serviceOrderStockItem(stockItem(), 1, new BigDecimal("15.00")));
            ArgumentCaptor<ServiceOrderStatusChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ServiceOrderStatusChangedEvent.class);

            when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));
            when(repository.save(order)).thenReturn(order);

            var updated = service.updateStatus(externalId, ServiceOrderStatusEnum.APPROVED);

            assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatusEnum.APPROVED);
            assertThat(updated.getApprovalDate()).isNotNull();

            verify(stockItemService).withdrawStock(order.getStockItems().get(0).getStockItem().getExternalId(), 2);
            verify(stockItemService).withdrawStock(order.getStockItems().get(1).getStockItem().getExternalId(), 1);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            var event = eventCaptor.getValue();
            assertThat(event.serviceOrder()).isSameAs(updated);
            assertThat(event.oldStatus()).isEqualTo(ServiceOrderStatusEnum.RECEIVED);
            assertThat(event.newStatus()).isEqualTo(ServiceOrderStatusEnum.APPROVED);
        }

        @Test
        void shouldSetCompletionDateWhenCompletingOrder() {
            UUID externalId = UUID.randomUUID();
            var order = serviceOrder();
            order.setStatus(ServiceOrderStatusEnum.IN_PROGRESS);

            when(repository.findByExternalId(externalId)).thenReturn(Optional.of(order));
            when(repository.save(order)).thenReturn(order);

            var updated = service.updateStatus(externalId, ServiceOrderStatusEnum.COMPLETED);

            assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatusEnum.COMPLETED);
            assertThat(updated.getCompletionDate()).isNotNull();
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

            assertThat(found)
                    .usingRecursiveAssertion()
                    .isEqualTo(order);
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
            Pageable pageable = PageRequest.of(0, 10);
            Page<ServiceOrder> expectedPage = new PageImpl<>(List.of(order));
            ArgumentCaptor<Example<ServiceOrder>> captor = exampleCaptor();

            when(repository.findAll(anyExample(), eq(pageable))).thenReturn(expectedPage);

            var result = service.search("ABC1234", ServiceOrderStatusEnum.PENDING_APPROVAL, pageable);

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
        order.setTotalAmount(BigDecimal.ZERO);
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
}
