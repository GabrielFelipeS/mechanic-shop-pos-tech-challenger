package org.project.mechanic_shop.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.dto.responses.ApiResponse;
import org.project.mechanic_shop.domain.dto.vehicle_dto.VehicleDto;
import org.project.mechanic_shop.domain.dto.vehicle_dto.VehicleManDto;
import org.project.mechanic_shop.application.mappers.VehicleMapper;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.application.services.VehicleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management")
@Slf4j
public class VehicleController {

	private final VehicleService service;
	private final VehicleMapper mapper;

	private static final String SUCCESS_MESSAGE = "success";

	@Operation(summary = "Find vehicle by ID")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle found")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'MECHANIC', 'CUSTOMER')")
	public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
		log.info("Find vehicle by External ID: {}", id);

		var vehicle = service.findByExternalId(id);
		var dto = mapper.toDto(vehicle);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@Operation(summary = "Register a new vehicle")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vehicle created — returns the new vehicle's external ID")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Owner (user) not found")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "License plate already registered")
	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
	public ResponseEntity<ApiResponse> create(@RequestBody @Valid VehicleManDto dto) {
		log.info("Try create vehicle with parameters: {}", dto);

		Vehicle vehicle = mapper.toEntity(dto);

		var vehicleCreated = service.create(vehicle, dto.ownerId());

		return ResponseEntity.status(HttpStatus.CREATED).body(
			new ApiResponse(HttpStatus.CREATED.value(), SUCCESS_MESSAGE, vehicleCreated.getExternalId())
		);
	}

	@Operation(summary = "Update vehicle data")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle updated")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle or new owner not found")
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
	public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody @Valid VehicleManDto dto) {
		log.info("Try update vehicle {} with parameters: {}", id, dto);

		var vehicleToUpdate = mapper.toEntity(dto);

		var updatedVehicle = service.update(id, vehicleToUpdate, dto.ownerId());

		VehicleDto vehicleDto = mapper.toDto(updatedVehicle);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, vehicleDto));
	}

	@Operation(summary = "Search vehicles", description = "Returns a paginated list of vehicles filtered by license plate, brand, model or owner.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated result")
	@GetMapping("/search")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'MECHANIC', 'CUSTOMER')")
	public ResponseEntity<ApiResponse> search(
		@RequestParam(name = "licensePlate", required = false) String licensePlate,
		@RequestParam(name = "brand", required = false) String brand,
		@RequestParam(name = "model", required = false) String model,
		@RequestParam(name = "ownerId", required = false) UUID ownerId,
		@ParameterObject @PageableDefault(
			size = 10,
			sort = "createdAt",
			direction = Sort.Direction.DESC
		) Pageable pageable
	) {
		log.info("Search vehicles with filters");

		Page<Vehicle> vehicles = service.search(licensePlate, brand, model, ownerId, pageable);

		var listDto = vehicles.map(mapper::toShortDto);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, listDto));
	}
}
