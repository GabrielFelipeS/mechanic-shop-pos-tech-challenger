package org.project.mechanic_shop.config;

import org.mockito.Mockito;
import org.project.mechanic_shop.config.security.AuthorizationService;
import org.project.mechanic_shop.config.security.SecurityFilter;
import org.project.mechanic_shop.config.security.TokenService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SecurityConfig {
    @Bean
    TokenService tokenService() {
        return Mockito.mock(TokenService.class);
    }

    @Bean
    AuthorizationService authorizationService() {
        return Mockito.mock(AuthorizationService.class);
    }

    @Bean
    SecurityFilter securityFilter() {
        return Mockito.mock(SecurityFilter.class);
    }
}
