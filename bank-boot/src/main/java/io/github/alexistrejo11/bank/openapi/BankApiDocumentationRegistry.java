package io.github.alexistrejo11.bank.openapi;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BankApiDocumentationRegistry {

	private final Map<String, BankApiOperationDoc> docs;

	public BankApiDocumentationRegistry() {
		this.docs = BankApiDocumentationCatalog.all();
	}

	public Optional<BankApiOperationDoc> find(String key) {
		return Optional.ofNullable(docs.get(key));
	}
}
