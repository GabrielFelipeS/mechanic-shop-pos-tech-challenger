package org.project.mechanic_shop.infrastructure.jpa.repositories;

import java.util.Optional;
import java.util.UUID;
import org.project.mechanic_shop.infrastructure.jpa.entities.VehicleJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, Long> {
	Optional<VehicleJpaEntity> findByLicensePlate(String licensePlate);
	Optional<VehicleJpaEntity> findByExternalId(UUID externalId);
	boolean existsByLicensePlate(String licensePlate);

	@EntityGraph(attributePaths = "owner")
	@Query(
		"SELECT v FROM VehicleJpaEntity v " +
		"WHERE (:licensePlate IS NULL OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', CAST(:licensePlate AS string), '%'))) " +
		"AND (:brand IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', CAST(:brand AS string), '%'))) " +
		"AND (:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', CAST(:model AS string), '%'))) " +
		"AND (:ownerId IS NULL OR v.owner.id = :ownerId)"
	)
	Page<VehicleJpaEntity> search(
		@Param("licensePlate") String licensePlate,
		@Param("brand") String brand,
		@Param("model") String model,
		@Param("ownerId") Long ownerId,
		Pageable pageable
	);
}
