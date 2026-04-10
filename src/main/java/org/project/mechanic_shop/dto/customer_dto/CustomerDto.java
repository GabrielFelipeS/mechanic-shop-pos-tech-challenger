package org.project.mechanic_shop.dto.customer_dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link org.project.mechanic_shop.models.Customer}
 */
public record CustomerDto(

        UUID externalId,
        LocalDateTime createdAt,
        String createdFor,
        LocalDateTime lastUpdatedAt,
        String document,
        String name,
        String email,
        Boolean active,
        String phone

) implements Serializable {

}