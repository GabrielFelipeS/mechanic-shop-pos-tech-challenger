package org.project.mechanic_shop.dto.part_dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link org.project.mechanic_shop.models.Part}
 */
public record PartShortDto(
        UUID externalId,
        LocalDateTime createdAt,
        String createdFor,
        LocalDateTime lastUpdatedAt,
        String lastUpdatedFor,
        String code,
        String name,
        Integer quantity,
        BigDecimal salePrice
) implements Serializable {
}