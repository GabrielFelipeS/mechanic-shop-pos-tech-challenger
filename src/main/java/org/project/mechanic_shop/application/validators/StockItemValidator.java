package org.project.mechanic_shop.application.validators;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.StockItemRepositoryPort;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockItemValidator {

	private final StockItemRepositoryPort repository;

	public void validate(StockItem stockItem) {
		if (existsByCode(stockItem)) {
			throw new IllegalArgumentException("A Part with this code already exists: " + stockItem.getCode());
		}

		if (stockItem.getSalePrice().compareTo(stockItem.getCostPrice()) < 0) {
			throw new IllegalArgumentException("Sale price cannot be lower than cost price.");
		}

		if (stockItem.getQuantity() < 0) {
			throw new IllegalArgumentException("Stock quantity cannot be negative.");
		}
	}

	public void validateUpdateEligibility(StockItem obj) {
		if (obj.getId() == null) {
			throw new IllegalArgumentException("You cannot update an object without an ID");
		}
	}

	private boolean existsByCode(StockItem stockItem) {
		Optional<StockItem> result = repository.findByCode(stockItem.getCode());

		if (stockItem.getId() == null) {
			return result.isPresent();
		}

		return (result.isPresent() && !stockItem.getId().equals(result.get().getId()));
	}
}
