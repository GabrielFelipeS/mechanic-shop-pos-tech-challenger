package org.project.mechanic_shop.dto.user_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;
import org.project.mechanic_shop.models.enums.UserRoleEnum;

import java.io.Serializable;

/**
 * DTO for {@link org.project.mechanic_shop.models.User}
 */
public record UserManDto(

        @NotBlank(message = "Document (CPF/CNPJ) is required.")
        @CPF(message = "Invalid cpf format")
        @Size(min = 11, max = 14, message = "Document must be between 11 and 14 characters.")
        String document,

        @NotBlank(message = "Name is required.")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        @Size(max = 100, message = "Email must not exceed 100 characters.")
        String email,

        @NotNull(message = "Role is required")
        UserRoleEnum role,

        @NotNull(message = "Activation status (active) is required.")
        Boolean active,

        @NotBlank(message = "Password is required.")
        @Size(min = 3, max = 50, message = "Password must be between 3 and 50 characters.")
        String password,

        @NotBlank(message = "Phone number is required.")
        @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters.")
        String phone

) implements Serializable {
}