package org.project.postechchallengemechanicshop.repositories;

import org.project.postechchallengemechanicshop.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByDocument(String document);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByExternalId(UUID externalId);
}
