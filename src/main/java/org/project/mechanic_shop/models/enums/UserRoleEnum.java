package org.project.mechanic_shop.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    WAREHOUSE_CLERK("Warehouse Clerk", "Responsible for managing inventory and parts."),

    RECEPTIONIST("Receptionist", "Handles customer check-ins, scheduling, and general inquiries."),

    MECHANIC("Mechanic", "Performs vehicle maintenance, inspections, and repairs."),

    SALESPERSON("Salesperson", "Responsible for selling services, parts, and interacting with clients."),

    CUSTOMER("Customer", "Client of the mechanic shop."),

    ADMIN("Admin", "System administrator with full access to all modules.");

    private final String label;
    private final String description;
}