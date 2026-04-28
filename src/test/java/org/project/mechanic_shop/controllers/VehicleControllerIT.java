package org.project.mechanic_shop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.project.mechanic_shop.dto.vehicle_dto.VehicleManDto;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.Vehicle;
import org.project.mechanic_shop.models.enums.UserRoleEnum;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class VehicleControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateVehicleAndPersistIt() throws Exception {
        User owner = userRepository.save(buildUser(
                "52998224725",
                "Dono Integracao",
                "owner.vehicle.create@test.com",
                "11999991001"
        ));

        VehicleManDto payload = new VehicleManDto(
                "ABC1D23",
                "Chevrolet",
                "Onix",
                2021,
                "Branco",
                owner.getExternalId()
        );

        String responseBody = mockMvc.perform(
                        post("/api/vehicles/create")
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

        Vehicle persistedVehicle = vehicleRepository.findByExternalId(
                java.util.UUID.fromString(objectMapper.readTree(responseBody).get("data").asText())
        ).orElseThrow();

        assertThat(persistedVehicle.getLicensePlate()).isEqualTo(payload.licensePlate());
        assertThat(persistedVehicle.getBrand()).isEqualTo(payload.brand());
        assertThat(persistedVehicle.getModel()).isEqualTo(payload.model());
        assertThat(persistedVehicle.getYear()).isEqualTo(payload.year());
        assertThat(persistedVehicle.getColor()).isEqualTo(payload.color());
        assertThat(persistedVehicle.getOwner().getExternalId()).isEqualTo(owner.getExternalId());
    }

    @Test
    void shouldReturnValidationErrorWhenCreatePayloadIsInvalid() throws Exception {
        VehicleManDto invalidPayload = new VehicleManDto(
                "INVALIDA",
                "",
                "",
                1800,
                "<b>preto</b>",
                null
        );

        mockMvc.perform(
                        post("/api/vehicles/create")
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
    void shouldFindVehicleByExternalIdUsingRealRepository() throws Exception {
        User owner = userRepository.save(buildUser(
                "11144477735",
                "Dono Busca",
                "owner.vehicle.find@test.com",
                "11999991002"
        ));
        Vehicle savedVehicle = vehicleRepository.save(buildVehicle("DEF1G45", "Fiat", "Argo", owner));

        mockMvc.perform(
                        get("/api/vehicles/{id}", savedVehicle.getExternalId())
                                .with(AuthUtil.admin())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.externalId").value(savedVehicle.getExternalId().toString()))
                .andExpect(jsonPath("$.data.licensePlate").value(savedVehicle.getLicensePlate()))
                .andExpect(jsonPath("$.data.owner.externalId").value(owner.getExternalId().toString()))
                .andExpect(jsonPath("$.data.owner.name").value(owner.getName()));
    }

    @Test
    void shouldUpdateVehicleUsingRealServiceAndRepository() throws Exception {
        User originalOwner = userRepository.save(buildUser(
                "16899535009",
                "Dono Original",
                "owner.vehicle.original@test.com",
                "11999991003"
        ));
        User updatedOwner = userRepository.save(buildUser(
                "39053344705",
                "Dono Atualizado",
                "owner.vehicle.updated@test.com",
                "11999991004"
        ));
        Vehicle savedVehicle = vehicleRepository.save(buildVehicle("GHI1J67", "Volkswagen", "Gol", originalOwner));

        VehicleManDto updatePayload = new VehicleManDto(
                "JKL2M89",
                "Hyundai",
                "HB20",
                2023,
                "Cinza",
                updatedOwner.getExternalId()
        );

        mockMvc.perform(
                        put("/api/vehicles/{id}", savedVehicle.getExternalId())
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePayload))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.externalId").value(savedVehicle.getExternalId().toString()))
                .andExpect(jsonPath("$.data.licensePlate").value(updatePayload.licensePlate()))
                .andExpect(jsonPath("$.data.brand").value(updatePayload.brand()))
                .andExpect(jsonPath("$.data.owner.externalId").value(updatedOwner.getExternalId().toString()));

        Vehicle updatedVehicle = vehicleRepository.findByExternalId(savedVehicle.getExternalId()).orElseThrow();

        assertThat(updatedVehicle.getLicensePlate()).isEqualTo(updatePayload.licensePlate());
        assertThat(updatedVehicle.getBrand()).isEqualTo(updatePayload.brand());
        assertThat(updatedVehicle.getModel()).isEqualTo(updatePayload.model());
        assertThat(updatedVehicle.getOwner().getExternalId()).isEqualTo(updatedOwner.getExternalId());
    }

    @Test
    void shouldSearchVehiclesUsingPersistedData() throws Exception {
        User owner = userRepository.save(buildUser(
                "45317828791",
                "Dono Pesquisa",
                "owner.vehicle.search@test.com",
                "11999991005"
        ));
        Vehicle savedVehicle = vehicleRepository.save(buildVehicle("NOP3Q12", "Toyota", "Yaris", owner));

        mockMvc.perform(
                        get("/api/vehicles/search")
                                .with(AuthUtil.admin())
                                .param("licensePlate", savedVehicle.getLicensePlate())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.content[0].externalId").value(savedVehicle.getExternalId().toString()))
                .andExpect(jsonPath("$.data.content[0].licensePlate").value(savedVehicle.getLicensePlate()))
                .andExpect(jsonPath("$.data.content[0].ownerName").value(owner.getName()));
    }

    private User buildUser(String document, String name, String email, String phone) {
        User user = new User();
        user.setDocument(document);
        user.setName(name);
        user.setEmail(email);
        user.setRole(UserRoleEnum.CUSTOMER.name());
        user.setActive(true);
        user.setPassword("Secret@123");
        user.setPhone(phone);
        return user;
    }

    private Vehicle buildVehicle(String licensePlate, String brand, String model, User owner) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(2020);
        vehicle.setColor("Prata");
        vehicle.setOwner(owner);
        return vehicle;
    }
}
