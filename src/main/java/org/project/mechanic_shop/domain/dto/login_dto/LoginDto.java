package org.project.mechanic_shop.domain.dto.login_dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credentials for authentication")
public record LoginDto(
	@Schema(description = "Registered e-mail address", example = "admin@shop.com")
	String email,

	@Schema(description = "User password", example = "123456")
	String password
) {}
