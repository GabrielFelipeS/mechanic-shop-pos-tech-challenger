package org.project.mechanic_shop.application.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.events.NewServiceOrderEvent;
import org.project.mechanic_shop.domain.events.ServiceOrderStatusChangedEvent;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.application.ports.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceOrderNotificationListener {

	private final EmailService emailService;

	@Value("${app.base-url}")
	private String baseUrl;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleStatusChangedEvent(ServiceOrderStatusChangedEvent event) {
		ServiceOrder order = event.serviceOrder();
		User customer = order.getVehicle().getOwner();
		User mechanic = order.getResponsibleMechanic();

		String customerEmail = (customer != null) ? customer.getEmail() : null;
		String mechanicEmail = (mechanic != null) ? mechanic.getEmail() : null;

		log.info("Triggering notifications for status change: {} -> {}", event.oldStatus(), event.newStatus());

		switch (event.newStatus()) {
			case RECEIVED -> log.info("Service Order '{}' received. Awaiting diagnosis.", order.getExternalId());
			case DIAGNOSIS -> sendEmail(
					customerEmail,
					"Atualização do Serviço: Diagnóstico Iniciado",
					"Seu veículo já está sendo avaliado por nossos mecânicos. Enviaremos o orçamento completo em breve."
			);
			case PENDING_APPROVAL -> {
				String token = order.getApprovalToken();
				String approveUrl = baseUrl + "/api/service-orders/budget-approval?token=" + token + "&approved=true";
				String rejectUrl  = baseUrl + "/api/service-orders/budget-approval?token=" + token + "&approved=false";
				String body = "O diagnóstico do seu veículo foi concluído e o orçamento está pronto para sua análise.\n\n" +
						"OS: " + order.getExternalId() + "\n" +
						"Total do Orçamento: R$ " + order.getBudget().getTotalAmount() + "\n\n" +
						"Clique em um dos links abaixo para responder:\n\n" +
						"✅ APROVAR: " + approveUrl + "\n\n" +
						"❌ RECUSAR: " + rejectUrl + "\n\n" +
						"Atenção: cada link pode ser utilizado apenas uma vez.";
				sendEmail(customerEmail, "Ação Necessária: Orçamento Pendente de Aprovação", body);
			}
			case IN_PROGRESS -> sendEmail(
					mechanicEmail,
					"Tarefa Aprovada: Iniciar Reparos",
					"O cliente aprovou o orçamento da OS #" +
							order.getExternalId() +
							". Você já pode prosseguir com o serviço no veículo."
			);
			case CANCELED -> {
				sendEmail(
						customerEmail,
						"Serviço Cancelado",
						"Conforme solicitado, a ordem de serviço com o código '" + order.getExternalId() + "' foi encerrada. Por favor, providencie a retirada do seu veículo assim que possível."
				);
				sendEmail(
						mechanicEmail,
						"Serviço Cancelado pelo Cliente",
						"O orçamento da OS #" +
								order.getExternalId() +
								" foi rejeitado. O serviço foi finalizado e nenhuma ação adicional é necessária de sua parte."
				);
			}
			case COMPLETED -> sendEmail(
					customerEmail,
					"Serviço Concluído! Seu carro está pronto: " + order.getExternalId(),
					"Ótimas notícias! A manutenção do seu veículo foi finalizada. Você já pode vir retirá-lo em nossa oficina."
			);
			case DELIVERED -> sendEmail(
					customerEmail,
					"Obrigado por escolher a Mechanic Shop! Código: " + order.getExternalId(),
					"Seu veículo foi entregue com sucesso. Agradecemos a confiança em nosso trabalho e esperamos vê-lo em sua próxima revisão!"
			);
		}
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleNewOrderEvent(NewServiceOrderEvent event) {

		ServiceOrder order = event.serviceOrder();
		User mechanic = order.getResponsibleMechanic();

		if (mechanic != null && mechanic.getEmail() != null) {
			log.info("Triggering notifications for NEW Service Order. Assignee: {}", mechanic.getEmail());

			sendEmail(mechanic.getEmail(),
					"Nova Ordem de Serviço Atribuída",
					"Uma nova ordem de serviço (OS #" + order.getExternalId() + ") foi designada a você. Por favor, revise os detalhes do veículo e prepare-se para o diagnóstico.");
		}
	}

	private void sendEmail(String to, String subject, String body) {
		if (to != null) {
			emailService.sendEmail(new String[] { to }, subject, body);
		}
	}
}