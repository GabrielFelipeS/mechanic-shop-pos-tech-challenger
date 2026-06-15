package org.project.mechanic_shop.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderCreateDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderMetricsDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderQuoteDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderStatusDto;
import org.project.mechanic_shop.domain.dto.responses.ApiResponse;
import org.project.mechanic_shop.domain.dto.service_order_dto.budget_dto.BudgetResponseDto;
import org.project.mechanic_shop.application.mappers.ServiceOrderMapper;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.application.services.ServiceOrderService;
import org.project.mechanic_shop.application.services.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-orders")
@RequiredArgsConstructor
@Tag(name = "Service Orders")
@Slf4j
public class ServiceOrderController {

	private final ServiceOrderService service;
	private final ServiceOrderMapper mapper;

	private static final String SUCCESS_MESSAGE = "success";
	private final UserService userService;

	@Operation(
		summary = "Process budget approval via email link",
		description = "Public endpoint — no authentication required. Called when the customer clicks the approve/reject link in the notification email. Token is single-use."
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Response processed")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token invalid or already used")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order is no longer pending approval")
	@GetMapping(value = "/budget-approval", produces = "text/html;charset=UTF-8")
	public ResponseEntity<String> processBudgetApprovalByEmail(
		@RequestParam String token,
		@RequestParam boolean approved
	) {
		log.info("Email budget approval link clicked. Approved: {}", approved);

		service.processBudgetResponseByToken(token, approved);

		String title = approved ? "Orçamento Aprovado!" : "Orçamento Recusado";
		String icon  = approved ? "✅" : "❌";
		String msg   = approved
			? "Obrigado! O serviço foi aprovado e nossa equipe já iniciará os reparos."
			: "Entendemos. A ordem de serviço foi cancelada. Por favor, providencie a retirada do seu veículo.";

		String html = "<html><body style='font-family:sans-serif;text-align:center;padding:60px'>" +
			"<h1>" + icon + " " + title + "</h1>" +
			"<p style='font-size:18px'>" + msg + "</p>" +
			"</body></html>";

		MediaType htmlUtf8 = new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);
		return ResponseEntity.ok().contentType(htmlUtf8).body(html);
	}

	@Operation(summary = "List active service orders", description = "Returns a paginated list of active orders (excludes COMPLETED, DELIVERED and CANCELED). Fixed sort: IN_PROGRESS → PENDING_APPROVAL → DIAGNOSIS → RECEIVED, oldest first within each group.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated result")
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC')")
	public ResponseEntity<ApiResponse> listActive(
		@ParameterObject @PageableDefault(size = 10) Pageable pageable
	) {
		log.info("Listing active service orders");

		Page<ServiceOrder> orders = service.listActiveOrders(pageable);
		var dto = orders.map(mapper::toShortDto);

		return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@Operation(summary = "Service order metrics", description = "Returns the average actual completion days across all finished service orders. Returns null when no orders have been completed yet.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Metrics returned")
	@GetMapping("/metrics")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC')")
	public ResponseEntity<ApiResponse> getMetrics() {
		log.info("Fetching service order metrics");
		ServiceOrderMetricsDto dto = service.getMetrics();
		return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@Operation(summary = "Find service order by ID")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service order found")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service order not found")
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC', 'CUSTOMER')")
	public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
		log.info("Find service order by External ID: {}", id);

		var serviceOrder = service.findByExternalId(id);
		var dto = mapper.toDto(serviceOrder);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@Operation(summary = "Get current status of a service order", description = "Lightweight endpoint returning only the status and deadline fields — no labors, parts or full details.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status returned")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service order not found")
	@GetMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC', 'CUSTOMER')")
	public ResponseEntity<ApiResponse> getStatus(@PathVariable UUID id) {
		log.info("Get status for service order: {}", id);

		ServiceOrderStatusDto dto = mapper.toStatusDto(service.findByExternalId(id));

		return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@Operation(summary = "Open a new service order", description = "Creates a service order with RECEIVED status for the given vehicle. Optionally assigns a mechanic.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Service order created — returns the new order's external ID")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle or mechanic not found")
	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
	public ResponseEntity<ApiResponse> create(@RequestBody @Valid ServiceOrderCreateDto dto) {
		log.info("Try create service order with parameters: {}", dto);

		var serviceOrderCreated = service.createServiceOrder(dto);

		return ResponseEntity.status(HttpStatus.CREATED).body(
			new ApiResponse(HttpStatus.CREATED.value(), SUCCESS_MESSAGE, serviceOrderCreated.getExternalId())
		);
	}

	@Operation(summary = "Update diagnosis and quote", description = "Records the mechanic's diagnosis and fills in the labors and parts for the budget. Transitions the order from RECEIVED to DIAGNOSIS on first update.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quote updated")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or order in a non-editable state")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service order, mechanic service or stock item not found")
	@PutMapping("/{id}/quote")
	@PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
	public ResponseEntity<ApiResponse> updateQuote(
		@PathVariable UUID id,
		@RequestBody @Valid ServiceOrderQuoteDto dto
	) {
		log.info("Try update quote for service order {} with parameters: {}", id, dto);

		var updatedServiceOrder = service.updateQuote(id, dto);
		var soDto = mapper.toDto(updatedServiceOrder);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, soDto));
	}

	@Operation(summary = "Send budget for customer approval", description = "Closes the diagnosis phase and moves the order to PENDING_APPROVAL. Calculates the estimated completion deadline.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval requested — order is now PENDING_APPROVAL")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order is not in DIAGNOSIS status")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service order not found")
	@PostMapping("/{id}/request-approval")
	@PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN')")
	public ResponseEntity<ApiResponse> requestApproval(@PathVariable UUID id) {
		log.info("Action triggered: Requesting customer approval for OS {}", id);

		var order = service.requestCustomerApproval(id);
		var dto = mapper.toDto(order);

		return ResponseEntity.ok().body(
			new ApiResponse(
				HttpStatus.OK.value(),
				"Approval requested successfully. Status updated to PENDING_APPROVAL.",
				dto
			)
		);
	}

	@Operation(summary = "Process customer budget response", description = "If approved, withdraws stock and moves the order to IN_PROGRESS. If rejected, cancels the order.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Budget response processed")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order is not in PENDING_APPROVAL status")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service order not found")
	@PostMapping("/{id}/budget-response")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
	public ResponseEntity<ApiResponse> processBudgetResponse(
		@PathVariable UUID id,
		@Valid @RequestBody BudgetResponseDto responseDto
	) {
		log.info("Action triggered: Processing budget response for OS {}. Approved: {}", id, responseDto.approved());

		ServiceOrder order = service.processBudgetResponse(id, responseDto.approved());
		ServiceOrderDto dto = mapper.toDto(order);

		String message = responseDto.approved()
			? "Budget approved successfully! Stock withdrawn and OS moved to IN_PROGRESS."
			: "Budget rejected. OS has been CANCELED.";

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), message, dto));
	}

	@Operation(summary = "Finish the service", description = "Marks the service as COMPLETED and records the actual finish date. Order must be IN_PROGRESS.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service finished — order is now COMPLETED")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order is not IN_PROGRESS")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service order not found")
	@PostMapping("/{id}/finish")
	@PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN')")
	public ResponseEntity<ApiResponse> finishService(@PathVariable UUID id) {
		log.info("Action triggered: Mechanic finishing service for OS {}", id);

		ServiceOrder order = service.finishService(id);
		ServiceOrderDto dto = mapper.toDto(order);

		return ResponseEntity.ok().body(
			new ApiResponse(
				HttpStatus.OK.value(),
				"Service finished successfully! OS moved to COMPLETED and customer notified.",
				dto
			)
		);
	}

	@Operation(summary = "Deliver vehicle to customer", description = "Closes the service order lifecycle by moving it to DELIVERED. Order must be COMPLETED.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle delivered — order is now DELIVERED")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order is not COMPLETED")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service order not found")
	@PostMapping("/{id}/deliver")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
	public ResponseEntity<ApiResponse> deliverVehicle(@PathVariable UUID id) {
		log.info("Action triggered: Receptionist delivering vehicle for OS {}", id);

		ServiceOrder order = service.deliverVehicle(id);
		ServiceOrderDto dto = mapper.toDto(order);

		return ResponseEntity.ok().body(
			new ApiResponse(
				HttpStatus.OK.value(),
				"Vehicle delivered successfully! OS lifecycle is now closed (DELIVERED).",
				dto
			)
		);
	}

	@Operation(summary = "Search service orders", description = "Returns a paginated list of service orders filtered by license plate, status or responsible mechanic.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated result")
	@GetMapping("/search")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MECHANIC', 'SALESPERSON', 'CUSTOMER')")
	public ResponseEntity<ApiResponse> search(
		@RequestParam(name = "licensePlate", required = false) String licensePlate,
		@RequestParam(name = "status", required = false) ServiceOrderStatusEnum status,
		@RequestParam(name = "mechanicId", required = false) UUID mechanicId,
		@ParameterObject @PageableDefault(
			size = 10,
			sort = "createdAt",
			direction = Sort.Direction.DESC
		) Pageable pageable
	) {
		log.info("Search service orders with filters");

		User mechanic = null;
		if (mechanicId != null) mechanic = userService.findByExternalId(mechanicId);

		Page<ServiceOrder> serviceOrders = service.search(licensePlate, status, pageable, mechanic);
		var listDto = serviceOrders.map(mapper::toShortDto);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, listDto));
	}
}
