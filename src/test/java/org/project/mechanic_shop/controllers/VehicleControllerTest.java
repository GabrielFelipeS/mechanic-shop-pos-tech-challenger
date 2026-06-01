package org.project.mechanic_shop.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.project.mechanic_shop.application.mappers.UserMapperImpl;
import org.project.mechanic_shop.application.mappers.VehicleMapperImpl;
import org.project.mechanic_shop.presentation.controllers.VehicleController;
import org.project.mechanic_shop.config.ObjectMapperConfig;
import org.project.mechanic_shop.config.SecurityConfig;
import org.project.mechanic_shop.domain.dto.vehicle_dto.VehicleManDto;

import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.domain.enums.UserRoleEnum;
import org.project.mechanic_shop.application.services.VehicleService;
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

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ SecurityConfig.class, VehicleMapperImpl.class, UserMapperImpl.class, ObjectMapperConfig.class })
class VehicleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private VehicleService service;

	@Nested
	class FindById {

		@Test
		void shouldFindVehicleByExternalId() throws Exception {
			var externalId = UUID.randomUUID();
			var vehicle = buildVehicle();

			when(service.findByExternalId(externalId)).thenReturn(vehicle);

			mockMvc
				.perform(get("/api/vehicles/{id}", externalId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.licensePlate").value(vehicle.getLicensePlate()))
				.andExpect(jsonPath("$.data.owner.name").value(vehicle.getOwner().getName()));
		}

		@Test
		void shouldReturnNotFoundWhenVehicleDoesNotExist() throws Exception {
			var externalId = UUID.randomUUID();

			when(service.findByExternalId(externalId)).thenThrow(EntityNotFoundException.class);

			mockMvc.perform(get("/api/vehicles/{id}", externalId)).andExpect(status().isNotFound());
		}
	}

	@Nested
	class Create {

		@Test
		void shouldCreateVehicle() throws Exception {
			var vehicle = buildVehicle();
			var dto = buildVehicleManDto(vehicle.getOwner().getExternalId());

			when(service.create(any(Vehicle.class), eq(dto.ownerId()))).thenReturn(vehicle);

			mockMvc
				.perform(
					post("/api/vehicles/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data").value(vehicle.getExternalId().toString()));
		}

		@Test
		void shouldReturnConflictWhenCreateFails() throws Exception {
			var dto = buildVehicleManDto(UUID.randomUUID());

			when(service.create(any(Vehicle.class), eq(dto.ownerId()))).thenThrow(
				new IllegalArgumentException("duplicate")
			);

			mockMvc
				.perform(
					post("/api/vehicles/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
		}
	}

	@Nested
	class Update {

		@Test
		void shouldUpdateVehicle() throws Exception {
			var externalId = UUID.randomUUID();
			var vehicle = buildVehicle();
			var dto = buildVehicleManDto(vehicle.getOwner().getExternalId());

			when(service.update(eq(externalId), any(Vehicle.class), eq(dto.ownerId()))).thenReturn(vehicle);

			mockMvc
				.perform(
					put("/api/vehicles/{id}", externalId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.externalId").value(vehicle.getExternalId().toString()))
				.andExpect(jsonPath("$.data.licensePlate").value(vehicle.getLicensePlate()));

			verify(service).update(eq(externalId), any(Vehicle.class), eq(dto.ownerId()));
		}

		@Test
		void shouldReturnConflictWhenUpdateFails() throws Exception {
			var externalId = UUID.randomUUID();
			var dto = buildVehicleManDto(UUID.randomUUID());

			when(service.update(eq(externalId), any(Vehicle.class), eq(dto.ownerId()))).thenThrow(
				new IllegalArgumentException("duplicate")
			);

			mockMvc
				.perform(
					put("/api/vehicles/{id}", externalId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
		}
	}

	@Nested
	class Search {

		@Test
		void shouldSearchWithPageable() throws Exception {
			var vehicle = buildVehicle();
			ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
			Page<Vehicle> page = new PageImpl<>(List.of(vehicle), PageRequest.of(0, 2), 1);

			when(
				service.search(
					eq(vehicle.getLicensePlate()),
					eq(vehicle.getBrand()),
					eq(vehicle.getModel()),
					eq(vehicle.getOwner().getExternalId()),
					any(Pageable.class)
				)
			).thenReturn(page);

			mockMvc
				.perform(
					get("/api/vehicles/search")
						.param("licensePlate", vehicle.getLicensePlate())
						.param("brand", vehicle.getBrand())
						.param("model", vehicle.getModel())
						.param("ownerId", vehicle.getOwner().getExternalId().toString())
						.param("page", "0")
						.param("size", "10")
						.param("sort", "brand,asc")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.content[0].licensePlate").value(vehicle.getLicensePlate()))
				.andExpect(jsonPath("$.data.content[0].ownerName").value(vehicle.getOwner().getName()));

			verify(service).search(
				eq(vehicle.getLicensePlate()),
				eq(vehicle.getBrand()),
				eq(vehicle.getModel()),
				eq(vehicle.getOwner().getExternalId()),
				captor.capture()
			);

			Pageable pageable = captor.getValue();
			assertThat(pageable.getPageNumber()).isZero();
			assertThat(pageable.getPageSize()).isEqualTo(10);
			assertThat(pageable.getSort().getOrderFor("brand")).isNotNull();
			assertThat(pageable.getSort().getOrderFor("brand").isAscending()).isTrue();
		}

		@Test
		void shouldSearchWithDefaultPageable() throws Exception {
			var vehicle = buildVehicle();
			ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
			Page<Vehicle> page = new PageImpl<>(List.of(vehicle), PageRequest.of(0, 2), 1);

			when(service.search(eq(vehicle.getLicensePlate()), any(), any(), any(), any(Pageable.class))).thenReturn(
				page
			);

			mockMvc
				.perform(get("/api/vehicles/search").param("licensePlate", vehicle.getLicensePlate()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.content[0].licensePlate").value(vehicle.getLicensePlate()));

			verify(service).search(eq(vehicle.getLicensePlate()), eq(null), eq(null), eq(null), captor.capture());

			Pageable pageable = captor.getValue();
			assertThat(pageable.getPageNumber()).isZero();
			assertThat(pageable.getPageSize()).isEqualTo(10);
			assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
			assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
		}
	}

	private Vehicle buildVehicle() {
		User owner = new User();
		owner.setId(1L);
		owner.setExternalId(UUID.randomUUID());
		owner.setDocument("57096255079");
		owner.setName("Cliente Teste");
		owner.setEmail("cliente@test.com");
		owner.setRole(UserRoleEnum.CUSTOMER.name());
		owner.setActive(true);
		owner.setPassword("Secret@123");
		owner.setPhone("11999990000");

		Vehicle vehicle = new Vehicle();
		vehicle.setId(1L);
		vehicle.setExternalId(UUID.randomUUID());
		vehicle.setLicensePlate("ABC1D23");
		vehicle.setBrand("Ford");
		vehicle.setModel("Ka");
		vehicle.setYear(2022);
		vehicle.setColor("Prata");
		vehicle.setOwner(owner);
		return vehicle;
	}

	private VehicleManDto buildVehicleManDto(UUID ownerId) {
		return new VehicleManDto("ABC1D23", "Ford", "Ka", 2022, "Prata", ownerId);
	}
}
