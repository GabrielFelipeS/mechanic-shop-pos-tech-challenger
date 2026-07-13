package org.project.mechanic_shop.shared.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth0.jwt.JWT;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TokenServiceTest {

	private TokenService tokenService;

	@BeforeEach
	void setup() {
		tokenService = new TokenService();
		ReflectionTestUtils.setField(tokenService, "secret", "test-secret");
	}

	@Test
	void shouldReturnSubjectForAValidToken() {
		String token = tokenService.generateToken("user@test.com");

		assertThat(tokenService.validateToken(token)).isEqualTo("user@test.com");
	}

	@Test
	void shouldReturnNullForAnInvalidToken() {
		assertThat(tokenService.validateToken("not-a-real-token")).isNull();
	}

	@Test
	void shouldExpireApproximatelyTwoHoursFromNowRegardlessOfServerTimeZone() {
		String token = tokenService.generateToken("user@test.com");
		Instant expiresAt = JWT.decode(token).getExpiresAtAsInstant();

		Duration lifetime = Duration.between(Instant.now(), expiresAt);

		assertThat(lifetime.toMinutes()).isBetween(119L, 121L);
	}
}
