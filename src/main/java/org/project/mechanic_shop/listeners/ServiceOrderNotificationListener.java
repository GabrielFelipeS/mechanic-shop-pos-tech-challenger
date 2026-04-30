package org.project.mechanic_shop.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.events.ServiceOrderStatusChangedEvent;
import org.project.mechanic_shop.models.ServiceOrder;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.services.EmailService;
import org.project.mechanic_shop.services.ServiceOrderService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceOrderNotificationListener {

	private final EmailService emailService;
	private final ServiceOrderService serviceOrderService;

	@Async
	@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleStatusChangedEvent(ServiceOrderStatusChangedEvent event) {
		ServiceOrder order = serviceOrderService.findByExternalId(event.serviceOrderExternalId());
		User customer = order.getVehicle().getOwner();
		User mechanic = order.getResponsibleMechanic();

		String customerEmail = (customer != null) ? customer.getEmail() : null;
		String mechanicEmail = (mechanic != null) ? mechanic.getEmail() : null;

		log.info("Triggering notifications for status change: {} -> {}", event.oldStatus(), event.newStatus());

		switch (event.newStatus()) {
			case RECEIVED -> sendEmail(mechanicEmail,
					"New Service Order Assigned",
					"A new service order (OS #" + order.getId() + ") has been assigned to you. Please review the details and prepare for the diagnosis.");
			case DIAGNOSIS -> sendEmail(
				customerEmail,
				"Service Update: Diagnosis Started",
				"Your vehicle is now being evaluated by our mechanics. We will send you the full quote soon."
			);
			case PENDING_APPROVAL -> sendEmail(
				customerEmail,
				"Action Required: Quote Pending Approval",
				"The diagnosis is complete! Please review and approve the quote in our system so we can start the repairs. " +"This Os code: " + order.getId()
			);
			case IN_PROGRESS -> sendEmail(
				mechanicEmail,
				"Task Approved: Start Repairs",
				"The customer has approved the quote for OS #" +
					order.getId() +
					". You can now proceed with the service."
			);
			case CANCELED -> {
				sendEmail(
					customerEmail,
					"Service Cancelled",
					"As requested, the service order has been closed. Please arrange to pick up your vehicle at your earliest convenience."
				);
				sendEmail(
					mechanicEmail,
					"Service Cancelled by Customer",
					"The quote for OS #" +
						order.getId() +
						" was rejected. The service is finalized and no further action is required."
				);
			}
			case COMPLETED -> sendEmail(
				customerEmail,
				"Service Completed! Your car is ready",
				"Great news! The maintenance of your vehicle is finished. You can come by to pick it up."
			);
			case DELIVERED -> sendEmail(
				customerEmail,
				"Thank you for choosing Mechanic Shop!",
				"Your vehicle has been successfully delivered. We appreciate your business and hope to see you for your next revision!"
			);
		}
	}

	private void sendEmail(String to, String subject, String body) {
		if (to != null) {
			emailService.sendEmail(new String[] { to }, subject, body);
		}
	}
}
