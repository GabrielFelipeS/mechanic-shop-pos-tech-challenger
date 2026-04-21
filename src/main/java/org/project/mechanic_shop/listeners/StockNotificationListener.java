package org.project.mechanic_shop.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.events.OutOfStockEvent;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.models.enums.UserRoleEnum; // Importação corrigida para o seu Enum
import org.project.mechanic_shop.repositories.UserRepository;
import org.project.mechanic_shop.services.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

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

        String subject = "URGENT: Restock Required - " + item.getCode();
        String body = String.format("O item '%s' (%s) atingiu saldo zero. Precisamos de pelo menos %d unidade(s).",
                item.getName(), item.getType(), missingQuantity);

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