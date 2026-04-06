package io.github.alexistrejo11.bank.shared.openapi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Links a controller method to centralized OpenAPI metadata (see {@link BankApiKeys} and
 * {@code BankApiDocumentationRegistry} in bank-boot). Keeps controllers free of verbose
 * {@code @Operation} / {@code @ApiResponse} blocks.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BankApiOperation {

	/**
	 * Stable key into the documentation registry (e.g. {@link BankApiKeys#AUTH_LOGIN}).
	 */
	String value();
}
