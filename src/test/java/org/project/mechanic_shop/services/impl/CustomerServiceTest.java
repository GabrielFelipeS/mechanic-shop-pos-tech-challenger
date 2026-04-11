package org.project.mechanic_shop.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.models.Customer;
import org.project.mechanic_shop.repositories.CustomerRepository;
import org.project.mechanic_shop.services.CustomerService;
import org.project.mechanic_shop.utils.CustomerHelper;
import org.project.mechanic_shop.validators.CustomerValidator;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerValidator customerValidator;

    @BeforeEach
    void setup() {
        customerService = new CustomerServiceImpl(customerRepository, customerValidator);
    }

    @Nested
    class Create {
        @Test
        void shouldCreateCustomer() {
            var customer = CustomerHelper.generateCustomer();

            when(customerRepository.save(customer))
                    .thenReturn(customer);

            var customerSave = customerService.create(customer);

            InOrder inOrder = inOrder(customerValidator, customerRepository);

            inOrder.verify(customerValidator).validate(customer);
            inOrder.verify(customerRepository).save(customer);

            assertThat(customerSave)
                    .usingRecursiveAssertion()
                    .ignoringAllNullFields()
                    .isEqualTo(customer);
        }

        @Test
        void shouldNotSaveWhenValidationFails() {
            var customer = CustomerHelper.generateCustomer();

            doThrow(new IllegalArgumentException())
                    .when(customerValidator).validate(customer);

            assertThatThrownBy(() -> customerService.create(customer))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    class FindByExternalId {
        @Test
        void shouldFindCustomerByExternalId() {
            UUID externalId = UUID.randomUUID();
            var customer = CustomerHelper.generateCustomer();

            when(customerRepository.findByExternalId(externalId))
                    .thenReturn(Optional.of(customer));

            var customerFind = customerService.findByExternalId(externalId);

            assertThat(customerFind)
                    .usingRecursiveAssertion()
                    .isEqualTo(customer);

            verify(customerRepository).findByExternalId(externalId);
        }

        @Test
        void shouldThrowExceptionWhenCustomerNotFound() {
            UUID externalId = UUID.randomUUID();

            when(customerRepository.findByExternalId(externalId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.findByExternalId(externalId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage(String.format("Customer not found for External ID: %s", externalId));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateCustomerWhenExists() {
            UUID externalId = UUID.randomUUID();
            var customerFind = CustomerHelper.generateCustomer();

            var customerToUpdate = CustomerHelper.generateCustomer();
            customerToUpdate.setName("NOME_ATUALIZADO");
            customerToUpdate.setActive(false);

            when(customerRepository.findByExternalId(externalId))
                    .thenReturn(Optional.of(customerFind));

            when(customerRepository.save(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var customerUpdated = customerService.update(externalId, customerToUpdate);

            assertThat(customerUpdated.getName()).isEqualTo("NOME_ATUALIZADO");
            assertThat(customerUpdated.getPassword())
                    .isEqualTo("Teste@123");
            assertThat(customerUpdated.getActive()).isFalse();

            assertThat(customerUpdated.getEmail())
                    .isEqualTo(customerFind.getEmail());

            InOrder inOrder = inOrder(customerRepository, customerValidator, customerRepository);

            inOrder.verify(customerRepository).findByExternalId(externalId);
            inOrder.verify(customerValidator).validateUpdateEligibility(customerFind);
            inOrder.verify(customerValidator).validate(customerFind);
            inOrder.verify(customerRepository).save(customerFind);
        }

        @Test
        void shouldThrowExceptionWhenCustomerToUpdateNotFound() {
            UUID externalId = UUID.randomUUID();
            var customer = CustomerHelper.generateCustomer();

            when(customerRepository.findByExternalId(externalId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.update(externalId, customer))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage(String.format("Customer not found for External ID: %s", externalId));
        }

        @Test
        void shouldNotUpdateWhenValidationFails() {
            UUID externalId = UUID.randomUUID();
            var customerFind = CustomerHelper.generateCustomer();
            var customerToUpdate = CustomerHelper.generateCustomer();

            when(customerRepository.findByExternalId(externalId))
                    .thenReturn(Optional.of(customerFind));

            doThrow(new IllegalArgumentException())
                    .when(customerValidator).validate(customerFind);

            assertThatThrownBy(() -> customerService.update(externalId, customerToUpdate)).isInstanceOf(IllegalArgumentException.class);

            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    class Search {
        @SuppressWarnings("unchecked")
        private ArgumentCaptor<Example<Customer>> exampleCustomerCaptor() {
            return (ArgumentCaptor<Example<Customer>>) (ArgumentCaptor<?>)
                    ArgumentCaptor.forClass(Example.class);
        }

        @SuppressWarnings("unchecked")
        private Example<Customer> getAnyExample() {
            return any(Example.class);
        }

        @Test
        void shouldReturnCustomersWhenSearchCriteriaIsProvided() {
            var customer = CustomerHelper.generateCustomer();
            Customer expected = new Customer();
            expected.setDocument(customer.getDocument());
            expected.setName(customer.getName());
            expected.setEmail(customer.getEmail());

            ArgumentCaptor<Example<Customer>> captor = exampleCustomerCaptor();

            Pageable pageable = PageRequest.of(0, 10);

            Page<Customer> expectedPage = new PageImpl<>(List.of(customer));

            when(customerRepository.findAll(getAnyExample(), eq(pageable)))
                    .thenReturn(expectedPage);

            var customerPage = customerService.search(
                    customer.getDocument(),
                    customer.getName(),
                    customer.getEmail(),
                    pageable
            );

            assertThat(customerPage).isEqualTo(expectedPage);

            verify(customerRepository).findAll(captor.capture(), eq(pageable));

            Example<Customer> capturedExample = captor.getValue();

            Customer probe = capturedExample.getProbe();

            assertThat(probe.getDocument()).isEqualTo(customer.getDocument());
            assertThat(probe.getName()).isEqualTo(customer.getName());
            assertThat(probe.getEmail()).isEqualTo(customer.getEmail());
        }
    }
}