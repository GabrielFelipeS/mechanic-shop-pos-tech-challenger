package org.project.mechanic_shop.mappers;

import org.mapstruct.Mapper;
import org.project.mechanic_shop.dto.customer_dto.CustomerDto;
import org.project.mechanic_shop.dto.customer_dto.CustomerManDto;
import org.project.mechanic_shop.dto.customer_dto.CustomerShortDto;
import org.project.mechanic_shop.models.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(CustomerManDto customerManDto);

    CustomerShortDto toShortDto(Customer customer);

    CustomerDto toDto(Customer customer);

}
