package org.project.mechanic_shop.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.common.responses.ApiResponse;
import org.project.mechanic_shop.dto.vehicle_dto.VehicleDto;
import org.project.mechanic_shop.dto.vehicle_dto.VehicleManDto;
import org.project.mechanic_shop.mappers.VehicleMapper;
import org.project.mechanic_shop.models.Vehicle;
import org.project.mechanic_shop.services.VehicleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management")
@Slf4j
public class VehicleController {

    private final VehicleService service;
    private final VehicleMapper mapper;

    private static final String SUCCESS_MESSAGE = "success";

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'MECHANIC')")
    public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
        log.info("Find vehicle by External ID: {}", id);

        var vehicle = service.findByExternalId(id);
        var dto = mapper.toDto(vehicle);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                dto
        ));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<ApiResponse> create(@RequestBody @Valid VehicleManDto dto) {
        log.info("Try create vehicle with parameters: {}", dto);

        Vehicle vehicle = mapper.toEntity(dto);

        var vehicleCreated = service.create(vehicle, dto.ownerId());

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                HttpStatus.CREATED.value(),
                SUCCESS_MESSAGE,
                vehicleCreated.getExternalId()
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<ApiResponse> update(@PathVariable UUID id,
                                              @RequestBody @Valid VehicleManDto dto) {
        log.info("Try update vehicle {} with parameters: {}", id, dto);

        var vehicleToUpdate = mapper.toEntity(dto);

        var updatedVehicle = service.update(id, vehicleToUpdate, dto.ownerId());

        VehicleDto vehicleDto = mapper.toDto(updatedVehicle);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                vehicleDto
        ));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN', 'MECHANIC')")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(name = "licensePlate", required = false) String licensePlate,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "model", required = false) String model,
            @RequestParam(name = "ownerId", required = false) UUID ownerId, // <--- Novo parâmetro
            @ParameterObject @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Search vehicles with filters");

        Page<Vehicle> vehicles = service.search(licensePlate, brand, model, ownerId, pageable);

        var listDto = vehicles.map(mapper::toShortDto);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                listDto
        ));
    }
}