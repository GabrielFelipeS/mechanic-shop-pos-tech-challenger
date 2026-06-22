package org.project.mechanic_shop.infrastructure.adapters;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.StockItemRepositoryPort;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.project.mechanic_shop.infrastructure.jpa.entities.StockItemJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.mappers.StockItemJpaMapper;
import org.project.mechanic_shop.infrastructure.jpa.repositories.StockItemJpaRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockItemRepositoryAdapter implements StockItemRepositoryPort {

	private final StockItemJpaRepository jpaRepository;
	private final StockItemJpaMapper mapper;

	@Override
	public StockItem save(StockItem item) {
		return mapper.toDomain(jpaRepository.save(mapper.toJpa(item)));
	}

	@Override
	public Optional<StockItem> findByCode(String code) {
		return jpaRepository.findByCode(code).map(mapper::toDomain);
	}

	@Override
	public Optional<StockItem> findByExternalId(UUID externalId) {
		return jpaRepository.findByExternalId(externalId).map(mapper::toDomain);
	}

	@Override
	public Page<StockItem> search(String code, String name, Pageable pageable) {
		StockItemJpaEntity probe = new StockItemJpaEntity();
		probe.setCode(code);
		probe.setName(name);

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths("id", "externalId", "description", "quantity", "costPrice", "salePrice", "createdAt", "createdFor", "lastUpdatedAt", "lastUpdatedFor")
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		return jpaRepository.findAll(Example.of(probe, matcher), pageable).map(mapper::toDomain);
	}
}
