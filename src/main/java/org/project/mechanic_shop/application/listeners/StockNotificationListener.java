package org.project.mechanic_shop.application.listeners;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.events.OutOfStockEvent;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.UserRoleEnum; // Importação corrigida para o seu Enum
import org.project.mechanic_shop.infrastructure.repositories.UserRepository;
import org.project.mechanic_shop.infrastructure.http.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockNotificationListener {

	private final EmailService emailService;
	private final UserRepository userRepository;

	@Async
	@EventListener
	public void handleOutOfStockEvent(OutOfStockEvent event) {
		var item = event.item();
		var missingQuantity = event.missingQuantity();

		log.warn("[EVENT RECEIVED] Alert processing for item: {}", item.getCode());

		String subject = "URGENTE: Reposição de Estoque Necessária -" + item.getCode();
		String body = String.format(
				"Atenção equipe! O item '%s' (%s) atingiu saldo zero no sistema."+"\n" +"Precisamos de pelo menos %d unidade(s) para atender as ordens de serviço pendentes. Por favor, providenciem a compra/reposição imediatamente.",
			item.getName(),
			item.getType(),
			missingQuantity
		);

		List<String> targetRoles = List.of(UserRoleEnum.WAREHOUSE_CLERK.name(), UserRoleEnum.BUYER.name());

		List<User> notificationTargets = userRepository.findByRoleIn(targetRoles);

		if (notificationTargets.isEmpty()) {
			log.error("No users found with roles {} to receive the stock alert!", targetRoles);
			return;
		}

		String[] recipientEmails = notificationTargets.stream().map(User::getEmail).toArray(String[]::new);
		log.info("Sending bulk stock notification to {} users", recipientEmails.length);
		emailService.sendEmail(recipientEmails, subject, body);
	}
}
