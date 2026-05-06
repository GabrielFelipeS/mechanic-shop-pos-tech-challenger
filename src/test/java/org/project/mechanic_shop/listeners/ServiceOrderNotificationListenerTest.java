package org.project.mechanic_shop.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.events.ServiceOrderStatusChangedEvent;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.Vehicle;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.models.enums.UserRoleEnum;
import org.project.mechanic_shop.services.EmailService;
import org.project.mechanic_shop.services.ServiceOrderService;

@ExtendWith(MockitoExtension.class)
class ServiceOrderNotificationListenerTest {

	private ServiceOrderNotificationListener listener;

	@Mock
	private EmailService emailService;

	@Mock
	private ServiceOrderService serviceOrderService;

	@BeforeEach
	void setup() {
		listener = new ServiceOrderNotificationListener(emailService, serviceOrderService);
	}

	@Test
	void shouldNotSendEmailWhenStatusIsReceived() {
		var externalId = UUID.randomUUID();
		when(serviceOrderService.findByExternalId(externalId)).thenReturn(buildOrder("customer@test.com", "mechanic@test.com"));

		listener.handleStatusChangedEvent(
			new ServiceOrderStatusChangedEvent(externalId, ServiceOrderStatusEnum.DIAGNOSIS, ServiceOrderStatusEnum.RECEIVED)
		);

		verifyNoInteractions(emailService);
	}

	@Test
	void shouldNotifyCustomerWhenDiagnosisStarts() {
		var externalId = UUID.randomUUID();
		when(serviceOrderService.findByExternalId(externalId)).thenReturn(buildOrder("customer@test.com", "mechanic@test.com"));

		listener.handleStatusChangedEvent(
			new ServiceOrderStatusChangedEvent(
				externalId,
				ServiceOrderStatusEnum.RECEIVED,
				ServiceOrderStatusEnum.DIAGNOSIS
			)
		);

		ArgumentCaptor<String[]> recipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

		verify(emailService).sendEmail(recipients.capture(), subject.capture(), body.capture());

		assertThat(recipients.getValue()).containsExactly("customer@test.com");
		assertThat(subject.getValue()).isEqualTo("Service Update: Diagnosis Started");
		assertThat(body.getValue()).contains("full quote soon");
	}

	@Test
	void shouldNotifyCustomerWhenApprovalIsRequested() {
		var externalId = UUID.randomUUID();
		var order = buildOrder("customer@test.com", "mechanic@test.com");
		when(serviceOrderService.findByExternalId(externalId)).thenReturn(order);

		listener.handleStatusChangedEvent(
			new ServiceOrderStatusChangedEvent(
				externalId,
				ServiceOrderStatusEnum.DIAGNOSIS,
				ServiceOrderStatusEnum.PENDING_APPROVAL
			)
		);

		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
		verify(emailService).sendEmail(org.mockito.ArgumentMatchers.any(String[].class), org.mockito.ArgumentMatchers.eq("Action Required: Quote Pending Approval"), body.capture());

		assertThat(body.getValue()).contains("This Os code: " + order.getId());
	}

	@Test
	void shouldSkipNotificationWhenMechanicEmailIsMissing() {
		var externalId = UUID.randomUUID();
		when(serviceOrderService.findByExternalId(externalId)).thenReturn(buildOrder("customer@test.com", null));

		listener.handleStatusChangedEvent(
			new ServiceOrderStatusChangedEvent(
				externalId,
				ServiceOrderStatusEnum.PENDING_APPROVAL,
				ServiceOrderStatusEnum.IN_PROGRESS
			)
		);

		verifyNoInteractions(emailService);
	}

