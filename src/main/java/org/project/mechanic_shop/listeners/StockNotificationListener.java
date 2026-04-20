package org.project.mechanic_shop.listeners;

import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.events.OutOfStockEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockNotificationListener {

    @Async
    @EventListener
    public void handleOutOfStockEvent(OutOfStockEvent event) {
        var item = event.item();
        var missingQuantity = event.missingQuantity();

        log.warn("🔔 [EVENT RECEIVED] Alert processing for item: {}", item.getCode());
        log.info("=================================================");
        log.info("📧 [MOCK EMAIL DISPATCHER]");
        log.info("To: compras@mechanicshop.com");
        log.info("Subject: 🚨 URGENTE: Reposição Necessária - {}", item.getCode());
        log.info("Body: O item '{}' ({}) atingiu saldo zero. Precisamos de pelo menos {} unidade(s).",
                item.getName(), item.getType().getLabel(), missingQuantity);
        log.info("Status: SUCCESS (Console Print Only)");
        log.info("=================================================");
    }
}