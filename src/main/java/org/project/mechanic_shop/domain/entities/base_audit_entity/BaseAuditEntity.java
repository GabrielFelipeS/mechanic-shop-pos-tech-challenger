package org.project.mechanic_shop.domain.entities.base_audit_entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseAuditEntity {
	private UUID externalId;
	private LocalDateTime createdAt;
	private String createdFor;
	private LocalDateTime lastUpdatedAt;
	private String lastUpdatedFor;
}
