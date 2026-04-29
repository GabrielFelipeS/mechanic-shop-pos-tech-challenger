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
import java.math.BigDecimal;
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
import org.project.mechanic_shop.models.MechanicService;
import org.project.mechanic_shop.repositories.MechanicServiceRepository;
import org.project.mechanic_shop.services.MechanicServiceService;
import org.project.mechanic_shop.utils.MechanicServiceHelper;
import org.project.mechanic_shop.validators.MechanicServiceValidator;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MechanicServiceServiceTest {

	private MechanicServiceService service;

	@Mock
	private MechanicServiceRepository repository;

	@Mock
	private MechanicServiceValidator validator;

	@BeforeEach
	void setup() {
		service = new MechanicServiceServiceImpl(repository, validator);
	}

	@Nested
	class Create {

		@Test
		void shouldCreateMechanicService() {
			var mechanicService = MechanicServiceHelper.generateMechanicServiceWithoutId();

			when(repository.save(mechanicService)).thenReturn(mechanicService);

			var saved = service.create(mechanicService);

			InOrder inOrder = inOrder(validator, repository);
			inOrder.verify(validator).validate(mechanicService);
			inOrder.verify(repository).save(mechanicService);

			assertThat(saved).usingRecursiveAssertion().ignoringAllNullFields().isEqualTo(mechanicService);
		}

		@Test
		void shouldNotSaveWhenValidationFails() {
			var mechanicService = MechanicServiceHelper.generateMechanicServiceWithoutId();

			doThrow(new IllegalArgumentException()).when(validator).validate(mechanicService);

			assertThatThrownBy(() -> service.create(mechanicService)).isInstanceOf(IllegalArgumentException.class);

			verify(repository, never()).save(any());
		}
	}

	@Nested
	class FindByExternalId {

		@Test
		void shouldFindMechanicServiceByExternalId() {
			UUID externalId = UUID.randomUUID();
			var mechanicService = MechanicServiceHelper.generateMechanicService();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(mechanicService));

			var found = service.findByExternalId(externalId);

			assertThat(found).usingRecursiveAssertion().isEqualTo(mechanicService);

			verify(repository).findByExternalId(externalId);
		}

		@Test
		void shouldThrowExceptionWhenServiceNotFound() {
			UUID externalId = UUID.randomUUID();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.findByExternalId(externalId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Service not found for External ID: " + externalId);
		}
	}

	@Nested
	class Update {

		@Test
		void shouldUpdateMechanicServiceWhenExists() {
			UUID externalId = UUID.randomUUID();
			var current = MechanicServiceHelper.generateMechanicService();
			var update = MechanicServiceHelper.generateMechanicService(
				null,
				"Alinhamento",
				"Atualizado",
				90,
				new BigDecimal("250.00")
			);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(current));
			when(repository.save(any(MechanicService.class))).thenAnswer(invocation -> invocation.getArgument(0));

			var updated = service.update(externalId, update);

			assertThat(updated.getName()).isEqualTo("Alinhamento");
			assertThat(updated.getDescription()).isEqualTo("Atualizado");
			assertThat(updated.getEstimatedTimeMinutes()).isEqualTo(90);
			assertThat(updated.getPrice()).isEqualByComparingTo("250.00");

			InOrder inOrder = inOrder(repository, validator, validator, repository);
			inOrder.verify(repository).findByExternalId(externalId);
			inOrder.verify(validator).validateUpdateEligibility(current);
			inOrder.verify(validator).validate(current);
			inOrder.verify(repository).save(current);
		}

		@Test
		void shouldThrowExceptionWhenServiceToUpdateNotFound() {
			UUID externalId = UUID.randomUUID();
			var mechanicService = new MechanicService();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.update(externalId, mechanicService))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Service not found with id: " + externalId);
		}

		@Test
		void shouldNotSaveWhenUpdateValidationFails() {
			UUID externalId = UUID.randomUUID();
			var current = MechanicServiceHelper.generateMechanicService();
			var update = MechanicServiceHelper.generateMechanicServiceWithoutId();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(current));
			doThrow(new IllegalArgumentException()).when(validator).validate(current);

			assertThatThrownBy(() -> service.update(externalId, update)).isInstanceOf(IllegalArgumentException.class);

			verify(repository, never()).save(any());
		}
	}

	@Nested
	class Search {

		@SuppressWarnings("unchecked")
		private ArgumentCaptor<Example<MechanicService>> exampleCaptor() {
			return (ArgumentCaptor<Example<MechanicService>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(
				Example.class
			);
		}

		@SuppressWarnings("unchecked")
		private Example<MechanicService> anyExample() {
			return any(Example.class);
		}

		@Test
		void shouldReturnMechanicServicesWhenSearchCriteriaIsProvided() {
			var mechanicService = MechanicServiceHelper.generateMechanicService();
			Pageable pageable = PageRequest.of(0, 10);
			Page<MechanicService> expectedPage = new PageImpl<>(List.of(mechanicService));
			ArgumentCaptor<Example<MechanicService>> captor = exampleCaptor();

			when(repository.findAll(anyExample(), eq(pageable))).thenReturn(expectedPage);

			var result = service.search(mechanicService.getName(), pageable);

			assertThat(result).isEqualTo(expectedPage);

			verify(repository).findAll(captor.capture(), eq(pageable));
			assertThat(captor.getValue().getProbe().getName()).isEqualTo(mechanicService.getName());
		}
	}
}
