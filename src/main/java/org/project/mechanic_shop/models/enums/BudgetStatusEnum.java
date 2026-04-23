package org.project.mechanic_shop.models.enums;

import lombok.Getter;

@Getter
public enum BudgetStatusEnum {

    OPEN("Open", "Budget is being drafted by the mechanic."),
    SENT("Sent", "Budget sent to the customer for approval."),
    APPROVED("Approved", "Customer approved the budget."),
    REJECTED("Rejected", "Customer rejected the budget.");

    private final String label;
    private final String description;

    BudgetStatusEnum(String label, String description) {
        this.label = label;
        this.description = description;
    }
}