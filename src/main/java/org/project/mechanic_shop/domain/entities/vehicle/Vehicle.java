package org.project.mechanic_shop.domain.entities.vehicle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.base_audit_entity.BaseAuditEntity;
import org.project.mechanic_shop.domain.entities.user.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends BaseAuditEntity {
	private Long id;
	private String licensePlate;
	private String brand;
	private String model;
	private Integer year;
	private String color;
	private User owner;
}
