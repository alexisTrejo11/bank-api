package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.shared.messaging.BankKafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "bank.kafka", name = "enabled", havingValue = "true")
public class KafkaTopicConfig {

	@Bean
	public NewTopic transfersTopic() {
		return TopicBuilder.name(BankKafkaTopics.TRANSFERS).partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic accountsTopic() {
		return TopicBuilder.name(BankKafkaTopics.ACCOUNTS).partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic loansTopic() {
		return TopicBuilder.name(BankKafkaTopics.LOANS).partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic notificationsTopic() {
		return TopicBuilder.name(BankKafkaTopics.NOTIFICATIONS).partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic auditTopic() {
		return TopicBuilder.name(BankKafkaTopics.AUDIT).partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic dlqTopic() {
		return TopicBuilder.name(BankKafkaTopics.DLQ).partitions(1).replicas(1).build();
	}
}
