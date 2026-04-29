package org.project.mechanic_shop.dto.service_order_dto.budget_dto;

import jakarta.validation.constraints.NotNull;

public record BudgetResponseDto(@NotNull(message = "The 'approved' field cannot be null") Boolean approved) {}
