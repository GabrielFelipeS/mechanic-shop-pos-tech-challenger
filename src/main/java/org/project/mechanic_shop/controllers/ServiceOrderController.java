package org.project.mechanic_shop.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.common.responses.ApiResponse;
import org.project.mechanic_shop.dto.service_order_dto.*;
import org.project.mechanic_shop.mappers.ServiceOrderMapper;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.services.ServiceOrderService;
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
@RequestMapping("/api/v1/service-orders") // Atualize para /api/service-orders se preferir sem o v1
@RequiredArgsConstructor
@Tag(name = "Service Orders")
@Slf4j
public class ServiceOrderController {

    private final ServiceOrderService service;
    private final ServiceOrderMapper mapper; // Lembre-se de criar este Mapper (MapStruct ou manual)

    private static final String SUCCESS_MESSAGE = "success";

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC', 'SALESPERSON')")
    public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
        log.info("Find service order by External ID: {}", id);

        var serviceOrder = service.findByExternalId(id);
        var dto = mapper.toDto(serviceOrder); // Retorna a OS completa com peças e serviços

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                dto
        ));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse> create(@RequestBody @Valid ServiceOrderCreateDto dto) {
        log.info("Try create service order with parameters: {}", dto);

        var serviceOrderCreated = service.createServiceOrder(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                HttpStatus.CREATED.value(),
                SUCCESS_MESSAGE,
                serviceOrderCreated.getExternalId()
        ));
    }

    @PutMapping("/{id}/quote")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
    public ResponseEntity<ApiResponse> updateQuote(
            @PathVariable UUID id,
            @RequestBody @Valid ServiceOrderQuoteDto dto) {

        log.info("Try update quote for service order {} with parameters: {}", id, dto);

        var updatedServiceOrder = service.updateQuote(id, dto);
        var shortDto = mapper.toShortDto(updatedServiceOrder);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                shortDto
        ));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody @Valid ServiceOrderStatusUpdateDto dto) {

        log.info("Try update status of service order {} to {}", id, dto.status());

        var updatedServiceOrder = service.updateStatus(id, dto.status());
        var shortDto = mapper.toShortDto(updatedServiceOrder);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                shortDto
        ));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC', 'SALESPERSON')")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(name = "licensePlate", required = false) String licensePlate,
            @RequestParam(name = "status", required = false) ServiceOrderStatusEnum status,
            @ParameterObject @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Search service orders with filters");

        Page<ServiceOrder> serviceOrders = service.search(licensePlate, status, pageable);
        var listDto = serviceOrders.map(mapper::toShortDto); // Retorna a lista resumida na paginação

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                listDto
        ));
    }
}