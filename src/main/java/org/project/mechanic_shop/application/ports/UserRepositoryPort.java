package org.project.mechanic_shop.application.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryPort {
	User save(User user);
	Optional<User> findByDocument(String document);
	Optional<User> findByEmail(String email);
	Optional<User> findByExternalId(UUID externalId);
	boolean existsByRole(String role);
	List<User> findByRoleIn(List<String> roles);
	Page<User> search(String document, String name, String email, String role, Pageable pageable);
}
