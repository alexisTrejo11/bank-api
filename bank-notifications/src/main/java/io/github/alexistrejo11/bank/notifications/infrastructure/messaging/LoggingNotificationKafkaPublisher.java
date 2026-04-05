package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationKafkaPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Placeholder for a Kafka producer (e.g. {@code spring-kafka} {@code KafkaTemplate}).
 * Wire a real implementation when the broker is available.
 */
@Component
public class LoggingNotificationKafkaPublisher implements NotificationKafkaPublisherPort {

	private static final Logger log = LoggerFactory.getLogger(LoggingNotificationKafkaPublisher.class);

	@Override
	public void publish(NotificationKafkaEnvelope envelope) {
		log.info(
				"kafka_notification_stub notificationId={} sourceEventType={} channel={} subject={} summary={}",
				envelope.notificationId(),
				envelope.sourceEventType(),
				envelope.channel(),
				envelope.subject(),
				envelope.payloadSummary()
		);
	}
}
