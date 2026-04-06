package io.github.alexistrejo11.bank.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankOpenApiConfiguration {

	public static final String JWT_SECURITY_SCHEME_NAME = "bearer-jwt";

	@Bean
	public OpenAPI bankOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Bank API")
						.version("1.0.0")
						.description(
								"Modular monolith: IAM, accounts (double-entry ledger), payments, loans, notifications, audit. "
										+ "API responses use the shared `ApiResponse` envelope unless noted."))
				.components(new Components().addSecuritySchemes(JWT_SECURITY_SCHEME_NAME,
						new SecurityScheme()
								.name(JWT_SECURITY_SCHEME_NAME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Obtain a token from POST /api/v1/auth/login or /api/v1/auth/register, then click Authorize and paste: Bearer &lt;token&gt;")));
	}
}
