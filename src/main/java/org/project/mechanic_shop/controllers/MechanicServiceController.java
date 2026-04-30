package org.project.mechanic_shop.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.common.responses.ApiResponse;
import org.project.mechanic_shop.dto.mechanic_service_dto.MechanicServiceDto;
import org.project.mechanic_shop.dto.mechanic_service_dto.MechanicServiceManDto;
import org.project.mechanic_shop.mappers.MechanicServiceMapper;
import org.project.mechanic_shop.models.MechanicService;
import org.project.mechanic_shop.services.MechanicServiceService;
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
@RequestMapping("/api/mechanic-services")
@RequiredArgsConstructor
@Tag(name = "Mechanic Service Catalog")
@Slf4j
public class MechanicServiceController {

	private final MechanicServiceService service;
	private final MechanicServiceMapper mapper;

	private static final String SUCCESS_MESSAGE = "success";

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC')")
	public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
		log.info("Find mechanic service by External ID: {}", id);

		var mechanicService = service.findByExternalId(id);
		var dto = mapper.toDto(mechanicService);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
	public ResponseEntity<ApiResponse> create(@RequestBody @Valid MechanicServiceManDto dto) {
		log.info("Try create mechanic service with parameters: {}", dto);

		MechanicService mechanicService = mapper.toEntity(dto);

		var mechanicServiceCreated = service.create(mechanicService);

		return ResponseEntity.status(HttpStatus.CREATED).body(
			new ApiResponse(HttpStatus.CREATED.value(), SUCCESS_MESSAGE, mechanicServiceCreated.getExternalId())
		);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
	public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody @Valid MechanicServiceManDto dto) {
		log.info("Try update mechanic service {} with parameters: {}", id, dto);

		var mechanicServiceToUpdate = mapper.toEntity(dto);

		var updatedMechanicService = service.update(id, mechanicServiceToUpdate);

		MechanicServiceDto mechanicServiceDto = mapper.toDto(updatedMechanicService);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, mechanicServiceDto));
	}

	@GetMapping("/search")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC')")
	public ResponseEntity<ApiResponse> search(
		@RequestParam(name = "name", required = false) String name,
		@ParameterObject @PageableDefault(
			size = 10,
			sort = "createdAt",
			direction = Sort.Direction.DESC
		) Pageable pageable
	) {
		log.info("Search mechanic services with filters");

		Page<MechanicService> mechanicServices = service.search(name, pageable);

		var listDto = mechanicServices.map(mapper::toShortDto);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, listDto));
	}
}
