package org.project.mechanic_shop.services.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.application.services.UserService;
import org.project.mechanic_shop.application.services.impl.UserServiceImpl;
import org.project.mechanic_shop.application.validators.UserValidator;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.utils.UserHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private UserService userService;

	@Mock
	private UserRepositoryPort userRepository;

	@Mock
	private UserValidator userValidator;

	@Mock
	PasswordEncoder passwordEncoder;

	@BeforeEach
	void setup() {
		userService = new UserServiceImpl(userRepository, userValidator, passwordEncoder);
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private void authenticateAs(String role) {
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("staff@test.com", null, authorities)
		);
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

		@Test
		void shouldAllowReceptionistToCreateCustomer() {
			var user = UserHelper.generateUser();
			user.setRole("CUSTOMER");

			when(userRepository.save(user)).thenReturn(user);
			authenticateAs("RECEPTIONIST");

			var userSave = userService.create(user);

			assertThat(userSave).usingRecursiveAssertion().ignoringAllNullFields().isEqualTo(user);
			verify(userRepository).save(user);
		}

		@Test
		void shouldDenyReceptionistCreatingAdmin() {
			var user = UserHelper.generateUser();
			user.setRole("ADMIN");

			authenticateAs("RECEPTIONIST");

			assertThatThrownBy(() -> userService.create(user)).isInstanceOf(AccessDeniedException.class);

			verify(userRepository, never()).save(any());
		}

		@Test
		void shouldDenyReceptionistCreatingStaffRoles() {
			var user = UserHelper.generateUser();
			user.setRole("MECHANIC");

			authenticateAs("RECEPTIONIST");

			assertThatThrownBy(() -> userService.create(user)).isInstanceOf(AccessDeniedException.class);

			verify(userRepository, never()).save(any());
		}

		@Test
		void shouldAllowAdminToCreateAnyRole() {
			var user = UserHelper.generateUser();
			user.setRole("ADMIN");

			when(userRepository.save(user)).thenReturn(user);
			authenticateAs("ADMIN");

			var userSave = userService.create(user);

			assertThat(userSave).usingRecursiveAssertion().ignoringAllNullFields().isEqualTo(user);
			verify(userRepository).save(user);
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
			inOrder.verify(userValidator).validateUpdateEligibility(userFind, userToUpdate);
			inOrder.verify(userValidator).validate(userFind);
			inOrder.verify(userRepository).save(userFind);
		}

		@Test
		void shouldReactivateAnInactiveUserWhenUpdateSetsActiveTrue() {
			UUID externalId = UUID.randomUUID();
			var userFind = UserHelper.generateUser();
			userFind.setActive(false);

			var userToUpdate = UserHelper.generateUser();
			userToUpdate.setActive(true);

			when(userRepository.findByExternalId(externalId)).thenReturn(Optional.of(userFind));
			when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

			var userUpdated = userService.update(externalId, userToUpdate);

			assertThat(userUpdated.getActive()).isTrue();
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
	class FindByRoles {

		@Test
		void shouldReturnUsersMatchingGivenRoles() {
			var buyer = UserHelper.generateUser();
			buyer.setRole("BUYER");
			var warehouse = UserHelper.generateUser();
			warehouse.setRole("WAREHOUSE_CLERK");
			List<String> roles = List.of("BUYER", "WAREHOUSE_CLERK");

			when(userRepository.findByRoleIn(roles)).thenReturn(List.of(buyer, warehouse));

			var result = userService.findByRoles(roles);

			assertThat(result).containsExactlyInAnyOrder(buyer, warehouse);
			verify(userRepository).findByRoleIn(roles);
		}

		@Test
		void shouldReturnEmptyListWhenNoUsersMatchRoles() {
			List<String> roles = List.of("BUYER");

			when(userRepository.findByRoleIn(roles)).thenReturn(List.of());

			var result = userService.findByRoles(roles);

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class Search {

		@Test
		void shouldReturnUsersWhenSearchCriteriaIsProvided() {
			var user = UserHelper.generateUser();
			Pageable pageable = PageRequest.of(0, 10);
			Page<User> expectedPage = new PageImpl<>(List.of(user));

			when(userRepository.search(user.getDocument(), user.getName(), user.getEmail(), user.getRole(), pageable))
				.thenReturn(expectedPage);

			var userPage = userService.search(user.getDocument(), user.getName(), user.getEmail(), user.getRole(), pageable);

			assertThat(userPage).isEqualTo(expectedPage);
			verify(userRepository).search(user.getDocument(), user.getName(), user.getEmail(), user.getRole(), pageable);
		}
	}
}
