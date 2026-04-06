package io.github.alexistrejo11.bank.openapi;

import io.github.alexistrejo11.bank.shared.openapi.BankApiOperation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.List;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Applies {@link BankApiOperation} keys to Springdoc operations using {@link BankApiDocumentationRegistry}.
 */
@Component
public class BankOpenApiOperationCustomizer implements OperationCustomizer {

	private final BankApiDocumentationRegistry registry;

	public BankOpenApiOperationCustomizer(BankApiDocumentationRegistry registry) {
		this.registry = registry;
	}

	@Override
	public Operation customize(Operation operation, HandlerMethod handlerMethod) {
		BankApiOperation ref = handlerMethod.getMethodAnnotation(BankApiOperation.class);
		if (ref == null) {
			return operation;
		}
		return registry.find(ref.value())
				.map(doc -> merge(operation, doc))
				.orElse(operation);
	}

	private static Operation merge(Operation operation, BankApiOperationDoc doc) {
		if (doc.summary() != null && !doc.summary().isBlank()) {
			operation.setSummary(doc.summary());
		}
		if (doc.description() != null && !doc.description().isBlank()) {
			operation.setDescription(doc.description());
		}
		if (doc.tag() != null && !doc.tag().isBlank()) {
			operation.setTags(List.of(doc.tag()));
		}
		if (doc.bearerAuthRequired()) {
			operation.addSecurityItem(new SecurityRequirement().addList(BankOpenApiConfiguration.JWT_SECURITY_SCHEME_NAME));
		}
		else {
			operation.setSecurity(List.of());
		}
		return operation;
	}
}