	@Test
	void shouldNotifyCustomerAndMechanicWhenOrderIsCanceled() {
		var externalId = UUID.randomUUID();
		when(serviceOrderService.findByExternalId(externalId)).thenReturn(buildOrder("customer@test.com", "mechanic@test.com"));

		listener.handleStatusChangedEvent(
			new ServiceOrderStatusChangedEvent(
				externalId,
				ServiceOrderStatusEnum.PENDING_APPROVAL,
				ServiceOrderStatusEnum.CANCELED
			)
		);

		ArgumentCaptor<String[]> recipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

		verify(emailService, org.mockito.Mockito.times(2))
			.sendEmail(recipients.capture(), subject.capture(), body.capture());

		assertThat(recipients.getAllValues()).containsExactly(
			new String[] { "customer@test.com" },
			new String[] { "mechanic@test.com" }
		);
		assertThat(subject.getAllValues()).containsExactly(
			"Service Cancelled",
			"Service Cancelled by Customer"
		);
		assertThat(body.getAllValues().get(1)).contains("OS #1");
	}

	@Test
	void shouldNotifyCustomerWhenServiceIsCompleted() {
		var externalId = UUID.randomUUID();
		when(serviceOrderService.findByExternalId(externalId)).thenReturn(buildOrder("customer@test.com", "mechanic@test.com"));

		listener.handleStatusChangedEvent(
			new ServiceOrderStatusChangedEvent(
				externalId,
				ServiceOrderStatusEnum.IN_PROGRESS,
				ServiceOrderStatusEnum.COMPLETED
			)
		);

		ArgumentCaptor<String[]> recipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

		verify(emailService).sendEmail(recipients.capture(), subject.capture(), body.capture());

		assertThat(recipients.getValue()).containsExactly("customer@test.com");
		assertThat(subject.getValue()).isEqualTo("Service Completed! Your car is ready");
		assertThat(body.getValue()).contains("maintenance of your vehicle is finished");
	}

	@Test
	void shouldNotifyCustomerWhenVehicleIsDelivered() {
		var externalId = UUID.randomUUID();
		when(serviceOrderService.findByExternalId(externalId)).thenReturn(buildOrder("customer@test.com", "mechanic@test.com"));

		listener.handleStatusChangedEvent(
			new ServiceOrderStatusChangedEvent(
				externalId,
				ServiceOrderStatusEnum.COMPLETED,
				ServiceOrderStatusEnum.DELIVERED
			)
		);

		ArgumentCaptor<String[]> recipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

		verify(emailService).sendEmail(recipients.capture(), subject.capture(), body.capture());

		assertThat(recipients.getValue()).containsExactly("customer@test.com");
		assertThat(subject.getValue()).isEqualTo("Thank you for choosing Mechanic Shop!");
		assertThat(body.getValue()).contains("successfully delivered");
	}

	private ServiceOrder buildOrder(String customerEmail, String mechanicEmail) {
		User customer = new User();
		customer.setId(1L);
		customer.setExternalId(UUID.randomUUID());
		customer.setName("Cliente");
		customer.setDocument("57096255079");
		customer.setEmail(customerEmail);
		customer.setRole(UserRoleEnum.CUSTOMER.name());
		customer.setActive(true);
		customer.setPassword("Secret@123");

		User mechanic = null;
		if (mechanicEmail != null) {
			mechanic = new User();
			mechanic.setId(2L);
			mechanic.setExternalId(UUID.randomUUID());
			mechanic.setName("Mecanico");
			mechanic.setDocument("39053344705");
			mechanic.setEmail(mechanicEmail);
			mechanic.setRole(UserRoleEnum.MECHANIC.name());
			mechanic.setActive(true);
			mechanic.setPassword("Secret@123");
		}

		Vehicle vehicle = new Vehicle();
		vehicle.setId(1L);
		vehicle.setExternalId(UUID.randomUUID());
		vehicle.setLicensePlate("ABC1D23");
		vehicle.setOwner(customer);

		ServiceOrder order = new ServiceOrder();
		order.setId(1L);
		order.setExternalId(UUID.randomUUID());
		order.setVehicle(vehicle);
		order.setResponsibleMechanic(mechanic);

		return order;
	}
}
