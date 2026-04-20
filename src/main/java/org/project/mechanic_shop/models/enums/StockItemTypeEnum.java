package org.project.mechanic_shop.models.enums;

import lombok.Getter;

@Getter
public enum StockItemTypeEnum {

    PART("Part", "Replacement part applied directly to the vehicle."),
    CONSUMABLE("Consumable", "Consumable material used by the shop during the execution of a service.");

    private final String label;
    private final String description;

    StockItemTypeEnum(String label, String description) {
        this.label = label;
        this.description = description;
    }
}