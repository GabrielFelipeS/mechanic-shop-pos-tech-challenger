package org.project.postechchallengemechanicshop.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.postechchallengemechanicshop.common.responses.ApiResponse;
import org.project.postechchallengemechanicshop.dto.customerDto.CustomerDto;
import org.project.postechchallengemechanicshop.dto.customerDto.CustomerManDto;
import org.project.postechchallengemechanicshop.mappers.CustomerMapper;
import org.project.postechchallengemechanicshop.models.Customer;
import org.project.postechchallengemechanicshop.services.CustomerService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management")
@Slf4j
public class CustomerController {

    private final CustomerService service;
    private final CustomerMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
        log.info("Find customer by External ID: {}", id);

        var customer = service.findByExternalId(id);
        var dto = mapper.toDto(customer);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                "success",
                dto
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody @Valid CustomerManDto dto) {
        log.info("Try create customer with parameters: {}", dto);

        Customer customer = mapper.toEntity(dto);

        var customerCreated = service.create(customer);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                HttpStatus.CREATED.value(),
                "success",
                customerCreated.getExternalId()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable UUID id,
                                              @RequestBody @Valid CustomerManDto dto) {
        log.info("Try update customer {} with parameters: {}", id, dto);

        var customerToUpdate =  mapper.toEntity(dto);

        var updatedCustomer = service.update(id, customerToUpdate);

        CustomerDto customerDto = mapper.toDto(updatedCustomer);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                "success",
                customerDto
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(name = "document", required = false) String document,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email,
            @ParameterObject @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Search customers with filters");

        Page<Customer> customers = service.search(document, name, email, pageable);

        var listDto = customers.map(mapper::toShortDto);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                "success",
                listDto
        ));
    }
}