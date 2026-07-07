package org.project.mechanic_shop.config;

import org.mockito.Mockito;
import org.project.mechanic_shop.application.mappers.UserMapper;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.application.services.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

	@Bean
	UserService userService() {
		return Mockito.mock(UserService.class);
	}

	@Bean
	UserMapper userMapper() {
		return Mockito.mock(UserMapper.class);
	}

	@Bean
	UserRepositoryPort userRepository() {
		return Mockito.mock(UserRepositoryPort.class);
	}
}
