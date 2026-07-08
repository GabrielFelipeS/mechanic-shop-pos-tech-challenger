package org.project.mechanic_shop.config;

import org.project.mechanic_shop.shared.config.security.AuthorizationService;
import org.project.mechanic_shop.shared.config.security.SecurityFilter;
import org.project.mechanic_shop.shared.config.security.TokenService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class SecurityConfig {

	@Bean
	TokenService tokenService() {
		return mock(TokenService.class);
	}

	@Bean
	AuthorizationService authorizationService() {
		return mock(AuthorizationService.class);
	}

	@Bean
	SecurityFilter securityFilter() {
		return mock(SecurityFilter.class);
	}
}
