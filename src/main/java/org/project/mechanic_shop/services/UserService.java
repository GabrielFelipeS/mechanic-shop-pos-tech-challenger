package org.project.mechanic_shop.services;


import org.project.mechanic_shop.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    User create(User obj);

    User findByExternalId(UUID externalId);

    Page<User> search(String document,
                          String name,
                          String email,
                          String role,
                          Pageable pageable);

    User update(UUID id, User update);
}