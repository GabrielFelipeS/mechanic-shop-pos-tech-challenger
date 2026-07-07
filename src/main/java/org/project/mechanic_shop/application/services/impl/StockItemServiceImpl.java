package org.project.mechanic_shop.application.services.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.events.OutOfStockEvent;
import org.project.mechanic_shop.application.ports.StockItemRepositoryPort;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.project.mechanic_shop.application.services.StockItemService;
import org.project.mechanic_shop.application.validators.StockItemValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockItemServiceImpl implements StockItemService {

	private final StockItemRepositoryPort repository;
	private final StockItemValidator validator;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public StockItem create(StockItem obj) {
		log.info("Creating new part with code: {}", obj.getCode());

		validator.validate(obj);

		return repository.save(obj);
	}

	@Override
	public StockItem findByExternalId(UUID externalId) {
		return repository
			.findByExternalId(externalId)
			.orElseThrow(() -> {
				log.warn("Part not found. Action: GET | Target External ID: {}", externalId);
				return new EntityNotFoundException("Part not found for External ID: " + externalId);
			});
	}

	@Override
	public Page<StockItem> search(String code, String name, Pageable pageable) {
		log.info("Searching parts with filters - code: {}, name: {}", code, name);

		return repository.search(code, name, pageable);
	}

	@Override
	public StockItem update(UUID id, StockItem update) {
		var obj = repository
			.findByExternalId(id)
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

	@Override
	@Transactional
	public void withdrawStock(UUID externalId, Integer requestedQuantity) {
		StockItem item = findByExternalId(externalId);
		int currentStock = item.getQuantity();

		int missingQuantity = requestedQuantity - currentStock;

		if (missingQuantity > 0) {
			item.setQuantity(0);
			item.setPendingDemand(item.getPendingDemand() + missingQuantity);

			eventPublisher.publishEvent(new OutOfStockEvent(item, item.getPendingDemand()));
		} else {
			item.setQuantity(currentStock - requestedQuantity);
		}

		repository.save(item);
	}
}
