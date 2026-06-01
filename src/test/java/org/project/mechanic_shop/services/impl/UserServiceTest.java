package org.project.mechanic_shop.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.application.services.impl.UserServiceImpl;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.infrastructure.repositories.UserRepository;
import org.project.mechanic_shop.application.services.UserService;
import org.project.mechanic_shop.utils.UserHelper;
import org.project.mechanic_shop.application.validators.UserValidator;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserValidator userValidator;

	@Mock
	PasswordEncoder passwordEncoder;

	@BeforeEach
	void setup() {
		userService = new UserServiceImpl(userRepository, userValidator, passwordEncoder);
	}

	@Nested
	class Create {

		@Test
		void shouldCreateUser() {
			var user = UserHelper.generateUser();

			when(userRepository.save(user)).thenReturn(user);

			var userSave = userService.create(user);

			InOrder inOrder = inOrder(userValidator, userRepository);

			inOrder.verify(userValidator).validate(user);
			inOrder.verify(userRepository).save(user);

			assertThat(userSave).usingRecursiveAssertion().ignoringAllNullFields().isEqualTo(user);
		}

		@Test
		void shouldNotSaveWhenValidationFails() {
			var user = UserHelper.generateUser();

			doThrow(new IllegalArgumentException()).when(userValidator).validate(user);

			assertThatThrownBy(() -> userService.create(user)).isInstanceOf(IllegalArgumentException.class);

			verify(userRepository, never()).save(any());
		}
	}

	@Nested
	class FindByExternalId {

		@Test
		void shouldFindUserByExternalId() {
			UUID externalId = UUID.randomUUID();
			var user = UserHelper.generateUser();

			when(userRepository.findByExternalId(externalId)).thenReturn(Optional.of(user));

			var userFind = userService.findByExternalId(externalId);

			assertThat(userFind).usingRecursiveAssertion().isEqualTo(user);

			verify(userRepository).findByExternalId(externalId);
		}

		@Test
		void shouldThrowExceptionWhenUserNotFound() {
			UUID externalId = UUID.randomUUID();

			when(userRepository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> userService.findByExternalId(externalId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage(String.format("User not found for External ID: %s", externalId));
		}
	}

	@Nested
	class Update {

		@Test
		void shouldUpdateUserWhenExists() {
			UUID externalId = UUID.randomUUID();
			var userFind = UserHelper.generateUser();

			var userToUpdate = UserHelper.generateUser();
			userToUpdate.setName("NOME_ATUALIZADO");
			userToUpdate.setActive(false);

			when(userRepository.findByExternalId(externalId)).thenReturn(Optional.of(userFind));

			when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

			var userUpdated = userService.update(externalId, userToUpdate);

			assertThat(userUpdated.getName()).isEqualTo("NOME_ATUALIZADO");
			assertThat(userUpdated.getActive()).isFalse();

			assertThat(userUpdated.getEmail()).isEqualTo(userFind.getEmail());

			InOrder inOrder = inOrder(userRepository, userValidator, userRepository);

			inOrder.verify(userRepository).findByExternalId(externalId);
			inOrder.verify(userValidator).validateUpdateEligibility(userFind);
			inOrder.verify(userValidator).validate(userFind);
			inOrder.verify(userRepository).save(userFind);
		}

		@Test
		void shouldThrowExceptionWhenUserToUpdateNotFound() {
			UUID externalId = UUID.randomUUID();
			var user = UserHelper.generateUser();

			when(userRepository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> userService.update(externalId, user))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage(String.format("User not found with External ID: %s", externalId));
		}

		@Test
		void shouldNotUpdateWhenValidationFails() {
			UUID externalId = UUID.randomUUID();
			var userFind = UserHelper.generateUser();
			var userToUpdate = UserHelper.generateUser();

			when(userRepository.findByExternalId(externalId)).thenReturn(Optional.of(userFind));

			doThrow(new IllegalArgumentException()).when(userValidator).validate(userFind);

			assertThatThrownBy(() -> userService.update(externalId, userToUpdate)).isInstanceOf(
				IllegalArgumentException.class
			);

			verify(userRepository, never()).save(any());
		}
	}

	@Nested
	class Search {

		@SuppressWarnings("unchecked")
		private ArgumentCaptor<Example<User>> exampleUserCaptor() {
			return (ArgumentCaptor<Example<User>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Example.class);
		}

		@SuppressWarnings("unchecked")
		private Example<User> getAnyExample() {
			return any(Example.class);
		}

		@Test
		void shouldReturnUsersWhenSearchCriteriaIsProvided() {
			var user = UserHelper.generateUser();
			User expected = new User();
			expected.setDocument(user.getDocument());
			expected.setName(user.getName());
			expected.setEmail(user.getEmail());

			ArgumentCaptor<Example<User>> captor = exampleUserCaptor();

			Pageable pageable = PageRequest.of(0, 10);

			Page<User> expectedPage = new PageImpl<>(List.of(user));

			when(userRepository.findAll(getAnyExample(), eq(pageable))).thenReturn(expectedPage);

			var userPage = userService.search(
				user.getDocument(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				pageable
			);

			assertThat(userPage).isEqualTo(expectedPage);

			verify(userRepository).findAll(captor.capture(), eq(pageable));

			Example<User> capturedExample = captor.getValue();

			User probe = capturedExample.getProbe();

			assertThat(probe.getDocument()).isEqualTo(user.getDocument());
			assertThat(probe.getName()).isEqualTo(user.getName());
			assertThat(probe.getEmail()).isEqualTo(user.getEmail());
		}
	}
}
