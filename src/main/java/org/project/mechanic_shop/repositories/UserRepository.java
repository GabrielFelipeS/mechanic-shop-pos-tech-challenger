package org.project.mechanic_shop.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByDocument(String document);

	Optional<User> findByEmail(String email);

	Optional<User> findByExternalId(UUID externalId);

	boolean existsByRole(String role);

	List<User> findByRoleIn(List<String> roles);
}
