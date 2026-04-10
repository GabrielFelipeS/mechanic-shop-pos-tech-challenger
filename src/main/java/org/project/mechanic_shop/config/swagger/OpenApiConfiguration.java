package org.project.mechanic_shop.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Qms Vision API",
                version = "v1",
                contact = @Contact(
                        name = "Eddie Marley",
                        email = "marley_eddie@hotmail.com"
                )
        )
)
public class OpenApiConfiguration {


}
