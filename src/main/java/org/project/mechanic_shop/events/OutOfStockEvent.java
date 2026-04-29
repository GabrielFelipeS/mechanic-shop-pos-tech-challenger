package org.project.mechanic_shop.events;

import org.project.mechanic_shop.models.StockItem;

public record OutOfStockEvent(StockItem item, Integer missingQuantity) {}
