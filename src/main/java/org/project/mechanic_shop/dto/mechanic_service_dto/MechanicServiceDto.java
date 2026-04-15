package org.project.mechanic_shop.dto.mechanic_service_dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MechanicServiceDto(
        UUID externalId, LocalDateTime createdAt, String createdFor,
        LocalDateTime lastUpdatedAt, String lastUpdatedFor,
        String name, String description, Integer estimatedTimeMinutes, BigDecimal price
) implements Serializable {}