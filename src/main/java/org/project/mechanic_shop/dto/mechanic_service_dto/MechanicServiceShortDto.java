package org.project.mechanic_shop.dto.mechanic_service_dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record MechanicServiceShortDto(
        UUID externalId, String name, Integer estimatedTimeMinutes, BigDecimal price
) implements Serializable {}