package org.project.mechanic_shop.dto.mechanic_service_dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link org.project.mechanic_shop.models.User}
 */
public record MechanicServiceManDto(

        @NotBlank(message = "Name is required.")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
        @Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
        String name,

        @Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
        String description,

        @NotNull(message = "Estimated time is required.")
        @Min(value = 1, message = "Estimated time must be at least 1 minute.")
        Integer estimatedTimeMinutes,

        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative.")
        BigDecimal price

) implements Serializable {
}