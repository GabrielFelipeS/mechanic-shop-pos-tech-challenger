package org.project.mechanic_shop.shared.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
	info = @Info(
		title = "Mechanic Shop API",
		version = "v1",
		description = """
			REST API for managing a mechanic shop: service orders, vehicles, stock, users and service catalog.

			## Seed users (password: `123456` for all)
			| E-mail | Role |
			|---|---|
			| admin@shop.com | ADMIN |
			| receptionist@shop.com | RECEPTIONIST |
			| mechanic@shop.com | MECHANIC |
			| warehouse@shop.com | WAREHOUSE |
			| buyer@shop.com | BUYER |

			> The `CUSTOMER` role is not seeded — a RECEPTIONIST must create it via `POST /api/v1/users`.
			""",
		contact = @Contact(name = "Eddie Marley", email = "marley_eddie@hotmail.com")
	),
	security = { @SecurityRequirement(name = "bearerAuth") }
)
@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT",
	in = SecuritySchemeIn.HEADER
)
public class OpenApiConfiguration {}
