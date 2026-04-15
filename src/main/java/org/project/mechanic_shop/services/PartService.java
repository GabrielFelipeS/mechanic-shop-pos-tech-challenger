package org.project.mechanic_shop.services;

import org.project.mechanic_shop.models.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PartService {

    Part create(Part obj);

    Part findByExternalId(UUID externalId);

    Page<Part> search(String code, String name, Pageable pageable);

    Part update(UUID id, Part update);
}