package org.project.mechanic_shop.application.services;

import java.util.List;
import java.util.UUID;
import org.project.mechanic_shop.domain.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
	User create(User obj);

	User findByExternalId(UUID externalId);

	User findByEmail(String email);

	Page<User> search(String document, String name, String email, String role, Pageable pageable);

	List<User> findByRoles(List<String> roles);

	User update(UUID id, User update);
}
