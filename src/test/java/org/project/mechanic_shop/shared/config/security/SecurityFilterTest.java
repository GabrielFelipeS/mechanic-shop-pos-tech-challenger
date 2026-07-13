package org.project.mechanic_shop.shared.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

class SecurityFilterTest {

	private final TokenService tokenService = mock(TokenService.class);
	private final AuthorizationService authorizationService = mock(AuthorizationService.class);
	private final SecurityFilter filter = new SecurityFilter(tokenService, authorizationService);

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldAuthenticateWhenTokenIsValidAndUserIsEnabled() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain filterChain = mock(FilterChain.class);
		UserDetails user = buildUserDetails(true);

		when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
		when(tokenService.validateToken("valid-token")).thenReturn("user@test.com");
		when(authorizationService.loadUserByUsername("user@test.com")).thenReturn(user);

		filter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
	}

	@Test
	void shouldNotAuthenticateWhenUserIsDisabled() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain filterChain = mock(FilterChain.class);
		UserDetails user = buildUserDetails(false);

		when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
		when(tokenService.validateToken("valid-token")).thenReturn("user@test.com");
		when(authorizationService.loadUserByUsername("user@test.com")).thenReturn(user);

		filter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain filterChain = mock(FilterChain.class);

		when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
		when(tokenService.validateToken("invalid-token")).thenReturn(null);

		filter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	private UserDetails buildUserDetails(boolean enabled) {
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
		return new org.springframework.security.core.userdetails.User(
			"user@test.com",
			"encoded-password",
			enabled,
			true,
			true,
			true,
			authorities
		);
	}
}
