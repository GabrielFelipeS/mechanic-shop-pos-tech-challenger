package org.project.mechanic_shop.infrastructure.jpa.entities.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseAuditJpaEntity {

	@Column(nullable = false, updatable = false, unique = true)
	private UUID externalId;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column
	private String createdFor;

	@Column(nullable = false)
	private LocalDateTime lastUpdatedAt;

	@Column
	private String lastUpdatedFor;

	@PrePersist
	protected void onCreate() {
		if (this.externalId == null) {
			this.externalId = UUID.randomUUID();
		}
		LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
		this.createdAt = now;
		this.lastUpdatedAt = now;
		String auditor = resolveCurrentAuditor();
		this.createdFor = auditor;
		this.lastUpdatedFor = auditor;
	}

	@PreUpdate
	protected void onUpdate() {
		this.lastUpdatedAt = LocalDateTime.now(ZoneId.systemDefault());
		this.lastUpdatedFor = resolveCurrentAuditor();
	}

	private String resolveCurrentAuditor() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) return "SYSTEM";
		var principal = auth.getPrincipal();
		if (principal instanceof UserDetails ud) return ud.getUsername();
		return principal != null ? principal.toString() : "SYSTEM";
	}
}
