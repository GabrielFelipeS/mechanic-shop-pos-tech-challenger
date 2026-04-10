package org.project.postechchallengemechanicshop.dto.customerDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link org.project.postechchallengemechanicshop.models.Customer}
 */
public record CustomerShortDto(

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