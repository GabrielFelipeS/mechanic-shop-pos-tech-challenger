package org.project.mechanic_shop.infrastructure.jpa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.infrastructure.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
	Optional<UserJpaEntity> findByDocument(String document);
	Optional<UserJpaEntity> findByEmail(String email);
	Optional<UserJpaEntity> findByExternalId(UUID externalId);
	boolean existsByRole(String role);
	List<UserJpaEntity> findByRoleIn(List<String> roles);
}
