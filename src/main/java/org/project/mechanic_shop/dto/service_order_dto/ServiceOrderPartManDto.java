package org.project.mechanic_shop.dto.service_order_dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ServiceOrderPartManDto(
        @NotNull UUID partExternalId,
        @NotNull @Min(1) Integer quantity
) {}
