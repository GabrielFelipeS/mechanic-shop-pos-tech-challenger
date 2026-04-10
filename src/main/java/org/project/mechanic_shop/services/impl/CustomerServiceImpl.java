package org.project.mechanic_shop.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.models.Customer;
import org.project.mechanic_shop.repositories.CustomerRepository;
import org.project.mechanic_shop.services.CustomerService;
import org.project.mechanic_shop.validators.CustomerValidator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerValidator validator;

    @Override
    @Transactional
    public Customer create(Customer obj) {
        log.info("Creating new customer with document: {}", obj.getDocument());

        validator.validate(obj);

        return repository.save(obj);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findByExternalId(UUID externalId) {
        return repository.findByExternalId(externalId).orElseThrow(() -> {
            log.warn("Customer not found. Action: GET | Target External ID: {}", externalId);
            return new EntityNotFoundException("Customer not found for External ID: " + externalId);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> search(String document, String name, String email, Pageable pageable) {
        log.info("Searching customers with filters - document: {}, name: {}, email: {}", document, name, email);

        var customer = new Customer();
        customer.setDocument(document);
        customer.setName(name);
        customer.setEmail(email);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths(
                        "id",
                        "externalId",
                        "phone",
                        "createdAt",
                        "createdFor",
                        "lastUpdatedAt",
                        "lastUpdatedFor"
                )
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Customer> example = Example.of(customer, matcher);

        return repository.findAll(example, pageable);
    }

    @Override
    @Transactional
    public Customer update(UUID id, Customer update) {

        var obj = repository.findByExternalId(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: "+ id));

        log.info("Updating customer with ID: {}", obj.getId());

        validator.validateUpdateEligibility(obj);

        obj.setName(update.getName());
        obj.setEmail(update.getEmail());
        obj.setPhone(update.getPhone());
        obj.setDocument(update.getDocument());

        obj.setActive(update.getActive());

        validator.validate(obj);


        return repository.save(obj);
    }
}