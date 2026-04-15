package org.project.mechanic_shop.utils;

import org.project.mechanic_shop.dto.customer_dto.CustomerManDto;
import org.project.mechanic_shop.models.Customer;

public class CustomerHelper {

    public static Customer generateCustomer() {
        return new Customer(
                1L,
                "57096255079",
                "TESTE",
                "teste@test.com",
                true,
                "Teste@123",
                "553470167400"
        );
    }

    public static CustomerManDto generateCustomerMenDto() {
        return generateCustomerMenDto(generateCustomer());
    }

    public static CustomerManDto generateCustomerMenDto(Customer customer) {
        return new CustomerManDto(
                customer.getDocument(),
                customer.getName(),
                customer.getEmail(),
                customer.getActive(),
                customer.getPassword(),
                customer.getPhone()
        );
    }


}
