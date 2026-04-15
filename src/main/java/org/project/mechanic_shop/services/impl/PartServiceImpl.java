package org.project.mechanic_shop.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.models.Part;
import org.project.mechanic_shop.repositories.PartRepository;
import org.project.mechanic_shop.services.PartService;
import org.project.mechanic_shop.validators.PartValidator;
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
public class PartServiceImpl implements PartService {

    private final PartRepository repository;
    private final PartValidator validator;

    @Override
    public Part create(Part obj) {
        log.info("Creating new part with code: {}", obj.getCode());

        validator.validate(obj);

        return repository.save(obj);
    }

    @Override
    public Part findByExternalId(UUID externalId) {
        return repository.findByExternalId(externalId).orElseThrow(() -> {
            log.warn("Part not found. Action: GET | Target External ID: {}", externalId);
            return new EntityNotFoundException("Part not found for External ID: " + externalId);
        });
    }

    @Override
    public Page<Part> search(String code, String name, Pageable pageable) {
        log.info("Searching parts with filters - code: {}, name: {}", code, name);

        var part = new Part();
        part.setCode(code);
        part.setName(name);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths(
                        "id",
                        "externalId",
                        "description",
                        "quantity",
                        "costPrice",
                        "salePrice",
                        "createdAt",
                        "createdFor",
                        "lastUpdatedAt",
                        "lastUpdatedFor"
                )
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Part> example = Example.of(part, matcher);

        return repository.findAll(example, pageable);
    }

    @Override
    public Part update(UUID id, Part update) {
        var obj = repository.findByExternalId(id)
                .orElseThrow(() -> new EntityNotFoundException("Part not found with id: " + id));

        log.info("Updating part with ID: {}", obj.getId());

        validator.validateUpdateEligibility(obj);

        obj.setCode(update.getCode());
        obj.setName(update.getName());
        obj.setDescription(update.getDescription());
        obj.setQuantity(update.getQuantity());
        obj.setCostPrice(update.getCostPrice());
        obj.setSalePrice(update.getSalePrice());

        validator.validate(obj);

        return repository.save(obj);
    }
}