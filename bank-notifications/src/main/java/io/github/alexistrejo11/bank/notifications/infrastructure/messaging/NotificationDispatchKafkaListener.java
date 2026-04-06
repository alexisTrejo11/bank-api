package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexistrejo11.bank.notifications.application.handler.command.DispatchNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.notifications.dispatch-mode", havingValue = "kafka")
public class NotificationDispatchKafkaListener {

	private static final Logger log = LoggerFactory.getLogger(NotificationDispatchKafkaListener.class);

	private final DispatchNotificationHandler dispatchNotificationHandler;
	private final ObjectMapper objectMapper;

	public NotificationDispatchKafkaListener(
			DispatchNotificationHandler dispatchNotificationHandler,
			ObjectMapper objectMapper
	) {
		this.dispatchNotificationHandler = dispatchNotificationHandler;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(
			topics = "${bank.notifications.kafka.dispatch-topic:bank.notifications.dispatch}",
			groupId = "${spring.kafka.consumer.group-id:bank-notifications}"
	)
	public void onDispatch(String payload,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
			@Header(KafkaHeaders.OFFSET) long offset) {
		try {
			NotificationDispatchMessage msg = objectMapper.readValue(payload, NotificationDispatchMessage.class);
			log.info(
					"kafka_notification_consumed sourceEventType={} partition={} offset={}",
					msg.sourceEventType(),
					partition,
					offset
			);
			dispatchNotificationHandler.handle(msg.toCommand());
		}
		catch (Exception e) {
			log.error("notification_dispatch_consume_failed partition={} offset={}", partition, offset, e);
			throw new RuntimeException(e);
		}
	}
}
