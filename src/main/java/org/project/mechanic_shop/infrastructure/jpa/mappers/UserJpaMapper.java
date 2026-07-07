package org.project.mechanic_shop.infrastructure.jpa.mappers;

import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.infrastructure.jpa.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserJpaMapper {

	public User toDomain(UserJpaEntity jpa) {
		if (jpa == null) return null;
		User user = new User();
		user.setExternalId(jpa.getExternalId());
		user.setCreatedAt(jpa.getCreatedAt());
		user.setCreatedFor(jpa.getCreatedFor());
		user.setLastUpdatedAt(jpa.getLastUpdatedAt());
		user.setLastUpdatedFor(jpa.getLastUpdatedFor());
		user.setId(jpa.getId());
		user.setDocument(jpa.getDocument());
		user.setName(jpa.getName());
		user.setEmail(jpa.getEmail());
		user.setRole(jpa.getRole());
		user.setActive(jpa.getActive());
		user.setPassword(jpa.getPassword());
		user.setPhone(jpa.getPhone());
		return user;
	}

	public UserJpaEntity toJpa(User user) {
		if (user == null) return null;
		UserJpaEntity jpa = new UserJpaEntity();
		jpa.setExternalId(user.getExternalId());
		jpa.setCreatedAt(user.getCreatedAt());
		jpa.setCreatedFor(user.getCreatedFor());
		jpa.setLastUpdatedAt(user.getLastUpdatedAt());
		jpa.setLastUpdatedFor(user.getLastUpdatedFor());
		jpa.setId(user.getId());
		jpa.setDocument(user.getDocument());
		jpa.setName(user.getName());
		jpa.setEmail(user.getEmail());
		jpa.setRole(user.getRole());
		jpa.setActive(user.getActive());
		jpa.setPassword(user.getPassword());
		jpa.setPhone(user.getPhone());
		return jpa;
	}
}
