package org.project.mechanic_shop.shared.config.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.project.mechanic_shop.infrastructure.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {

	private final UserRepository repository;

	@Override
	public UserDetails loadUserByUsername(@NonNull String username) {
		var user = repository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return new UserPrincipal(user);
	}
}
