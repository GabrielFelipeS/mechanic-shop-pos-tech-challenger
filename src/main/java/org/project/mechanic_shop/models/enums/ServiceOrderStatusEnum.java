package org.project.mechanic_shop.models.enums;

import lombok.Getter;

@Getter
public enum ServiceOrderStatusEnum {

    DRAFT("Draft", "Initial phase. Awaiting diagnosis."),
    DIAGNOSIS("In Diagnosis", "Mechanic is evaluating the vehicle and adding items."),
    PENDING_APPROVAL("Pending Approval", "Awaiting customer approval."),
    APPROVED("Approved", "Customer approved. Awaiting mechanic to start."),
    REJECTED("Rejected", "Customer rejected the service order. Final state."),
    IN_PROGRESS("In Progress", "Mechanic is currently working on the vehicle."),
    COMPLETED("Completed", "Service finished. Final state.");

    private final String label;
    private final String description;

    ServiceOrderStatusEnum(String label, String description) {
        this.label = label;
        this.description = description;
    }
}