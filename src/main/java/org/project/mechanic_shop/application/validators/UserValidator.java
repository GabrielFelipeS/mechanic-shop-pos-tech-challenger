package org.project.mechanic_shop.application.validators;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.infrastructure.repositories.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

	private final UserRepository repository;

	public void validate(User user) {
		if (existsUserByDocument(user)) {
			throw new IllegalArgumentException("A User with this document already exists: " + user.getDocument());
		}

		if (existsUserByEmail(user)) {
			throw new IllegalArgumentException("A User with this email already exists: " + user.getEmail());
		}
	}

	public void validateUpdateEligibility(User obj) {
		if (obj.getId() == null) {
			throw new IllegalArgumentException("You cannot update an object without an ID");
		}

		if (Boolean.FALSE.equals(obj.getActive())) {
			throw new IllegalArgumentException("You cannot update an inactive object");
		}
	}

	private boolean existsUserByDocument(User user) {
		Optional<User> result = repository.findByDocument(user.getDocument());

		if (user.getId() == null) {
			return result.isPresent();
		}

		return result.isPresent() && !user.getId().equals(result.get().getId());
	}

	private boolean existsUserByEmail(User user) {
		Optional<User> result = repository.findByEmail(user.getEmail());

		if (user.getId() == null) {
			return result.isPresent();
		}

		return result.isPresent() && !user.getId().equals(result.get().getId());
	}
}
