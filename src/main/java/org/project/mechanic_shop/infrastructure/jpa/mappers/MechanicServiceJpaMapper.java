package org.project.mechanic_shop.infrastructure.jpa.mappers;

import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;
import org.project.mechanic_shop.infrastructure.jpa.entities.MechanicServiceJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class MechanicServiceJpaMapper {

	public MechanicService toDomain(MechanicServiceJpaEntity jpa) {
		if (jpa == null) return null;
		MechanicService ms = new MechanicService();
		ms.setExternalId(jpa.getExternalId());
		ms.setCreatedAt(jpa.getCreatedAt());
		ms.setCreatedFor(jpa.getCreatedFor());
		ms.setLastUpdatedAt(jpa.getLastUpdatedAt());
		ms.setLastUpdatedFor(jpa.getLastUpdatedFor());
		ms.setId(jpa.getId());
		ms.setName(jpa.getName());
		ms.setDescription(jpa.getDescription());
		ms.setEstimatedTimeMinutes(jpa.getEstimatedTimeMinutes());
		ms.setPrice(jpa.getPrice());
		return ms;
	}

	public MechanicServiceJpaEntity toJpa(MechanicService ms) {
		if (ms == null) return null;
		MechanicServiceJpaEntity jpa = new MechanicServiceJpaEntity();
		jpa.setExternalId(ms.getExternalId());
		jpa.setCreatedAt(ms.getCreatedAt());
		jpa.setCreatedFor(ms.getCreatedFor());
		jpa.setLastUpdatedAt(ms.getLastUpdatedAt());
		jpa.setLastUpdatedFor(ms.getLastUpdatedFor());
		jpa.setId(ms.getId());
		jpa.setName(ms.getName());
		jpa.setDescription(ms.getDescription());
		jpa.setEstimatedTimeMinutes(ms.getEstimatedTimeMinutes());
		jpa.setPrice(ms.getPrice());
		return jpa;
	}
}
