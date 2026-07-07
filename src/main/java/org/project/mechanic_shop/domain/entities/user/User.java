package org.project.mechanic_shop.domain.entities.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.base_audit_entity.BaseAuditEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditEntity {
	private Long id;
	private String document;
	private String name;
	private String email;
	private String role;
	private Boolean active;
	private String password;
	private String phone;
}
