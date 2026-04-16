package org.project.mechanic_shop.services.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserValidator validator;

    @Override
    @Transactional
    public User create(User obj) {
        log.info("Creating new User with document: {}", obj.getDocument());

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
        log.info("Searching Users with filters - document: {}, name: {}, email: {}", document, name, email);

        var user = new User();
        user.setDocument(document);
        user.setName(name);
        user.setEmail(email);

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

        Example<User> example = Example.of(user, matcher);

        return repository.findAll(example, pageable);
    }

    @Override
    @Transactional
    public User update(UUID externalId, User update) {

        var obj = repository.findByExternalId(externalId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with External ID: "+ externalId));

        log.info("Updating User with ID: {}", obj.getId());

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