package org.project.mechanic_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MechanicShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(MechanicShopApplication.class, args);
    }

}
