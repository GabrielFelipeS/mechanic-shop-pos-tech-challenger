package org.project.mechanic_shop.domain.dto.service_order_dto;

import jakarta.validation.constraints.NotNull;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;

public record ServiceOrderStatusUpdateDto(@NotNull ServiceOrderStatusEnum status) {}
