package org.project.mechanic_shop.models.enums;

import lombok.Getter;

@Getter
public enum ServiceOrderStatusEnum {

    RECEIVED("Received", "Initial phase. Awaiting diagnosis."),
    DIAGNOSIS("In Diagnosis", "Mechanic is evaluating the vehicle and adding items."),
    PENDING_APPROVAL("Pending Approval", "Awaiting customer approval."),
    IN_PROGRESS("In Progress", "Mechanic is currently working on the vehicle."),
    COMPLETED("Completed", "Service finished. Awaiting customer pickup."),
    DELIVERED("Delivered", "Vehicle handed over to the customer. Process closed."),
    CANCELED("Canceled", "Service canceled (e.g., budget rejected). Final state.");

    private final String label;
    private final String description;

    ServiceOrderStatusEnum(String label, String description) {
        this.label = label;
        this.description = description;
    }
}