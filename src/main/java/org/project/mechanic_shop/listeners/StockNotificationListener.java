package org.project.mechanic_shop.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.events.OutOfStockEvent;
import org.project.mechanic_shop.services.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockNotificationListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleOutOfStockEvent(OutOfStockEvent event) {



        var item = event.item();
        var missingQuantity = event.missingQuantity();

        log.warn("[EVENT RECEIVED] Alert processing for item: {}", item.getCode());

        String to = "compras@mechanicshop.com";
        String subject = "URGENTE: Reposição Necessária - " + item.getCode();
        String body = String.format("O item '%s' (%s) atingiu saldo zero. Precisamos de pelo menos %d unidade(s).",
                item.getName(), item.getType(), missingQuantity);

        emailService.sendEmail(to, subject, body);
    }
}