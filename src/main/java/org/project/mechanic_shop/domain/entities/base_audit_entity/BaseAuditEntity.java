package org.project.mechanic_shop.domain.entities.base_audit_entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

	@Column(nullable = false, updatable = false, unique = true)
	private UUID externalId;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column
	@CreatedBy
	private String createdFor;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime lastUpdatedAt;

	@Column
	@LastModifiedBy
	private String lastUpdatedFor;

	@PrePersist
	protected void onCreate() {
		if (this.externalId == null) {
			this.externalId = UUID.randomUUID();
		}
	}
}
