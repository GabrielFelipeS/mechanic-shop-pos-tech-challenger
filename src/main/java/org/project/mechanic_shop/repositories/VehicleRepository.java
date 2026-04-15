package org.project.mechanic_shop.repositories;

import org.project.mechanic_shop.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    Optional<Vehicle> findByExternalId(UUID externalId);

    boolean existsByLicensePlate(String licensePlate);
}