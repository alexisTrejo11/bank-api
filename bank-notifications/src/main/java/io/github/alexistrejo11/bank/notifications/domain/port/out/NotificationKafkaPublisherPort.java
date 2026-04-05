package io.github.alexistrejo11.bank.notifications.domain.port.out;

import java.util.UUID;

/**
 * Publishes a durable copy of notification intent for async workers (planned: Kafka producer).
 */
public interface NotificationKafkaPublisherPort {

	void publish(NotificationKafkaEnvelope envelope);

	record NotificationKafkaEnvelope(
			UUID notificationId,
			String sourceEventType,
			String channel,
			String subject,
			String payloadSummary
	) {
	}
}
