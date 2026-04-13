package org.project.mechanic_shop.services;


import com.auth0.jwt.JWT;
import org.project.mechanic_shop.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.UUID;

public interface UserService {

    User create(User obj);

    User findByExternalId(UUID externalId);

    Page<User> search(String document,
                          String name,
                          String email,
                          Pageable pageable);

    User update(UUID id, User update, UserDetails userAuth);
}