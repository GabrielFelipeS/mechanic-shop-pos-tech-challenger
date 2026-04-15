package org.project.mechanic_shop.dto.user_dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link org.project.mechanic_shop.models.User
 *}
 */
public record UserShortDto(

        UUID externalId,
        LocalDateTime createdAt,
        String createdFor,
        LocalDateTime lastUpdatedAt,
        String lastUpdatedFor,
        String document,
        String name,
        String email,
        Boolean active,
        String phone

) implements Serializable {

}