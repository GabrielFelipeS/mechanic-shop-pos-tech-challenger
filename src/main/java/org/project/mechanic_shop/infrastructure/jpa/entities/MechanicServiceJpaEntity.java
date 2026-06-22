package org.project.mechanic_shop.infrastructure.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.infrastructure.jpa.entities.base.BaseAuditJpaEntity;

@Entity
@Table(name = "mechanic_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MechanicServiceJpaEntity extends BaseAuditJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "estimated_time_minutes", nullable = false)
	private Integer estimatedTimeMinutes;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;
}
