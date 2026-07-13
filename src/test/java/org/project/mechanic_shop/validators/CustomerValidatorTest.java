package org.project.mechanic_shop.validators;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.application.validators.UserValidator;
import org.project.mechanic_shop.utils.UserHelper;

@ExtendWith(MockitoExtension.class)
class CustomerValidatorTest {

	private UserValidator userValidator;

	@Mock
	private UserRepositoryPort userRepository;

	@BeforeEach
	void beforeEach() {
		userValidator = new UserValidator(userRepository);
	}

	@Nested
	class Validate {

		@Test
		void shouldNotThrowExceptionWhenUserIsSame() {
			var user = UserHelper.generateUser();

			when(userRepository.findByDocument(user.getDocument())).thenReturn(Optional.of(user));
			when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

			assertDoesNotThrow(() -> userValidator.validate(user));

			InOrder inOrder = inOrder(userRepository);

			inOrder.verify(userRepository).findByDocument(user.getDocument());
			inOrder.verify(userRepository).findByEmail(user.getEmail());
		}

		@Test
		void shouldNotThrowExceptionWhenUserDoesNotExists() {
			var user = UserHelper.generateUser();

			when(userRepository.findByDocument(user.getDocument())).thenReturn(Optional.empty());
			when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

			assertDoesNotThrow(() -> userValidator.validate(user));

			InOrder inOrder = inOrder(userRepository);

			inOrder.verify(userRepository).findByDocument(user.getDocument());
			inOrder.verify(userRepository).findByEmail(user.getEmail());
		}

		@Test
		void shouldThrowExceptionWhenAlreadyExistsUserWithDocument() {
			var user = UserHelper.generateUser();
			user.setId(null);

			var alreadyUser = UserHelper.generateUser();

			when(userRepository.findByDocument(user.getDocument())).thenReturn(Optional.of(alreadyUser));

			assertThatThrownBy(() -> userValidator.validate(user))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(String.format("A User with this document already exists: %s", user.getDocument()));

			verify(userRepository).findByDocument(alreadyUser.getDocument());
			verify(userRepository, never()).findByEmail(alreadyUser.getEmail());
		}

		@Test
		void shouldThrowExceptionWhenAlreadyUserWithDocumentHaveDifferentId() {
			var user = UserHelper.generateUser();
			var alreadyUser = UserHelper.generateUser();
			alreadyUser.setId(2L);

			when(userRepository.findByDocument(user.getDocument())).thenReturn(Optional.of(alreadyUser));

			assertThatThrownBy(() -> userValidator.validate(user))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(String.format("A User with this document already exists: %s", user.getDocument()));

			verify(userRepository).findByDocument(alreadyUser.getDocument());
			verify(userRepository, never()).findByEmail(alreadyUser.getEmail());
		}

		@Test
		void shouldThrowExceptionWhenAlreadyExistsUserWithEmail() {
			var user = UserHelper.generateUser();
			user.setId(null);

			var alreadyUser = UserHelper.generateUser();

			when(userRepository.findByDocument(user.getDocument())).thenReturn(Optional.empty());
			when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(alreadyUser));

			assertThatThrownBy(() -> userValidator.validate(user))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(String.format("A User with this email already exists: %s", user.getEmail()));

			InOrder inOrder = inOrder(userRepository);

			inOrder.verify(userRepository).findByDocument(alreadyUser.getDocument());
			inOrder.verify(userRepository).findByEmail(alreadyUser.getEmail());
		}

		@Test
		void shouldThrowExceptionWhenAlreadyUserWithEmailHaveDifferentId() {
			var user = UserHelper.generateUser();
			var alreadyUser = UserHelper.generateUser();
			alreadyUser.setId(2L);

			when(userRepository.findByDocument(user.getDocument())).thenReturn(Optional.empty());
			when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(alreadyUser));

			assertThatThrownBy(() -> userValidator.validate(user))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(String.format("A User with this email already exists: %s", user.getEmail()));

			verify(userRepository).findByDocument(alreadyUser.getDocument());
			verify(userRepository).findByEmail(alreadyUser.getEmail());
		}
	}

	@Nested
	class ValidateUpdateEligibility {

		@Test
		void shouldNotThrowWhenUserAlreadyExitsIdAndIsActive() {
			var user = UserHelper.generateUser();

			assertDoesNotThrow(() -> userValidator.validateUpdateEligibility(user, user));
		}

		@Test
		void shouldThrowWhenUserDoesNotHaveId() {
			var user = UserHelper.generateUser();
			user.setId(null);

			assertThatThrownBy(() -> userValidator.validateUpdateEligibility(user, user))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("You cannot update an object without an ID");
		}

		@Test
		void shouldThrowWhenUserIsNotActiveAndUpdateDoesNotReactivate() {
			var current = UserHelper.generateUser();
			current.setActive(false);
			var update = UserHelper.generateUser();
			update.setActive(false);

			assertThatThrownBy(() -> userValidator.validateUpdateEligibility(current, update))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("You cannot update an inactive object");
		}

		@Test
		void shouldNotThrowWhenUserIsNotActiveButUpdateReactivatesIt() {
			var current = UserHelper.generateUser();
			current.setActive(false);
			var update = UserHelper.generateUser();
			update.setActive(true);

			assertDoesNotThrow(() -> userValidator.validateUpdateEligibility(current, update));
		}
	}
}
