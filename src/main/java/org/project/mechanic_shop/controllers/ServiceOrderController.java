package org.project.mechanic_shop.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.common.responses.ApiResponse;
import org.project.mechanic_shop.dto.service_order_dto.*;
import org.project.mechanic_shop.dto.service_order_dto.budget_dto.BudgetResponseDto;
import org.project.mechanic_shop.mappers.ServiceOrderMapper;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.services.ServiceOrderService;
import org.project.mechanic_shop.services.UserService;
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
    private final UserService userService;

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


    @PostMapping("/{id}/request-approval")
    @PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN')")
    public ResponseEntity<ApiResponse> requestApproval(@PathVariable UUID id) {
        log.info("Action triggered: Requesting customer approval for OS {}", id);

        var order = service.requestCustomerApproval(id);
        var dto = mapper.toDto(order);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                "Approval requested successfully. Status updated to PENDING_APPROVAL.",
                dto
        ));
    }

    @PostMapping("/{id}/budget-response")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse> processBudgetResponse(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetResponseDto responseDto) {

        log.info("Action triggered: Processing budget response for OS {}. Approved: {}", id, responseDto.approved());

        ServiceOrder order = service.processBudgetResponse(id, responseDto.approved());
        ServiceOrderDto dto = mapper.toDto(order);

        String message = responseDto.approved() ?
                "Budget approved successfully! Stock withdrawn and OS moved to IN_PROGRESS." :
                "Budget rejected. OS has been CANCELED.";

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                message,
                dto
        ));
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN')")
    public ResponseEntity<ApiResponse> finishService(@PathVariable UUID id) {
        log.info("Action triggered: Mechanic finishing service for OS {}", id);

        ServiceOrder order = service.finishService(id);
        ServiceOrderDto dto = mapper.toDto(order);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                "Service finished successfully! OS moved to COMPLETED and customer notified.",
                dto
        ));
    }

    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<ApiResponse> deliverVehicle(@PathVariable UUID id) {
        log.info("Action triggered: Receptionist delivering vehicle for OS {}", id);

        ServiceOrder order = service.deliverVehicle(id);
        ServiceOrderDto dto = mapper.toDto(order);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                "Vehicle delivered successfully! OS lifecycle is now closed (DELIVERED).",
                dto
        ));
    }


    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC', 'SALESPERSON')")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(name = "licensePlate", required = false) String licensePlate,
            @RequestParam(name = "status", required = false) ServiceOrderStatusEnum status,
            @RequestParam(name = "mechanicId", required = false) UUID mechanicId,
            @ParameterObject @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Search service orders with filters");

        User mechanic =  null;
        if (mechanicId != null ) mechanic = userService.findByExternalId(mechanicId);

        Page<ServiceOrder> serviceOrders = service.search(licensePlate, status, pageable, mechanic);
        var listDto = serviceOrders.map(mapper::toShortDto);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                listDto
        ));
    }
}