package org.project.mechanic_shop.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.repositories.CustomerRepository;
import org.project.mechanic_shop.utils.CustomerHelper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerValidatorTest {

    private CustomerValidator customerValidator;
    @Mock
    private CustomerRepository customerRepository;

    @BeforeEach
    void beforeEach() {
        customerValidator = new CustomerValidator(customerRepository);
    }

    @Nested
    class Validate {
        @Test
        void shouldNotThrowExceptionWhenCustomerIsSame() {
            var customer = CustomerHelper.generateCustomer();

            when(customerRepository.findByDocument(customer.getDocument())).thenReturn(Optional.of(customer));
            when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));

            assertDoesNotThrow(() -> customerValidator.validate(customer));

            InOrder inOrder = inOrder(customerRepository);

            inOrder.verify(customerRepository).findByDocument(customer.getDocument());
            inOrder.verify(customerRepository).findByEmail(customer.getEmail());
        }

        @Test
        void shouldNotThrowExceptionWhenCustomerDoesNotExists() {
            var customer = CustomerHelper.generateCustomer();

            when(customerRepository.findByDocument(customer.getDocument())).thenReturn(Optional.empty());
            when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> customerValidator.validate(customer));

            InOrder inOrder = inOrder(customerRepository);

            inOrder.verify(customerRepository).findByDocument(customer.getDocument());
            inOrder.verify(customerRepository).findByEmail(customer.getEmail());
        }

        @Test
        void shouldThrowExceptionWhenAlreadyExistsCustomerWithDocument() {
            var customer = CustomerHelper.generateCustomer();
            customer.setId(null);

            var alreadyCustomer = CustomerHelper.generateCustomer();

            when(customerRepository.findByDocument(customer.getDocument())).thenReturn(Optional.of(alreadyCustomer));

            assertThatThrownBy(() -> customerValidator.validate(customer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("A Customer with this document already exists: %s", customer.getDocument()));

            verify(customerRepository).findByDocument(alreadyCustomer.getDocument());
            verify(customerRepository, never()).findByEmail(alreadyCustomer.getEmail());
        }

        @Test
        void shouldThrowExceptionWhenAlreadyCustomerWithDocumentHaveDifferentId() {
            var customer = CustomerHelper.generateCustomer();
            var alreadyCustomer = CustomerHelper.generateCustomer();
            alreadyCustomer.setId(2L);

            when(customerRepository.findByDocument(customer.getDocument())).thenReturn(Optional.of(alreadyCustomer));

            assertThatThrownBy(() -> customerValidator.validate(customer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("A Customer with this document already exists: %s", customer.getDocument()));

            verify(customerRepository).findByDocument(alreadyCustomer.getDocument());
            verify(customerRepository, never()).findByEmail(alreadyCustomer.getEmail());
        }

        @Test
        void shouldThrowExceptionWhenAlreadyExistsCustomerWithEmail() {
            var customer = CustomerHelper.generateCustomer();
            customer.setId(null);

            var alreadyCustomer = CustomerHelper.generateCustomer();

            when(customerRepository.findByDocument(customer.getDocument())).thenReturn(Optional.empty());
            when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(alreadyCustomer));

            assertThatThrownBy(() -> customerValidator.validate(customer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("A Customer with this email already exists: %s", customer.getEmail()));

            InOrder inOrder = inOrder(customerRepository);

            inOrder.verify(customerRepository).findByDocument(alreadyCustomer.getDocument());
            inOrder.verify(customerRepository).findByEmail(alreadyCustomer.getEmail());
        }

        @Test
        void shouldThrowExceptionWhenAlreadyCustomerWithEmailHaveDifferentId() {
            var customer = CustomerHelper.generateCustomer();
            var alreadyCustomer = CustomerHelper.generateCustomer();
            alreadyCustomer.setId(2L);

            when(customerRepository.findByDocument(customer.getDocument())).thenReturn(Optional.empty());
            when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(alreadyCustomer));

            assertThatThrownBy(() -> customerValidator.validate(customer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("A Customer with this email already exists: %s", customer.getEmail()));

            verify(customerRepository).findByDocument(alreadyCustomer.getDocument());
            verify(customerRepository).findByEmail(alreadyCustomer.getEmail());
        }
    }

    @Nested
    class ValidateUpdateEligibility {
        @Test
        void shouldNotThrowWhenCustomerAlreadyExitsIdAndIsActive() {
            var customer = CustomerHelper.generateCustomer();

            assertDoesNotThrow(() -> customerValidator.validateUpdateEligibility(customer));
        }

        @Test
        void shouldThrowWhenCustomerDoesNotHaveId() {
            var customer = CustomerHelper.generateCustomer();
            customer.setId(null);

            assertThatThrownBy(() -> customerValidator.validateUpdateEligibility(customer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("You cannot update an object without an ID");
        }

        @Test
        void shouldThrowWhenCustomerIsNotActive() {
            var customer = CustomerHelper.generateCustomer();
            customer.setActive(false);

            assertThatThrownBy(() -> customerValidator.validateUpdateEligibility(customer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("You cannot update an inactive object");
        }
    }
}