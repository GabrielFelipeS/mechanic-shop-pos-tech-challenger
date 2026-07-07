package org.project.mechanic_shop.infrastructure.adapters;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.MechanicServiceRepositoryPort;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;
import org.project.mechanic_shop.infrastructure.jpa.entities.MechanicServiceJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.mappers.MechanicServiceJpaMapper;
import org.project.mechanic_shop.infrastructure.jpa.repositories.MechanicServiceJpaRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MechanicServiceRepositoryAdapter implements MechanicServiceRepositoryPort {

	private final MechanicServiceJpaRepository jpaRepository;
	private final MechanicServiceJpaMapper mapper;

	@Override
	public MechanicService save(MechanicService ms) {
		return mapper.toDomain(jpaRepository.save(mapper.toJpa(ms)));
	}

	@Override
	public Optional<MechanicService> findByName(String name) {
		return jpaRepository.findByName(name).map(mapper::toDomain);
	}

	@Override
	public Optional<MechanicService> findByExternalId(UUID externalId) {
		return jpaRepository.findByExternalId(externalId).map(mapper::toDomain);
	}

	@Override
	public Page<MechanicService> search(String name, Pageable pageable) {
		MechanicServiceJpaEntity probe = new MechanicServiceJpaEntity();
		probe.setName(name);

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths("id", "externalId", "estimatedTimeMinutes", "price", "createdAt", "createdFor", "lastUpdatedAt", "lastUpdatedFor")
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		return jpaRepository.findAll(Example.of(probe, matcher), pageable).map(mapper::toDomain);
	}
}
