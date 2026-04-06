package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank.kafka")
public class BankKafkaProperties {

	/**
	 * When true, domain events go to Kafka and {@code ApplicationEvent} listeners for domain events are disabled.
	 */
	private boolean enabled = false;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
