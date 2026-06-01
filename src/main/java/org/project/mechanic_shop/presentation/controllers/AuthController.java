package org.project.mechanic_shop.presentation.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.domain.dto.responses.ApiResponse;
import org.project.mechanic_shop.shared.config.security.TokenService;
import org.project.mechanic_shop.shared.config.security.UserPrincipal;
import org.project.mechanic_shop.domain.dto.login_dto.LoginDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth login")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final TokenService tokenService;

	String successMessage = "success";

	@PostMapping("/login")
	public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginDto data) {
		var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
		var auth = this.authenticationManager.authenticate(usernamePassword);

		var userPrincipal = (UserPrincipal) auth.getPrincipal();
		assert userPrincipal != null;
		var token = tokenService.generateToken(userPrincipal.getUsername());

		return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), successMessage, token));
	}
}
