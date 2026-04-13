package org.project.mechanic_shop.repositories;

import org.project.mechanic_shop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByDocument(String document);

    Optional<User> findByEmail(String email);

    Optional<User> findByExternalId(UUID externalId);
}
