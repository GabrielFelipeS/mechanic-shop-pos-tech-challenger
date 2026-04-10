package org.project.postechchallengemechanicshop.services;

import org.project.postechchallengemechanicshop.models.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

public interface CustomerService {

    Customer create(Customer obj);

    Customer findByExternalId(UUID externalId);

    Page<Customer> search(String document,
                          String name,
                          String email,
                          Pageable pageable);

    Customer update(UUID id, Customer update);
}