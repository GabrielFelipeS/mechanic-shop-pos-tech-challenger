package org.project.mechanic_shop.application.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.UserRoleEnum;
import org.project.mechanic_shop.application.services.UserService;
import org.project.mechanic_shop.application.validators.UserValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

	private final UserRepositoryPort repository;
	private final UserValidator validator;
	private final PasswordEncoder passwordEncoder;

	private static final Set<String> RECEPTIONIST_ASSIGNABLE_ROLES = Set.of(UserRoleEnum.CUSTOMER.name());

	@Override
	@Transactional
	public User create(User obj) {
		log.info("Creating new User with document: {}", obj.getDocument());

		assertCallerCanAssignRole(obj);

		obj.setPassword(passwordEncoder.encode(obj.getPassword()));
		validator.validate(obj);

		return repository.save(obj);
	}

	private void assertCallerCanAssignRole(User obj) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) return;

		boolean isReceptionist = auth
			.getAuthorities()
			.stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRoleEnum.RECEPTIONIST));

		if (!isReceptionist) return;

		if (obj.getRole() == null || !RECEPTIONIST_ASSIGNABLE_ROLES.contains(obj.getRole())) {
			throw new AccessDeniedException("Receptionists can only create CUSTOMER accounts.");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public User findByExternalId(UUID externalId) {
		return repository
			.findByExternalId(externalId)
			.orElseThrow(() -> {
				log.warn("User not found. Action: GET | Target External ID: {}", externalId);
				return new EntityNotFoundException("User not found for External ID: " + externalId);
			});
	}

	@Override
	@Transactional(readOnly = true)
	public User findByEmail(String email) {
		return repository
			.findByEmail(email)
			.orElseThrow(() -> new EntityNotFoundException("User not found for email: " + email));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<User> search(String document, String name, String email, String role, Pageable pageable) {
		log.info("Searching Users with filters - document: {}, name: {}, email: {}", document, name, email);

		return repository.search(document, name, email, role, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findByRoles(List<String> roles) {
		return repository.findByRoleIn(roles);
	}

	@Override
	@Transactional
	public User update(UUID externalId, User update) {
		var obj = repository
			.findByExternalId(externalId)
			.orElseThrow(() -> new EntityNotFoundException("User not found with External ID: " + externalId));

		log.info("Updating User with ID: {}", obj.getId());

		validator.validateUpdateEligibility(obj, update);

		obj.setName(update.getName());
		obj.setEmail(update.getEmail());
		obj.setPhone(update.getPhone());
		obj.setDocument(update.getDocument());
		obj.setPassword(passwordEncoder.encode(update.getPassword()));
		obj.setActive(update.getActive());

		validator.validate(obj);

		return repository.save(obj);
	}
}
