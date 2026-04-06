package io.github.alexistrejo11.bank.openapi;

/**
 * Centralized OpenAPI text for a single operation (linked from controllers via {@code @BankApiOperation}).
 *
 * @param bearerAuthRequired when {@code true}, the operation lists the JWT security scheme in OpenAPI (Authorize in Swagger UI).
 */
public record BankApiOperationDoc(String summary, String description, String tag, boolean bearerAuthRequired) {

	public BankApiOperationDoc(String summary, String description, String tag) {
		this(summary, description, tag, true);
	}
}
