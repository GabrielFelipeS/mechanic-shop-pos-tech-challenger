package org.project.mechanic_shop.validators;

import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.models.Part;
import org.project.mechanic_shop.repositories.PartRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartValidator {

    private final PartRepository repository;

    public void validate(Part part) {
        if (existsByCode(part)) {
            throw new IllegalArgumentException("A Part with this code already exists: " + part.getCode());
        }

        if (part.getSalePrice().compareTo(part.getCostPrice()) < 0) {
            throw new IllegalArgumentException("Sale price cannot be lower than cost price.");
        }

        if (part.getQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
    }

    public void validateUpdateEligibility(Part obj) {
        if (obj.getId() == null) {
            throw new IllegalArgumentException("You cannot update an object without an ID");
        }
    }

    private boolean existsByCode(Part part) {
        Optional<Part> result = repository.findByCode(part.getCode());

        if (part.getId() == null) {
            return result.isPresent();
        }

        return result.isPresent() && !part.getId().equals(result.get().getId());
    }
}