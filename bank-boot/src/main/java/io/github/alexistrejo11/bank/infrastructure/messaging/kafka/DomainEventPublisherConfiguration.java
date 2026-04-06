package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.messaging.DomainEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class DomainEventPublisherConfiguration {

	@Bean
	@Primary
	@ConditionalOnProperty(prefix = "bank.kafka", name = "enabled", havingValue = "true")
	public DomainEventPublisher kafkaDomainEventPublisher(KafkaTemplate<String, BankDomainEvent> kafkaTemplate) {
		return new KafkaDomainEventPublisher(kafkaTemplate);
	}

	@Bean
	@Primary
	@ConditionalOnProperty(prefix = "bank.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
	public DomainEventPublisher legacyDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		return new LegacyDomainEventPublisher(applicationEventPublisher);
	}
}
