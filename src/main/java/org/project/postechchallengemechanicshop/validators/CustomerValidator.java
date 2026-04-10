package org.project.postechchallengemechanicshop.validators;

import lombok.RequiredArgsConstructor;
import org.project.postechchallengemechanicshop.models.Customer;
import org.project.postechchallengemechanicshop.repositories.CustomerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerValidator {

    private final CustomerRepository repository;

    public void validate(Customer customer) {
        if (existsCustomerByDocument(customer)) {

            throw new IllegalArgumentException("A Customer with this document already exists: " + customer.getDocument());
        }

        if (existsCustomerByEmail(customer)) {
            throw new IllegalArgumentException("A Customer with this email already exists: " + customer.getEmail());
        }
    }

    public void validateUpdateEligibility(Customer obj) {
        if (obj.getId() == null) {
            throw new IllegalArgumentException("You cannot update an object without an ID");
        }


        if (Boolean.FALSE.equals(obj.getActive())) {
             throw new IllegalArgumentException("You cannot update an inactive object");
        }
    }

    private boolean existsCustomerByDocument(Customer customer) {
        Optional<Customer> result = repository.findByDocument(customer.getDocument());

        if (customer.getId() == null) {
            return result.isPresent();
        }

        return result.isPresent() && !customer.getId().equals(result.get().getId());
    }

    private boolean existsCustomerByEmail(Customer customer) {
        Optional<Customer> result = repository.findByEmail(customer.getEmail());

        if (customer.getId() == null) {
            return result.isPresent();
        }

        return result.isPresent() && !customer.getId().equals(result.get().getId());
    }
}