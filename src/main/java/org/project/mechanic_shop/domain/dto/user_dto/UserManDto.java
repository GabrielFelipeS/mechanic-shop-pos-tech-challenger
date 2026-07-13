package org.project.mechanic_shop.domain.dto.user_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.project.mechanic_shop.domain.validation.CpfOrCnpj;
import org.project.mechanic_shop.domain.enums.UserRoleEnum;

import java.io.Serializable;

@Schema(description = "Payload for creating or updating a user")
public record UserManDto(
	@Schema(description = "CPF (11 digits) or CNPJ (14 digits) without formatting", example = "12345678901")
	@NotBlank(message = "Document (CPF/CNPJ) is required.")
	@CpfOrCnpj
	@Size(min = 11, max = 14, message = "Document must be between 11 and 14 characters.")
	String document,

	@Schema(description = "Full name", example = "João da Silva")
	@NotBlank(message = "Name is required.")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
	String name,

	@Schema(description = "E-mail address", example = "joao@mechanic.com")
	@NotBlank(message = "Email is required.")
	@Email(message = "Invalid email format.")
	@Size(max = 100, message = "Email must not exceed 100 characters.")
	String email,

	@Schema(description = "User role", example = "MECHANIC")
	@NotNull(message = "Role is required") UserRoleEnum role,

	@Schema(description = "Whether the account is active", example = "true")
	@NotNull(message = "Activation status (active) is required.") Boolean active,

	@Schema(description = "Password (min 3 characters)", example = "secret123")
	@NotBlank(message = "Password is required.")
	@Size(min = 3, max = 50, message = "Password must be between 3 and 50 characters.")
	String password,

	@Schema(description = "Phone number with area code", example = "11987654321")
	@NotBlank(message = "Phone number is required.")
	@Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters.")
	String phone
) implements Serializable {


	private static final String PROTECTED = "[PROTECTED]" ;

	@Override
	public String toString() {
		return "UserManDto[document=" + document +
			", name=" + name +
			", email=" + email +
			", role=" + role +
			", active=" + active +
			", password= "+ PROTECTED +
			", phone=" + phone +
			"]";
	}
}
