package org.project.mechanic_shop.dto.service_order_dto;

import jakarta.validation.constraints.NotNull;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;

public record ServiceOrderStatusUpdateDto(@NotNull ServiceOrderStatusEnum status) {}
