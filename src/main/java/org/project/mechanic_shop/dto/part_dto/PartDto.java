package org.project.mechanic_shop.dto.part_dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * DTO for {@link org.project.mechanic_shop.models.Part}
 */
public record PartDto(
        UUID externalId,
        LocalDateTime createdAt,
        String createdFor,
        LocalDateTime lastUpdatedAt,
        String lastUpdatedFor,
        String code,
        String name,
        String description,
        Integer quantity,
        BigDecimal costPrice,
        BigDecimal salePrice
) implements Serializable {
}