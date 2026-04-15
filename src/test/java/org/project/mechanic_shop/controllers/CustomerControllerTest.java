package org.project.mechanic_shop.controllers;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.mockito.ArgumentCaptor;
import org.project.mechanic_shop.config.ObjectMapperConfig;
import org.project.mechanic_shop.dto.customer_dto.CustomerManDto;
import org.project.mechanic_shop.mappers.CustomerMapperImpl;
import org.project.mechanic_shop.models.Customer;
import org.project.mechanic_shop.services.CustomerService;
import org.project.mechanic_shop.utils.CustomerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;


@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({CustomerMapperImpl.class, ObjectMapperConfig.class})
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService service;

    @Nested
    class FindById {
        @Test
        void shouldFindCustomerByExternalId() throws Exception {
            var externalId = UUID.randomUUID();
            var customer = CustomerHelper.generateCustomer();

            when(service.findByExternalId(externalId)).thenReturn(customer);

            mockMvc.perform(get(String.format("/api/customers/%s", externalId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.name").value("TESTE"));
        }

        @Test
        void shouldNotFoundCustomerByExternalId() throws Exception {
            var externalId = UUID.randomUUID();

            when(service.findByExternalId(externalId)).thenThrow(EntityNotFoundException.class);

            mockMvc.perform(get(String.format("/api/customers/%s", externalId)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Create {
        @Test
        void shouldBeCreateCustomer() throws Exception {
            var customer = CustomerHelper.generateCustomer();

            CustomerManDto customerDto = CustomerHelper.generateCustomerMenDto();

            when(service.create(any(Customer.class))).thenReturn(customer);

            mockMvc.perform(
                    post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(customerDto))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value( HttpStatus.CREATED.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data").value(customer.getExternalId()));
        }

        @Test
        void shouldThrowIllegalArgumentException() throws Exception {
            CustomerManDto customerDto = CustomerHelper.generateCustomerMenDto();

            when(service.create(any(Customer.class))).thenThrow(IllegalArgumentException.class);

            mockMvc.perform(
                            post("/api/customers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(customerDto))
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value( HttpStatus.CONFLICT.value()))
            ;
        }
    }

    @Nested
    class Update {
        @Test
        void update() {
            fail("not implemented");
        }
    }

    @Nested
    class Search {
        @Test
        void shouldSearchWithPageable() throws Exception {
            var customer = CustomerHelper.generateCustomer();
            var listCustomer = List.of(customer);
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

            Page<Customer> page = new PageImpl<>(
                    listCustomer,
                    PageRequest.of(0, 2),
                    1
            );

            when(service.search(
                    eq(customer.getDocument()),
                    eq(customer.getName()),
                    eq(customer.getEmail()),
                    any(Pageable.class)
            )).thenReturn(page);

            mockMvc.perform(
                            get("/api/customers/search")
                                    .param("document", customer.getDocument())
                                    .param("name", customer.getName())
                                    .param("email", customer.getEmail())
                                    .param("page", "0")
                                    .param("size", "10")
                                    .param("sort", "name,asc")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value( HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value(customer.getName()))
            ;

            verify(service).search(
                    eq(customer.getDocument()),
                    eq(customer.getName()),
                    eq(customer.getEmail()),
                    captor.capture()
            );

            Pageable pageableCaptured = captor.getValue();

            assertThat(pageableCaptured.getPageNumber()).isZero();
            assertThat(pageableCaptured.getPageSize()).isEqualTo(10);
            assertThat(pageableCaptured.getSort().getOrderFor("name").isAscending()).isTrue();
        }

        @Test
        void shouldSearchWithPageableDefault() throws Exception {
            var customer = CustomerHelper.generateCustomer();
            var listCustomer = List.of(customer);
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

            Page<Customer> page = new PageImpl<>(
                    listCustomer,
                    PageRequest.of(0, 2),
                    1
            );

            when(service.search(
                    eq(customer.getDocument()),
                    any(),
                    any(),
                    any(Pageable.class)
            )).thenReturn(page);

            mockMvc.perform(
                            get("/api/customers/search")
                                    .param("document", customer.getDocument())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value( HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value(customer.getName()))
            ;

            verify(service).search(
                    eq(customer.getDocument()),
                    eq(customer.getName()),
                    eq(customer.getEmail()),
                    captor.capture()
            );

            Pageable pageableCaptured = captor.getValue();
            pageableCaptured.getSort().forEach(System.out::println);
            assertThat(pageableCaptured.getPageNumber()).isZero();
            assertThat(pageableCaptured.getPageSize()).isEqualTo(10);
            assertThat(pageableCaptured.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        }
    }
}