package org.project.postechchallengemechanicshop.mappers;

import org.mapstruct.Mapper;
import org.project.postechchallengemechanicshop.dto.customerDto.CustomerDto;
import org.project.postechchallengemechanicshop.dto.customerDto.CustomerManDto;
import org.project.postechchallengemechanicshop.dto.customerDto.CustomerShortDto;
import org.project.postechchallengemechanicshop.models.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(CustomerManDto customerManDto);

    CustomerShortDto toShortDto(Customer customer);

    CustomerDto toDto(Customer customer);

}
