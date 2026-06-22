package org.project.mechanic_shop.infrastructure.jpa.mappers;

import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.infrastructure.jpa.entities.VehicleJpaEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleJpaMapper {

	private final UserJpaMapper userJpaMapper;

	public Vehicle toDomain(VehicleJpaEntity jpa) {
		if (jpa == null) return null;
		Vehicle vehicle = new Vehicle();
		vehicle.setExternalId(jpa.getExternalId());
		vehicle.setCreatedAt(jpa.getCreatedAt());
		vehicle.setCreatedFor(jpa.getCreatedFor());
		vehicle.setLastUpdatedAt(jpa.getLastUpdatedAt());
		vehicle.setLastUpdatedFor(jpa.getLastUpdatedFor());
		vehicle.setId(jpa.getId());
		vehicle.setLicensePlate(jpa.getLicensePlate());
		vehicle.setBrand(jpa.getBrand());
		vehicle.setModel(jpa.getModel());
		vehicle.setYear(jpa.getYear());
		vehicle.setColor(jpa.getColor());
		vehicle.setOwner(userJpaMapper.toDomain(jpa.getOwner()));
		return vehicle;
	}

	public VehicleJpaEntity toJpa(Vehicle vehicle) {
		if (vehicle == null) return null;
		VehicleJpaEntity jpa = new VehicleJpaEntity();
		jpa.setExternalId(vehicle.getExternalId());
		jpa.setCreatedAt(vehicle.getCreatedAt());
		jpa.setCreatedFor(vehicle.getCreatedFor());
		jpa.setLastUpdatedAt(vehicle.getLastUpdatedAt());
		jpa.setLastUpdatedFor(vehicle.getLastUpdatedFor());
		jpa.setId(vehicle.getId());
		jpa.setLicensePlate(vehicle.getLicensePlate());
		jpa.setBrand(vehicle.getBrand());
		jpa.setModel(vehicle.getModel());
		jpa.setYear(vehicle.getYear());
		jpa.setColor(vehicle.getColor());
		jpa.setOwner(userJpaMapper.toJpa(vehicle.getOwner()));
		return jpa;
	}
}
