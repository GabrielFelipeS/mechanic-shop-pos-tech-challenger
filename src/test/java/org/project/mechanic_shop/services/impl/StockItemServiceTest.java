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
import org.project.mechanic_shop.events.OutOfStockEvent;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;
import org.project.mechanic_shop.repositories.StockItemRepository;
import org.project.mechanic_shop.services.StockItemService;
import org.project.mechanic_shop.validators.StockItemValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StockItemServiceTest {

	private StockItemService service;

	@Mock
	private StockItemRepository repository;

	@Mock
	private StockItemValidator validator;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@BeforeEach
	void setup() {
		service = new StockItemServiceImpl(repository, validator, eventPublisher);
	}

	@Nested
	class Create {

		@Test
		void shouldCreateStockItem() {
			var item = stockItem();

			when(repository.save(item)).thenReturn(item);

			var saved = service.create(item);

			InOrder inOrder = inOrder(validator, repository);
			inOrder.verify(validator).validate(item);
			inOrder.verify(repository).save(item);

			assertThat(saved).usingRecursiveAssertion().ignoringAllNullFields().isEqualTo(item);
		}

		@Test
		void shouldNotSaveWhenValidationFails() {
			var item = stockItem();

			doThrow(new IllegalArgumentException()).when(validator).validate(item);

			assertThatThrownBy(() -> service.create(item)).isInstanceOf(IllegalArgumentException.class);

			verify(repository, never()).save(any());
		}
	}

	@Nested
	class FindByExternalId {

		@Test
		void shouldFindStockItemByExternalId() {
			UUID externalId = UUID.randomUUID();
			var item = stockItem();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(item));

			var found = service.findByExternalId(externalId);

			assertThat(found).usingRecursiveAssertion().isEqualTo(item);

			verify(repository).findByExternalId(externalId);
		}

		@Test
		void shouldThrowExceptionWhenStockItemNotFound() {
			UUID externalId = UUID.randomUUID();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.findByExternalId(externalId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Part not found for External ID: " + externalId);
		}
	}

	@Nested
	class Search {

		@SuppressWarnings("unchecked")
		private ArgumentCaptor<Example<StockItem>> exampleCaptor() {
			return (ArgumentCaptor<Example<StockItem>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Example.class);
		}

		@SuppressWarnings("unchecked")
		private Example<StockItem> anyExample() {
			return any(Example.class);
		}

		@Test
		void shouldReturnStockItemsWhenSearchCriteriaIsProvided() {
			var item = stockItem();
			Pageable pageable = PageRequest.of(0, 10);
			Page<StockItem> expectedPage = new PageImpl<>(List.of(item));
			ArgumentCaptor<Example<StockItem>> captor = exampleCaptor();

			when(repository.findAll(anyExample(), eq(pageable))).thenReturn(expectedPage);

			var result = service.search(item.getCode(), item.getName(), pageable);

			assertThat(result).isEqualTo(expectedPage);

			verify(repository).findAll(captor.capture(), eq(pageable));
			StockItem probe = captor.getValue().getProbe();
			assertThat(probe.getCode()).isEqualTo(item.getCode());
			assertThat(probe.getName()).isEqualTo(item.getName());
		}
	}

	@Nested
	class Update {

		@Test
		void shouldUpdateStockItemWhenExists() {
			UUID externalId = UUID.randomUUID();
			var current = stockItem();
			var update = stockItem();
			update.setCode("BRK-200");
			update.setName("Pastilha Premium");
			update.setDescription("Nova descricao");
			update.setQuantity(9);
			update.setCostPrice(new BigDecimal("30.00"));
			update.setSalePrice(new BigDecimal("60.00"));

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(current));
			when(repository.save(any(StockItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

			var updated = service.update(externalId, update);

			assertThat(updated.getCode()).isEqualTo("BRK-200");
			assertThat(updated.getName()).isEqualTo("Pastilha Premium");
			assertThat(updated.getDescription()).isEqualTo("Nova descricao");
			assertThat(updated.getQuantity()).isEqualTo(9);
			assertThat(updated.getCostPrice()).isEqualByComparingTo("30.00");
			assertThat(updated.getSalePrice()).isEqualByComparingTo("60.00");

			InOrder inOrder = inOrder(repository, validator, validator, repository);
			inOrder.verify(repository).findByExternalId(externalId);
			inOrder.verify(validator).validateUpdateEligibility(current);
			inOrder.verify(validator).validate(current);
			inOrder.verify(repository).save(current);
		}

		@Test
		void shouldThrowExceptionWhenStockItemToUpdateNotFound() {
			UUID externalId = UUID.randomUUID();
			var current = stockItem();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.update(externalId, current))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("Part not found with id: " + externalId);
		}

		@Test
		void shouldNotSaveWhenUpdateValidationFails() {
			UUID externalId = UUID.randomUUID();
			var current = stockItem();
			var newStockItem = stockItem();

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(current));
			doThrow(new IllegalArgumentException()).when(validator).validate(current);

			assertThatThrownBy(() -> service.update(externalId, newStockItem)).isInstanceOf(
				IllegalArgumentException.class
			);

			verify(repository, never()).save(any());
		}
	}

	@Nested
	class WithdrawStock {

		@Test
		void shouldReduceQuantityWhenStockIsEnough() {
			UUID externalId = UUID.randomUUID();
			var item = stockItem();
			item.setQuantity(10);
			item.setPendingDemand(2);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(item));

			service.withdrawStock(externalId, 4);

			assertThat(item.getQuantity()).isEqualTo(6);
			assertThat(item.getPendingDemand()).isEqualTo(2);

			verify(repository).save(item);
			verify(eventPublisher, never()).publishEvent(any());
		}

		@Test
		void shouldPublishOutOfStockEventWhenStockIsInsufficient() {
			UUID externalId = UUID.randomUUID();
			var item = stockItem();
			item.setQuantity(3);
			item.setPendingDemand(1);
			ArgumentCaptor<OutOfStockEvent> eventCaptor = ArgumentCaptor.forClass(OutOfStockEvent.class);

			when(repository.findByExternalId(externalId)).thenReturn(Optional.of(item));

			service.withdrawStock(externalId, 5);

			assertThat(item.getQuantity()).isZero();
			assertThat(item.getPendingDemand()).isEqualTo(3);

			verify(repository).save(item);
			verify(eventPublisher).publishEvent(eventCaptor.capture());

			OutOfStockEvent event = eventCaptor.getValue();
			assertThat(event.item()).isSameAs(item);
			assertThat(event.missingQuantity()).isEqualTo(3);
		}
	}

	private StockItem stockItem() {
		var item = new StockItem();
		item.setId(1L);
		item.setExternalId(UUID.randomUUID());
		item.setCode("BRK-100");
		item.setName("Pastilha");
		item.setType(StockItemTypeEnum.PART);
		item.setDescription("Pastilha de freio");
		item.setQuantity(12);
		item.setPendingDemand(0);
		item.setCostPrice(new BigDecimal("20.00"));
		item.setSalePrice(new BigDecimal("45.00"));
		return item;
	}
}
