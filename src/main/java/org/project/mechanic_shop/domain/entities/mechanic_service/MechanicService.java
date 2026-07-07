package org.project.mechanic_shop.domain.entities.mechanic_service;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.base_audit_entity.BaseAuditEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MechanicService extends BaseAuditEntity {
	private Long id;
	private String name;
	private String description;
	private Integer estimatedTimeMinutes;
	private BigDecimal price;
}
