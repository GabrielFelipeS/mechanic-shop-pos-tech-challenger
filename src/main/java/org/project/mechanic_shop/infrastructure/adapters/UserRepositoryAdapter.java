package org.project.mechanic_shop.infrastructure.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.infrastructure.jpa.entities.UserJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.mappers.UserJpaMapper;
import org.project.mechanic_shop.infrastructure.jpa.repositories.UserJpaRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

	private final UserJpaRepository jpaRepository;
	private final UserJpaMapper mapper;

	@Override
	public User save(User user) {
		return mapper.toDomain(jpaRepository.save(mapper.toJpa(user)));
	}

	@Override
	public Optional<User> findByDocument(String document) {
		return jpaRepository.findByDocument(document).map(mapper::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return jpaRepository.findByEmail(email).map(mapper::toDomain);
	}

	@Override
	public Optional<User> findByExternalId(UUID externalId) {
		return jpaRepository.findByExternalId(externalId).map(mapper::toDomain);
	}

	@Override
	public boolean existsByRole(String role) {
		return jpaRepository.existsByRole(role);
	}

	@Override
	public List<User> findByRoleIn(List<String> roles) {
		return jpaRepository.findByRoleIn(roles).stream().map(mapper::toDomain).toList();
	}

	@Override
	public Page<User> search(String document, String name, String email, String role, Pageable pageable) {
		UserJpaEntity probe = new UserJpaEntity();
		probe.setDocument(document);
		probe.setName(name);
		probe.setEmail(email);
		probe.setRole(role);

		ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnorePaths("id", "externalId", "phone", "createdAt", "createdFor", "lastUpdatedAt", "lastUpdatedFor")
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		return jpaRepository.findAll(Example.of(probe, matcher), pageable).map(mapper::toDomain);
	}
}
