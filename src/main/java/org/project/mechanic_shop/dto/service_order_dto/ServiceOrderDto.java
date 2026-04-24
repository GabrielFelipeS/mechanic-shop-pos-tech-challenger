package org.project.mechanic_shop.dto.service_order_dto;

import org.project.mechanic_shop.dto.service_order_dto.budget_dto.BudgetDto;
import org.project.mechanic_shop.dto.user_dto.UserShortDto;
import org.project.mechanic_shop.dto.vehicle_dto.VehicleShortDto;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link org.project.mechanic_shop.models.ServiceOrder}
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
