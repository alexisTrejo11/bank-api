package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.shared.shared_kernel.messaging.DomainEventPublisher;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.BankDomainEvent;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class DomainEventPublisherConfiguration {

	@Bean
	@Primary
	public DomainEventPublisher kafkaDomainEventPublisher(
			@Qualifier("bankDomainEventKafkaTemplate") KafkaTemplate<String, BankDomainEvent> kafkaTemplate) {
		return new KafkaDomainEventPublisher(kafkaTemplate);
	}
}
