package io.github.alexistrejo11.bank.notifications.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4 does not always expose a Jackson 2 {@link ObjectMapper} bean; notification Kafka
 * adapters serialize JSON explicitly and need one when {@code bank.notifications.kafka.enabled=true}.
 */
@Configuration
public class NotificationsJacksonConfiguration {

	@Bean
	@ConditionalOnMissingBean(ObjectMapper.class)
	ObjectMapper notificationObjectMapper() {
		return new ObjectMapper();
	}
}
