package org.project.mechanic_shop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderLaborManDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderStatusUpdateDto;
import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderStockItemManDto;
import org.project.mechanic_shop.models.MechanicService;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.ServiceOrderStockItem;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.Vehicle;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;
import org.project.mechanic_shop.models.enums.UserRoleEnum;
import org.project.mechanic_shop.repositories.MechanicServiceRepository;
import org.project.mechanic_shop.repositories.ServiceOrderRepository;
import org.project.mechanic_shop.repositories.StockItemRepository;
import org.project.mechanic_shop.repositories.UserRepository;
import org.project.mechanic_shop.repositories.VehicleRepository;
import org.project.mechanic_shop.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ServiceOrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private MechanicServiceRepository mechanicServiceRepository;

    @Test
    void shouldCreateServiceOrderAndPersistIt() throws Exception {
        User owner = userRepository.save(buildUser(
                "52998224725",
                "Cliente Ordem",
                "customer.service-order.create@test.com",
                UserRoleEnum.CUSTOMER
        ));
        User mechanic = userRepository.save(buildUser(
                "39053344705",
                "Mecanico Ordem",
                "mechanic.service-order.create@test.com",
                UserRoleEnum.MECHANIC
        ));
        Vehicle vehicle = vehicleRepository.save(buildVehicle("ABC1D23", owner));

        ServiceOrderCreateDto payload = new ServiceOrderCreateDto(
                vehicle.getExternalId(),
                "Motor falhando",
                45210,
                mechanic.getExternalId()
        );

        String responseBody = mockMvc.perform(
                        post("/api/v1/service-orders/create")
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID externalId = UUID.fromString(objectMapper.readTree(responseBody).get("data").asText());
        ServiceOrder persistedOrder = serviceOrderRepository.findByExternalId(externalId).orElseThrow();

        assertThat(persistedOrder.getVehicle().getExternalId()).isEqualTo(vehicle.getExternalId());
        assertThat(persistedOrder.getResponsibleMechanic().getExternalId()).isEqualTo(mechanic.getExternalId());
        assertThat(persistedOrder.getCustomerComplaint()).isEqualTo(payload.customerComplaint());
        assertThat(persistedOrder.getOdometerReading()).isEqualTo(payload.odometerReading());
        assertThat(persistedOrder.getStatus()).isEqualTo(ServiceOrderStatusEnum.RECEIVED);
        assertThat(persistedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnValidationErrorWhenCreatePayloadIsInvalid() throws Exception {
        ServiceOrderCreateDto invalidPayload = new ServiceOrderCreateDto(
                null,
                "",
                45210,
                null
        );

        mockMvc.perform(
                        post("/api/v1/service-orders/create")
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidPayload))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_CONTENT.value()))
                .andExpect(jsonPath("$.message").value("Validation error."))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldFindServiceOrderByExternalIdUsingRealRepository() throws Exception {
        User owner = userRepository.save(buildUser(
                "11144477735",
                "Cliente Busca Ordem",
                "customer.service-order.find@test.com",
                UserRoleEnum.CUSTOMER
        ));
        User mechanic = userRepository.save(buildUser(
                "16899535009",
                "Mecanico Busca Ordem",
                "mechanic.service-order.find@test.com",
                UserRoleEnum.MECHANIC
        ));
        Vehicle vehicle = vehicleRepository.save(buildVehicle("DEF1G45", owner));
        ServiceOrder savedOrder = serviceOrderRepository.save(buildServiceOrder(vehicle, mechanic, "Barulho ao frear"));

        mockMvc.perform(
                        get("/api/v1/service-orders/{id}", savedOrder.getExternalId())
                                .with(AuthUtil.admin())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.externalId").value(savedOrder.getExternalId().toString()))
                .andExpect(jsonPath("$.data.customerComplaint").value(savedOrder.getCustomerComplaint()))
                .andExpect(jsonPath("$.data.vehicle.licensePlate").value(vehicle.getLicensePlate()))
                .andExpect(jsonPath("$.data.responsibleMechanic.externalId").value(mechanic.getExternalId().toString()));
    }

    @Test
    void shouldUpdateQuoteUsingRealServiceAndRepository() throws Exception {
        User owner = userRepository.save(buildUser(
                "45317828791",
                "Cliente Orcamento",
                "customer.service-order.quote@test.com",
                UserRoleEnum.CUSTOMER
        ));
        User mechanic = userRepository.save(buildUser(
                "56812345087",
                "Mecanico Orcamento",
                "mechanic.service-order.quote@test.com",
                UserRoleEnum.MECHANIC
        ));
        Vehicle vehicle = vehicleRepository.save(buildVehicle("GHI1J67", owner));
        ServiceOrder savedOrder = serviceOrderRepository.save(buildServiceOrder(vehicle, mechanic, "Vazamento de oleo"));
        StockItem stockItem = stockItemRepository.save(buildStockItem("P-SO-01", "Filtro de Oleo", 10, "20.00"));
        MechanicService mechanicService = mechanicServiceRepository.save(buildMechanicService("Troca de Oleo", "150.00"));

        ServiceOrderQuoteDto payload = new ServiceOrderQuoteDto(
                "Troca de oleo e filtro",
                List.of(new ServiceOrderStockItemManDto(stockItem.getExternalId(), 1)),
                List.of(new ServiceOrderLaborManDto(mechanicService.getExternalId(), 1))
        );

        mockMvc.perform(
                        put("/api/v1/service-orders/{id}/quote", savedOrder.getExternalId())
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.externalId").value(savedOrder.getExternalId().toString()))
                .andExpect(jsonPath("$.data.totalAmount").value(170.0))
                .andExpect(jsonPath("$.data.licensePlate").value(vehicle.getLicensePlate()));

        ServiceOrder updatedOrder = serviceOrderRepository.findByExternalId(savedOrder.getExternalId()).orElseThrow();

        assertThat(updatedOrder.getMechanicDiagnosis()).isEqualTo(payload.mechanicDiagnosis());
        assertThat(updatedOrder.getTotalAmount()).isEqualByComparingTo("170.00");
        assertThat(updatedOrder.getStockItems()).hasSize(1);
        assertThat(updatedOrder.getLabors()).hasSize(1);
        assertThat(updatedOrder.getStockItems().getFirst().getStockItem().getExternalId()).isEqualTo(stockItem.getExternalId());
        assertThat(updatedOrder.getLabors().getFirst().getMechanicService().getExternalId()).isEqualTo(mechanicService.getExternalId());
    }

    @Test
    void shouldUpdateStatusAndWithdrawStockUsingRealServiceAndRepository() throws Exception {
        User owner = userRepository.save(buildUser(
                "81731234060",
                "Cliente Status",
                "customer.service-order.status@test.com",
                UserRoleEnum.CUSTOMER
        ));
        User mechanic = userRepository.save(buildUser(
                "93453224031",
                "Mecanico Status",
                "mechanic.service-order.status@test.com",
                UserRoleEnum.MECHANIC
        ));
        Vehicle vehicle = vehicleRepository.save(buildVehicle("JKL2M89", owner));
        StockItem stockItem = stockItemRepository.save(buildStockItem("P-SO-02", "Correia Dentada", 5, "80.00"));

        ServiceOrder serviceOrder = buildServiceOrder(vehicle, mechanic, "Ruido no motor");
        serviceOrder.setStatus(ServiceOrderStatusEnum.PENDING_APPROVAL);

        ServiceOrderStockItem orderPart = new ServiceOrderStockItem();
        orderPart.setStockItem(stockItem);
        orderPart.setStockItemType(stockItem.getType());
        orderPart.setQuantity(2);
        orderPart.setUnitPrice(stockItem.getSalePrice());
        orderPart.setTotalPrice(new BigDecimal("160.00"));
        serviceOrder.addStockItem(orderPart);
        serviceOrder.setTotalAmount(new BigDecimal("160.00"));

        ServiceOrder savedOrder = serviceOrderRepository.save(serviceOrder);

        mockMvc.perform(
                        patch("/api/v1/service-orders/{id}/status", savedOrder.getExternalId())
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new ServiceOrderStatusUpdateDto(ServiceOrderStatusEnum.APPROVED)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.status").value(ServiceOrderStatusEnum.APPROVED.name()));

        ServiceOrder updatedOrder = serviceOrderRepository.findByExternalId(savedOrder.getExternalId()).orElseThrow();
        StockItem updatedItem = stockItemRepository.findByExternalId(stockItem.getExternalId()).orElseThrow();

        assertThat(updatedOrder.getStatus()).isEqualTo(ServiceOrderStatusEnum.APPROVED);
        assertThat(updatedOrder.getApprovalDate()).isNotNull();
        assertThat(updatedItem.getQuantity()).isEqualTo(3);
        assertThat(updatedItem.getPendingDemand()).isZero();
    }

    @Test
    void shouldSearchServiceOrdersUsingPersistedData() throws Exception {
        User owner = userRepository.save(buildUser(
                "22456789012",
                "Cliente Pesquisa Ordem",
                "customer.service-order.search@test.com",
                UserRoleEnum.CUSTOMER
        ));
        User mechanic = userRepository.save(buildUser(
                "78234567019",
                "Mecanico Pesquisa Ordem",
                "mechanic.service-order.search@test.com",
                UserRoleEnum.MECHANIC
        ));
        Vehicle vehicle = vehicleRepository.save(buildVehicle("NOP3Q12", owner));
        ServiceOrder savedOrder = serviceOrderRepository.save(buildServiceOrder(vehicle, mechanic, "Freio baixo"));

        mockMvc.perform(
                        get("/api/v1/service-orders/search")
                                .with(AuthUtil.admin())
                                .param("licensePlate", vehicle.getLicensePlate())
                                .param("status", savedOrder.getStatus().name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.content[0].externalId").value(savedOrder.getExternalId().toString()))
                .andExpect(jsonPath("$.data.content[0].licensePlate").value(vehicle.getLicensePlate()))
                .andExpect(jsonPath("$.data.content[0].customerName").value(owner.getName()))
                .andExpect(jsonPath("$.data.content[0].status").value(savedOrder.getStatus().name()));
    }

    private User buildUser(String document, String name, String email, UserRoleEnum role) {
        User user = new User();
        user.setDocument(document);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role.name());
        user.setActive(true);
        user.setPassword("Secret@123");
        user.setPhone("11999990000");
        return user;
    }

    private Vehicle buildVehicle(String licensePlate, User owner) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setBrand("Ford");
        vehicle.setModel("Ka");
        vehicle.setYear(2021);
        vehicle.setColor("Prata");
        vehicle.setOwner(owner);
        return vehicle;
    }

    private ServiceOrder buildServiceOrder(Vehicle vehicle, User mechanic, String complaint) {
        ServiceOrder order = new ServiceOrder();
        order.setVehicle(vehicle);
        order.setResponsibleMechanic(mechanic);
        order.setCustomerComplaint(complaint);
        order.setOdometerReading(45210);
        order.setStatus(ServiceOrderStatusEnum.RECEIVED);
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }

    private StockItem buildStockItem(String code, String name, int quantity, String salePrice) {
        StockItem item = new StockItem();
        item.setCode(code);
        item.setName(name);
        item.setType(StockItemTypeEnum.PART);
        item.setDescription("Item para OS");
        item.setQuantity(quantity);
        item.setPendingDemand(0);
        item.setCostPrice(new BigDecimal("50.00"));
        item.setSalePrice(new BigDecimal(salePrice));
        return item;
    }

    private MechanicService buildMechanicService(String name, String price) {
        MechanicService service = new MechanicService();
        service.setName(name);
        service.setDescription("Servico de manutencao");
        service.setEstimatedTimeMinutes(60);
        service.setPrice(new BigDecimal(price));
        return service;
    }
}
