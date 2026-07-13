package org.project.mechanic_shop.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.mechanic_shop.application.listeners.ServiceOrderNotificationListener;
import org.project.mechanic_shop.application.ports.EmailService;
import org.project.mechanic_shop.domain.entities.budget.Budget;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.domain.enums.BudgetStatusEnum;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.domain.events.NewServiceOrderEvent;
import org.project.mechanic_shop.domain.events.ServiceOrderStatusChangedEvent;
import org.springframework.test.util.ReflectionTestUtils;

class ServiceOrderNotificationListenerTest {

	private final EmailService emailService = mock(EmailService.class);
	private ServiceOrderNotificationListener listener;

	@BeforeEach
	void setup() {
		listener = new ServiceOrderNotificationListener(emailService);
		ReflectionTestUtils.setField(listener, "baseUrl", "http://localhost:8080");
	}

	@Test
	void shouldBuildApprovalEmailFromEventSnapshotWithoutTouchingTheDatabase() {
		ServiceOrder order = buildOrder();
		order.setApprovalToken("token-abc-123");
		var event = new ServiceOrderStatusChangedEvent(
			order,
			ServiceOrderStatusEnum.DIAGNOSIS,
			ServiceOrderStatusEnum.PENDING_APPROVAL
		);

		listener.handleStatusChangedEvent(event);

		verify(emailService).sendEmail(
			eq(new String[] { "customer@test.com" }),
			eq("Ação Necessária: Orçamento Pendente de Aprovação"),
			contains("token=token-abc-123")
		);
	}

	@Test
	void shouldNotifyMechanicWhenBudgetIsApproved() {
		ServiceOrder order = buildOrder();
		var event = new ServiceOrderStatusChangedEvent(
			order,
			ServiceOrderStatusEnum.PENDING_APPROVAL,
			ServiceOrderStatusEnum.IN_PROGRESS
		);

		listener.handleStatusChangedEvent(event);

		verify(emailService).sendEmail(
			eq(new String[] { "mechanic@test.com" }),
			eq("Tarefa Aprovada: Iniciar Reparos"),
			any()
		);
	}

	@Test
	void shouldNotifyMechanicWhenNewOrderIsAssigned() {
		ServiceOrder order = buildOrder();
		var event = new NewServiceOrderEvent(order);

		listener.handleNewOrderEvent(event);

		verify(emailService).sendEmail(
			eq(new String[] { "mechanic@test.com" }),
			eq("Nova Ordem de Serviço Atribuída"),
			any()
		);
	}

	@Test
	void shouldNotSendAssignmentEmailWhenNoMechanicIsResponsible() {
		ServiceOrder order = buildOrder();
		order.setResponsibleMechanic(null);
		var event = new NewServiceOrderEvent(order);

		listener.handleNewOrderEvent(event);

		verify(emailService, never()).sendEmail(any(), any(), any());
	}

	private ServiceOrder buildOrder() {
		User customer = new User();
		customer.setExternalId(UUID.randomUUID());
		customer.setEmail("customer@test.com");
		customer.setName("Cliente Teste");

		User mechanic = new User();
		mechanic.setExternalId(UUID.randomUUID());
		mechanic.setEmail("mechanic@test.com");
		mechanic.setName("Mecanico Teste");

		Vehicle vehicle = new Vehicle();
		vehicle.setExternalId(UUID.randomUUID());
		vehicle.setLicensePlate("ABC1D23");
		vehicle.setOwner(customer);

		Budget budget = new Budget();
		budget.setExternalId(UUID.randomUUID());
		budget.setStatus(BudgetStatusEnum.SENT);
		budget.setTotalAmount(new BigDecimal("170.00"));

		ServiceOrder order = new ServiceOrder();
		order.setExternalId(UUID.randomUUID());
		order.setVehicle(vehicle);
		order.setResponsibleMechanic(mechanic);
		order.setBudget(budget);
		return order;
	}
}
