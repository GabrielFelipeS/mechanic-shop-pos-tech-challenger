package org.project.mechanic_shop.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.common.responses.ApiResponse;
import org.project.mechanic_shop.dto.stock_item_dto.StockItemDto;
import org.project.mechanic_shop.dto.stock_item_dto.StockItemManDto;
import org.project.mechanic_shop.mappers.StockItemMapper;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.services.StockItemService;
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

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('WAREHOUSE_CLERK', 'ADMIN', 'MECHANIC', 'RECEPTIONIST')")
	public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
		log.info("Find part by External ID: {}", id);

		var part = service.findByExternalId(id);
		var dto = mapper.toDto(part);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('WAREHOUSE_CLERK', 'ADMIN')")
	public ResponseEntity<ApiResponse> create(@RequestBody @Valid StockItemManDto dto) {
		log.info("Try create part with parameters: {}", dto);

		StockItem stockItem = mapper.toEntity(dto);
		var partCreated = service.create(stockItem);

		return ResponseEntity.status(HttpStatus.CREATED).body(
			new ApiResponse(HttpStatus.CREATED.value(), SUCCESS_MESSAGE, partCreated.getExternalId())
		);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('WAREHOUSE_CLERK', 'ADMIN')")
	public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody @Valid StockItemManDto dto) {
		log.info("Try update part {} with parameters: {}", id, dto);

		var stockItemToUpdate = mapper.toEntity(dto);
		var updatedStockItem = service.update(id, stockItemToUpdate);
		StockItemDto partDto = mapper.toDto(updatedStockItem);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, partDto));
	}

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
