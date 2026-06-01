package org.project.mechanic_shop.domain.events;

import org.project.mechanic_shop.domain.entities.stock_item.StockItem;

public record OutOfStockEvent(StockItem item, Integer missingQuantity) {}
