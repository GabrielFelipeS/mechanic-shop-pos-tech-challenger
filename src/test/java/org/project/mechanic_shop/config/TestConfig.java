package org.project.mechanic_shop.config;

import org.mockito.Mockito;
import org.project.mechanic_shop.mappers.CustomerMapper;
import org.project.mechanic_shop.repositories.CustomerRepository;
import org.project.mechanic_shop.services.CustomerService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean
    CustomerService customerService() {
        return Mockito.mock(CustomerService.class);
    }

    @Bean
    CustomerMapper customerMapper() {
        return Mockito.mock(CustomerMapper.class);
    }

    @Bean
    CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }
}
