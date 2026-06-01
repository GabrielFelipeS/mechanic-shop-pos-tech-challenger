package org.project.mechanic_shop.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.project.mechanic_shop.application.services.impl.VehicleServiceImpl;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.infrastructure.repositories.UserRepository;
import org.project.mechanic_shop.infrastructure.repositories.VehicleRepository;
import org.project.mechanic_shop.application.services.VehicleService;
import org.project.mechanic_shop.utils.UserHelper;
import org.project.mechanic_shop.application.validators.VehicleValidator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

	private VehicleService service;

	@Mock
	private VehicleRepository repository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private VehicleValidator validator;

	@BeforeEach
	void setup() {
		service = new VehicleServiceImpl(repository, userRepository, validator);
	}

	@Nested
	class Create {

		@Test
		void shouldCreateVehicleWithOwner() {
			UUID ownerId = UUID.randomUUID();
			var owner = UserHelper.generateUser();
			var vehicle = vehicle();

			when(userRepository.findByExternalId(ownerId)).thenReturn(Optional.of(owner));
			when(repository.save(vehicle)).thenReturn(vehicle);

			var saved = service.create(vehicle, ownerId);

			assertThat(saved.getOwner()).isSameAs(owner);

			InOrder inOrder = inOrder(userRepository, validator, repository);
			inOrder.verify(userRepository).findByExternalId(ownerId);
			inOrder.verify(validator).validate(vehicle);
			inOrder.verify(repository).save(vehicle);
		}

		@Test
		void shouldThrowExceptionWhenOwnerNotFoundOnCreate() {
			UUID ownerId = UUID.randomUUID();
			var vehicle = vehicle();

			when(userRepository.findByExternalId(ownerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.create(vehicle, ownerId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Owner not found for External ID: " + ownerId);

			verify(repository, never()).save(any());
		}
	}

	@Nested
	class FindByExternalId {

		@Test
		void shouldFindVehicleByExternalId() {
			UUID externalId = UUID.randomUUID();
			var vehicle = vehicle();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(vehicle));

			var found = service.findByExternalId(externalId);

			assertThat(found).usingRecursiveAssertion().isEqualTo(vehicle);
		}

		@Test
		void shouldThrowExceptionWhenVehicleNotFound() {
			UUID externalId = UUID.randomUUID();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.findByExternalId(externalId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Vehicle not found for External ID: " + externalId);
		}
	}

	@Nested
	class Search {

		@SuppressWarnings("unchecked")
		private ArgumentCaptor<Example<Vehicle>> exampleCaptor() {
			return (ArgumentCaptor<Example<Vehicle>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Example.class);
		}

		@SuppressWarnings("unchecked")
		private Example<Vehicle> anyExample() {
			return any(Example.class);
		}

		@Test
		void shouldReturnEmptyPageWhenOwnerFilterDoesNotExist() {
			UUID ownerId = UUID.randomUUID();
			Pageable pageable = PageRequest.of(0, 10);

			when(userRepository.findByExternalId(ownerId)).thenReturn(Optional.empty());

			var result = service.search("ABC1234", "Ford", "Ka", ownerId, pageable);

			assertThat(result).isEmpty();
			verify(repository, never()).findAll(anyExample(), eq(pageable));
		}

		@Test
		void shouldReturnVehiclesWhenSearchCriteriaIsProvided() {
			UUID ownerId = UUID.randomUUID();
			var owner = UserHelper.generateUser();
			var vehicle = vehicle();
			Pageable pageable = PageRequest.of(0, 10);
			Page<Vehicle> expectedPage = new PageImpl<>(List.of(vehicle));
			ArgumentCaptor<Example<Vehicle>> captor = exampleCaptor();

			when(userRepository.findByExternalId(ownerId)).thenReturn(Optional.of(owner));
			when(repository.findAll(anyExample(), eq(pageable))).thenReturn(expectedPage);

			var result = service.search(
				vehicle.getLicensePlate(),
				vehicle.getBrand(),
				vehicle.getModel(),
				ownerId,
				pageable
			);

			assertThat(result).isEqualTo(expectedPage);

			verify(repository).findAll(captor.capture(), eq(pageable));
			Vehicle probe = captor.getValue().getProbe();
			assertThat(probe.getLicensePlate()).isEqualTo(vehicle.getLicensePlate());
			assertThat(probe.getBrand()).isEqualTo(vehicle.getBrand());
			assertThat(probe.getModel()).isEqualTo(vehicle.getModel());
			assertThat(probe.getOwner()).isSameAs(owner);
		}
	}

	@Nested
	class Update {

		@Test
		void shouldUpdateVehicleAndOwnerWhenProvided() {
			UUID externalId = UUID.randomUUID();
			UUID ownerId = UUID.randomUUID();
			var current = vehicle();
			var newOwner = anotherUser();
			var update = vehicle();
			update.setLicensePlate("XYZ9876");
			update.setBrand("Chevrolet");
			update.setModel("Onix");
			update.setYear(2024);
			update.setColor("Preto");

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(current));
			when(userRepository.findByExternalId(ownerId)).thenReturn(Optional.of(newOwner));
			when(repository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

			var updated = service.update(externalId, update, ownerId);

			assertThat(updated.getOwner()).isSameAs(newOwner);
			assertThat(updated.getLicensePlate()).isEqualTo("XYZ9876");
			assertThat(updated.getBrand()).isEqualTo("Chevrolet");
			assertThat(updated.getModel()).isEqualTo("Onix");
			assertThat(updated.getYear()).isEqualTo(2024);
			assertThat(updated.getColor()).isEqualTo("Preto");

			InOrder inOrder = inOrder(repository, validator, userRepository, validator, repository);
			inOrder.verify(repository).findByExternalId(externalId);
			inOrder.verify(validator).validateUpdateEligibility(current);
			inOrder.verify(userRepository).findByExternalId(ownerId);
			inOrder.verify(validator).validate(current);
			inOrder.verify(repository).save(current);
		}

		@Test
		void shouldThrowExceptionWhenVehicleToUpdateNotFound() {
			UUID externalId = UUID.randomUUID();
			var vehicle = vehicle();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.update(externalId, vehicle, null))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Vehicle not found with id: " + externalId);
		}

		@Test
		void shouldThrowExceptionWhenNewOwnerNotFound() {
			UUID externalId = UUID.randomUUID();
			UUID ownerId = UUID.randomUUID();
			var current = vehicle();
			var vehicle = vehicle();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(current));
			when(userRepository.findByExternalId(ownerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.update(externalId, vehicle, ownerId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Owner not found for External ID: " + ownerId);

			verify(repository, never()).save(any());
		}

		@Test
		void shouldNotSaveWhenValidationFails() {
			UUID externalId = UUID.randomUUID();
			var current = vehicle();
			var vehicle = vehicle();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(current));
			doThrow(new IllegalArgumentException()).when(validator).validate(current);

			assertThatThrownBy(() -> service.update(externalId, vehicle, null)).isInstanceOf(
				IllegalArgumentException.class
			);

			verify(repository, never()).save(any());
		}
	}

	private Vehicle vehicle() {
		var vehicle = new Vehicle();
		vehicle.setId(1L);
		vehicle.setExternalId(UUID.randomUUID());
		vehicle.setLicensePlate("ABC1234");
		vehicle.setBrand("Ford");
		vehicle.setModel("Ka");
		vehicle.setYear(2020);
		vehicle.setColor("Branco");
		vehicle.setOwner(UserHelper.generateUser());
		return vehicle;
	}

	private User anotherUser() {
		var user = UserHelper.generateUser();
		user.setId(2L);
		user.setEmail("novo@teste.com");
		user.setDocument("05698702000");
		return user;
	}
}
