package org.project.mechanic_shop.presentation.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.domain.dto.internal_dto.CustomerStatusDto;
import org.project.mechanic_shop.domain.enums.UserRoleEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Hidden
@RestController
@RequestMapping("/internal/customers")
@RequiredArgsConstructor
public class InternalCustomerController {

	private final UserRepositoryPort userRepositoryPort;

	@Value("${internal.api.secret}")
	private String internalSecret;

	@GetMapping("/{document}/status")
	public CustomerStatusDto status(
		@PathVariable String document,
		@RequestHeader(value = "X-Internal-Secret", required = false, defaultValue = "") String secret
	) {
		if (!internalSecret.equals(secret)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal secret.");
		}

		return userRepositoryPort
			.findByDocument(document)
			.filter(user -> UserRoleEnum.CUSTOMER.name().equals(user.getRole()))
			.map(user -> new CustomerStatusDto(true, Boolean.TRUE.equals(user.getActive()), user.getEmail()))
			.orElse(new CustomerStatusDto(false, false, null));
	}
}
