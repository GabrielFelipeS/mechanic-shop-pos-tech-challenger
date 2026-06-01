package org.project.mechanic_shop.domain.dto.service_order_dto;

import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.dto.service_order_dto.budget_dto.BudgetDto;
import org.project.mechanic_shop.domain.dto.user_dto.UserShortDto;
import org.project.mechanic_shop.domain.dto.vehicle_dto.VehicleShortDto;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link ServiceOrder}
 */
public record ServiceOrderDto(
	UUID externalId,
	LocalDateTime createdAt,
	String createdFor,
	LocalDateTime lastUpdatedAt,
	String lastUpdatedFor,
	ServiceOrderStatusEnum status,
	String customerComplaint,
	String mechanicDiagnosis,
	Integer odometerReading,
	LocalDateTime approvalDate,
	LocalDateTime estimatedCompletionDate,
	Long estimatedCompletionDays,
	LocalDateTime actualCompletionDate,
	Long actualCompletionDays,
	VehicleShortDto vehicle,
	UserShortDto responsibleMechanic,
	List<ServiceOrderStockItemDto> stockItems,
	List<ServiceOrderLaborDto> labors,
	BudgetDto budget
) implements Serializable {}
