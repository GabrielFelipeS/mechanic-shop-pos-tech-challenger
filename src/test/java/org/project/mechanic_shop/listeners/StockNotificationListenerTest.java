package org.project.mechanic_shop.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.events.OutOfStockEvent;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;
import org.project.mechanic_shop.models.enums.UserRoleEnum;
import org.project.mechanic_shop.repositories.UserRepository;
import org.project.mechanic_shop.services.EmailService;

@ExtendWith(MockitoExtension.class)
class StockNotificationListenerTest {

	private StockNotificationListener listener;

	@Mock
	private EmailService emailService;

	@Mock
	private UserRepository userRepository;

	@BeforeEach
	void setup() {
		listener = new StockNotificationListener(emailService, userRepository);
	}

	@Test
	void shouldNotifyWarehouseClerkAndBuyer() {
		var item = buildStockItem();
		var warehouseClerk = buildUser("warehouse@test.com", UserRoleEnum.WAREHOUSE_CLERK);
		var buyer = buildUser("buyer@test.com", UserRoleEnum.BUYER);

		when(userRepository.findByRoleIn(List.of(UserRoleEnum.WAREHOUSE_CLERK.name(), UserRoleEnum.BUYER.name())))
			.thenReturn(List.of(warehouseClerk, buyer));

		listener.handleOutOfStockEvent(new OutOfStockEvent(item, 4));

		ArgumentCaptor<String[]> recipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

		verify(emailService).sendEmail(recipients.capture(), subject.capture(), body.capture());

		assertThat(recipients.getValue()).containsExactly("warehouse@test.com", "buyer@test.com");
		assertThat(subject.getValue()).isEqualTo("URGENT: Restock Required - P-100");
		assertThat(body.getValue()).contains("Filtro de oleo", "PART", "4 unidade(s)");
	}

	@Test
	void shouldNotSendEmailWhenNoUsersCanReceiveStockAlert() {
		var item = buildStockItem();

		when(userRepository.findByRoleIn(List.of(UserRoleEnum.WAREHOUSE_CLERK.name(), UserRoleEnum.BUYER.name())))
			.thenReturn(List.of());

		listener.handleOutOfStockEvent(new OutOfStockEvent(item, 2));

		verifyNoInteractions(emailService);
	}

	private StockItem buildStockItem() {
		StockItem item = new StockItem();
		item.setId(1L);
		item.setExternalId(UUID.randomUUID());
		item.setCode("P-100");
		item.setName("Filtro de oleo");
		item.setType(StockItemTypeEnum.PART);
		item.setQuantity(0);
		item.setCostPrice(new BigDecimal("10.00"));
		item.setSalePrice(new BigDecimal("20.00"));
		return item;
	}

	private User buildUser(String email, UserRoleEnum role) {
		User user = new User();
		user.setId(1L);
		user.setExternalId(UUID.randomUUID());
		user.setDocument(UUID.randomUUID().toString().substring(0, 11));
		user.setName(role.getLabel());
		user.setEmail(email);
		user.setRole(role.name());
		user.setActive(true);
		user.setPassword("Secret@123");
		return user;
	}
}
