package org.project.mechanic_shop.config;

import org.project.mechanic_shop.application.mappers.UserMapper;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.application.services.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

	@Bean
	UserService userService() {
		return mock(UserService.class);
	}

	@Bean
	UserMapper userMapper() {
		return mock(UserMapper.class);
	}

	@Bean
	UserRepositoryPort userRepository() {
		return mock(UserRepositoryPort.class);
	}
}
