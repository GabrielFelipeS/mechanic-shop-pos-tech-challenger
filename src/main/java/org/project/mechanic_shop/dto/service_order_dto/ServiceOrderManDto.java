package org.project.mechanic_shop.dto.service_order_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ServiceOrderManDto(
        @NotNull UUID vehicleExternalId,
        @NotBlank String customerComplaint,
        Integer odometerReading,
        UUID mechanicExternalId,
        List<ServiceOrderPartManDto> parts,
        List<ServiceOrderLaborManDto> labors
) {}
