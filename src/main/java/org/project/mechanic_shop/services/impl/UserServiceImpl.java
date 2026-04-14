package org.project.mechanic_shop.services.impl;

import com.auth0.jwt.JWT;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.repositories.UserRepository;
import org.project.mechanic_shop.services.UserService;
import org.project.mechanic_shop.validators.UserValidator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator validator;

    @Override
    @Transactional
    public User create(User obj) {
        log.info("Creating new customer with document: {}", obj.getDocument());

        obj.setPassword(passwordEncoder.encode(obj.getPassword()));
        validator.validate(obj);

        return repository.save(obj);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByExternalId(UUID externalId) {
        return repository.findByExternalId(externalId).orElseThrow(() -> {
            log.warn("User not found. Action: GET | Target External ID: {}", externalId);
            return new EntityNotFoundException("User not found for External ID: " + externalId);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> search(String document, String name, String email, Pageable pageable) {
        log.info("Searching customers with filters - document: {}, name: {}, email: {}", document, name, email);

        var customer = new User();
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

        Example<User> example = Example.of(customer, matcher);

        return repository.findAll(example, pageable);
    }

    @Override
    @Transactional
    public User update(UUID id, User update) {

        var obj = repository.findByExternalId(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: "+ id));

        log.info("Updating customer with ID: {}", obj.getId());

        validator.validateUpdateEligibility(obj);

        obj.setName(update.getName());
        obj.setEmail(update.getEmail());
        obj.setPassword(passwordEncoder.encode(update.getPassword()));
        obj.setPhone(update.getPhone());
        obj.setDocument(update.getDocument());
        obj.setRole(update.getRole());
        obj.setActive(update.getActive());
        validator.validate(obj);


        return repository.save(obj);
    }
}