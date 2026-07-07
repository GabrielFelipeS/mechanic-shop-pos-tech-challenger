package org.project.mechanic_shop.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.dto.responses.ApiResponse;
import org.project.mechanic_shop.domain.dto.stock_item_dto.StockItemDto;
import org.project.mechanic_shop.domain.dto.stock_item_dto.StockItemManDto;
import org.project.mechanic_shop.application.mappers.StockItemMapper;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.project.mechanic_shop.application.services.StockItemService;
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
@RequestMapping("/api/stock-items")
@RequiredArgsConstructor
@Tag(name = "Stock Item Management")
@Slf4j
public class StockItemController {

	private final StockItemService service;
	private final StockItemMapper mapper;

	private static final String SUCCESS_MESSAGE = "success";

	@Operation(summary = "Find stock item by ID")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock item found")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Stock item not found")
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('WAREHOUSE_CLERK', 'ADMIN', 'MECHANIC', 'RECEPTIONIST')")
	public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
		log.info("Find part by External ID: {}", id);

		var part = service.findByExternalId(id);
		var dto = mapper.toDto(part);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@Operation(summary = "Create a new stock item")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Stock item created — returns the new item's external ID")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Item code already registered")
	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('WAREHOUSE_CLERK', 'ADMIN', 'MECHANIC')")
	public ResponseEntity<ApiResponse> create(@RequestBody @Valid StockItemManDto dto) {
		log.info("Try create part with parameters: {}", dto);

		StockItem stockItem = mapper.toEntity(dto);
		var partCreated = service.create(stockItem);

		return ResponseEntity.status(HttpStatus.CREATED).body(
			new ApiResponse(HttpStatus.CREATED.value(), SUCCESS_MESSAGE, partCreated.getExternalId())
		);
	}

	@Operation(summary = "Update stock item data")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock item updated")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Stock item not found")
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('WAREHOUSE_CLERK', 'ADMIN')")
	public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody @Valid StockItemManDto dto) {
		log.info("Try update part {} with parameters: {}", id, dto);

		var stockItemToUpdate = mapper.toEntity(dto);
		var updatedStockItem = service.update(id, stockItemToUpdate);
		StockItemDto partDto = mapper.toDto(updatedStockItem);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, partDto));
	}

	@Operation(summary = "Search stock items", description = "Returns a paginated list of stock items filtered by code or name.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated result")
	@GetMapping("/search")
	@PreAuthorize("hasAnyRole('WAREHOUSE_CLERK', 'ADMIN', 'MECHANIC', 'RECEPTIONIST')")
	public ResponseEntity<ApiResponse> search(
		@RequestParam(name = "code", required = false) String code,
		@RequestParam(name = "name", required = false) String name,
		@ParameterObject @PageableDefault(
			size = 10,
			sort = "createdAt",
			direction = Sort.Direction.DESC
		) Pageable pageable
	) {
		log.info("Search parts with filters");

		Page<StockItem> parts = service.search(code, name, pageable);
		var listDto = parts.map(mapper::toShortDto);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, listDto));
	}
}
