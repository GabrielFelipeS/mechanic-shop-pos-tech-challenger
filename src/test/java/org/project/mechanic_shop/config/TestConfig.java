package org.project.mechanic_shop.config;

import org.mockito.Mockito;
import org.project.mechanic_shop.mappers.UserMapper;
import org.project.mechanic_shop.repositories.UserRepository;
import org.project.mechanic_shop.services.UserService;
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
    UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

}
