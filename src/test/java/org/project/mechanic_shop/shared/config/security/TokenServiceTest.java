package org.project.mechanic_shop.shared.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth0.jwt.JWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
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
	void shouldExpireExactlyTwoHoursAfterGeneration() {
		Instant fixedNow = Instant.parse("2024-01-01T10:00:00Z");
		TokenService fixedClockTokenService = new TokenService(Clock.fixed(fixedNow, ZoneOffset.UTC));
		ReflectionTestUtils.setField(fixedClockTokenService, "secret", "test-secret");

		String token = fixedClockTokenService.generateToken("user@test.com");
		Instant expiresAt = JWT.decode(token).getExpiresAtAsInstant();

		assertThat(expiresAt).isEqualTo(fixedNow.plus(Duration.ofHours(2)));
	}
}
