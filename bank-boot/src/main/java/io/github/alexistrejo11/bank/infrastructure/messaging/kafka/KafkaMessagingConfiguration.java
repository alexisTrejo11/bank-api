package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.shared.event.BankDomainEvent;
import io.github.alexistrejo11.bank.shared.messaging.BankKafkaTopics;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
@ConditionalOnProperty(prefix = "bank.kafka", name = "enabled", havingValue = "true")
public class KafkaMessagingConfiguration {

	@Bean
	public ConsumerFactory<String, BankDomainEvent> bankDomainEventConsumerFactory(
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
			@Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset
	) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		JsonDeserializer<BankDomainEvent> jsonDeserializer = new JsonDeserializer<>(BankDomainEvent.class);
		jsonDeserializer.addTrustedPackages("io.github.alexistrejo11.bank");
		jsonDeserializer.setUseTypeMapperForKey(false);
		ErrorHandlingDeserializer<BankDomainEvent> errorHandlingDeserializer =
				new ErrorHandlingDeserializer<>(jsonDeserializer);
		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, BankDomainEvent> bankDomainEventKafkaListenerContainerFactory(
			ConsumerFactory<String, BankDomainEvent> bankDomainEventConsumerFactory,
			KafkaTemplate<String, ?> kafkaTemplate
	) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate,
				(record, ex) -> new TopicPartition(BankKafkaTopics.DLQ, 0));
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3L));

		ConcurrentKafkaListenerContainerFactory<String, BankDomainEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(bankDomainEventConsumerFactory);
		factory.setCommonErrorHandler(errorHandler);
		return factory;
	}

	@Bean
	public ConsumerFactory<String, String> dlqStringConsumerFactory(
			@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
	) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> dlqStringKafkaListenerContainerFactory(
			ConsumerFactory<String, String> dlqStringConsumerFactory
	) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(dlqStringConsumerFactory);
		return factory;
	}
}
