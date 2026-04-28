package org.project.mechanic_shop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.project.mechanic_shop.config.ObjectMapperConfig;
import org.project.mechanic_shop.config.SecurityConfig;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderLaborManDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderStatusUpdateDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderStockItemManDto;
import org.project.mechanic_shop.mappers.ServiceOrderMapperImpl;
import org.project.mechanic_shop.mappers.UserMapperImpl;
import org.project.mechanic_shop.mappers.VehicleMapperImpl;
import org.project.mechanic_shop.models.MechanicService;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.ServiceOrderLabor;
import org.project.mechanic_shop.models.ServiceOrderStockItem;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.Vehicle;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;
import org.project.mechanic_shop.models.enums.UserRoleEnum;
import org.project.mechanic_shop.services.ServiceOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        SecurityConfig.class,
        ServiceOrderMapperImpl.class,
        VehicleMapperImpl.class,
        UserMapperImpl.class,
        ObjectMapperConfig.class
})
class ServiceOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceOrderService service;

    @Nested
    class FindById {
        @Test
        void shouldFindServiceOrderByExternalId() throws Exception {
            var externalId = UUID.randomUUID();
            var serviceOrder = buildServiceOrder();

            when(service.findByExternalId(externalId)).thenReturn(serviceOrder);

            mockMvc.perform(get("/api/v1/service-orders/{id}", externalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.customerComplaint").value(serviceOrder.getCustomerComplaint()))
                    .andExpect(jsonPath("$.data.vehicle.licensePlate").value(serviceOrder.getVehicle().getLicensePlate()))
                    .andExpect(jsonPath("$.data.responsibleMechanic.name").value(serviceOrder.getResponsibleMechanic().getName()))
                    .andExpect(jsonPath("$.data.stockItems[0].partCode").value(serviceOrder.getStockItems().getFirst().getStockItem().getCode()));
        }

        @Test
        void shouldReturnNotFoundWhenServiceOrderDoesNotExist() throws Exception {
            var externalId = UUID.randomUUID();

            when(service.findByExternalId(externalId)).thenThrow(EntityNotFoundException.class);

            mockMvc.perform(get("/api/v1/service-orders/{id}", externalId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Create {
        @Test
        void shouldCreateServiceOrder() throws Exception {
            var serviceOrder = buildServiceOrder();
            var dto = buildCreateDto(serviceOrder.getVehicle().getExternalId(), serviceOrder.getResponsibleMechanic().getExternalId());

            when(service.createServiceOrder(dto)).thenReturn(serviceOrder);

            mockMvc.perform(post("/api/v1/service-orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data").value(serviceOrder.getExternalId().toString()));
        }

        @Test
        void shouldReturnConflictWhenCreateFails() throws Exception {
            var dto = buildCreateDto(UUID.randomUUID(), UUID.randomUUID());

            when(service.createServiceOrder(dto)).thenThrow(new IllegalArgumentException("invalid"));

            mockMvc.perform(post("/api/v1/service-orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
        }
    }

    @Nested
    class UpdateQuote {
        @Test
        void shouldUpdateQuote() throws Exception {
            var externalId = UUID.randomUUID();
            var serviceOrder = buildServiceOrder();
            var dto = buildQuoteDto(
                    serviceOrder.getStockItems().getFirst().getStockItem().getExternalId(),
                    serviceOrder.getLabors().getFirst().getMechanicService().getExternalId()
            );

            when(service.updateQuote(externalId, dto)).thenReturn(serviceOrder);

            mockMvc.perform(put("/api/v1/service-orders/{id}/quote", externalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.externalId").value(serviceOrder.getExternalId().toString()))
                    .andExpect(jsonPath("$.data.totalAmount").value(170.0))
                    .andExpect(jsonPath("$.data.licensePlate").value(serviceOrder.getVehicle().getLicensePlate()));
        }

        @Test
        void shouldReturnConflictWhenUpdateQuoteFails() throws Exception {
            var externalId = UUID.randomUUID();
            var dto = buildQuoteDto(UUID.randomUUID(), UUID.randomUUID());

            when(service.updateQuote(externalId, dto)).thenThrow(new IllegalArgumentException("invalid"));

            mockMvc.perform(put("/api/v1/service-orders/{id}/quote", externalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
        }
    }

    @Nested
    class UpdateStatus {
        @Test
        void shouldUpdateStatus() throws Exception {
            var externalId = UUID.randomUUID();
            var serviceOrder = buildServiceOrder();
            serviceOrder.setStatus(ServiceOrderStatusEnum.APPROVED);
            var dto = new ServiceOrderStatusUpdateDto(ServiceOrderStatusEnum.APPROVED);

            when(service.updateStatus(externalId, dto.status())).thenReturn(serviceOrder);

            mockMvc.perform(patch("/api/v1/service-orders/{id}/status", externalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.status").value(ServiceOrderStatusEnum.APPROVED.name()));
        }
    }

    @Nested
    class Search {
        @Test
        void shouldSearchWithPageable() throws Exception {
            var serviceOrder = buildServiceOrder();
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            Page<ServiceOrder> page = new PageImpl<>(List.of(serviceOrder), PageRequest.of(0, 2), 1);

            when(service.search(
                    eq(serviceOrder.getVehicle().getLicensePlate()),
                    eq(serviceOrder.getStatus()),
                    any(Pageable.class)
            )).thenReturn(page);

            mockMvc.perform(get("/api/v1/service-orders/search")
                            .param("licensePlate", serviceOrder.getVehicle().getLicensePlate())
                            .param("status", serviceOrder.getStatus().name())
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "createdAt,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.content[0].licensePlate").value(serviceOrder.getVehicle().getLicensePlate()))
                    .andExpect(jsonPath("$.data.content[0].customerName").value(serviceOrder.getVehicle().getOwner().getName()));

            verify(service).search(
                    eq(serviceOrder.getVehicle().getLicensePlate()),
                    eq(serviceOrder.getStatus()),
                    captor.capture()
            );

            Pageable pageable = captor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").isAscending()).isTrue();
        }

        @Test
        void shouldSearchWithDefaultPageable() throws Exception {
            var serviceOrder = buildServiceOrder();
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            Page<ServiceOrder> page = new PageImpl<>(List.of(serviceOrder), PageRequest.of(0, 2), 1);

            when(service.search(eq(serviceOrder.getVehicle().getLicensePlate()), any(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/service-orders/search")
                            .param("licensePlate", serviceOrder.getVehicle().getLicensePlate()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.content[0].licensePlate").value(serviceOrder.getVehicle().getLicensePlate()));

            verify(service).search(eq(serviceOrder.getVehicle().getLicensePlate()), eq(null), captor.capture());

            Pageable pageable = captor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        }
    }

    private ServiceOrder buildServiceOrder() {
        User customer = new User();
        customer.setId(1L);
        customer.setExternalId(UUID.randomUUID());
        customer.setDocument("57096255079");
        customer.setName("Cliente Teste");
        customer.setEmail("cliente@test.com");
        customer.setRole(UserRoleEnum.CUSTOMER.name());
        customer.setActive(true);
        customer.setPassword("Secret@123");

        User mechanic = new User();
        mechanic.setId(2L);
        mechanic.setExternalId(UUID.randomUUID());
        mechanic.setDocument("39053344705");
        mechanic.setName("Mecanico Teste");
        mechanic.setEmail("mecanico@test.com");
        mechanic.setRole(UserRoleEnum.MECHANIC.name());
        mechanic.setActive(true);
        mechanic.setPassword("Secret@123");

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setExternalId(UUID.randomUUID());
        vehicle.setLicensePlate("ABC1D23");
        vehicle.setBrand("Ford");
        vehicle.setModel("Ka");
        vehicle.setYear(2022);
        vehicle.setColor("Prata");
        vehicle.setOwner(customer);

        StockItem stockItem = new StockItem();
        stockItem.setId(1L);
        stockItem.setExternalId(UUID.randomUUID());
        stockItem.setCode("P-100");
        stockItem.setName("Filtro de oleo");
        stockItem.setType(StockItemTypeEnum.PART);
        stockItem.setQuantity(10);
        stockItem.setCostPrice(new BigDecimal("10.00"));
        stockItem.setSalePrice(new BigDecimal("20.00"));

        MechanicService mechanicService = new MechanicService();
        mechanicService.setId(1L);
        mechanicService.setExternalId(UUID.randomUUID());
        mechanicService.setName("Troca de oleo");
        mechanicService.setDescription("Servico");
        mechanicService.setEstimatedTimeMinutes(60);
        mechanicService.setPrice(new BigDecimal("150.00"));

        ServiceOrder order = new ServiceOrder();
        order.setId(1L);
        order.setExternalId(UUID.randomUUID());
        order.setVehicle(vehicle);
        order.setCustomerComplaint("Barulho no motor");
        order.setOdometerReading(45210);
        order.setResponsibleMechanic(mechanic);
        order.setMechanicDiagnosis("Trocar filtro e oleo");
        order.setStatus(ServiceOrderStatusEnum.DIAGNOSIS);
        order.setTotalAmount(new BigDecimal("170.00"));

        ServiceOrderStockItem orderPart = new ServiceOrderStockItem();
        orderPart.setId(1L);
        orderPart.setStockItem(stockItem);
        orderPart.setStockItemType(stockItem.getType());
        orderPart.setQuantity(1);
        orderPart.setUnitPrice(stockItem.getSalePrice());
        orderPart.setTotalPrice(new BigDecimal("20.00"));
        order.addStockItem(orderPart);

        ServiceOrderLabor labor = new ServiceOrderLabor();
        labor.setId(1L);
        labor.setMechanicService(mechanicService);
        labor.setQuantity(1);
        labor.setUnitPrice(mechanicService.getPrice());
        labor.setTotalPrice(new BigDecimal("150.00"));
        order.addLabor(labor);

        return order;
    }

    private ServiceOrderCreateDto buildCreateDto(UUID vehicleExternalId, UUID mechanicExternalId) {
        return new ServiceOrderCreateDto(vehicleExternalId, "Barulho no motor", 45210, mechanicExternalId);
    }

    private ServiceOrderQuoteDto buildQuoteDto(UUID partExternalId, UUID mechanicServiceExternalId) {
        return new ServiceOrderQuoteDto(
                "Trocar filtro e oleo",
                List.of(new ServiceOrderStockItemManDto(partExternalId, 1)),
                List.of(new ServiceOrderLaborManDto(mechanicServiceExternalId, 1))
        );
    }
}
