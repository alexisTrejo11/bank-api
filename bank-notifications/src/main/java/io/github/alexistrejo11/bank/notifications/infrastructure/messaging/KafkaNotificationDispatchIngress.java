package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexistrejo11.bank.notifications.domain.command.DispatchNotificationCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.notifications.dispatch-mode", havingValue = "kafka")
public class KafkaNotificationDispatchIngress implements NotificationDispatchIngress {

	private static final Logger log = LoggerFactory.getLogger(KafkaNotificationDispatchIngress.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String dispatchTopic;

	public KafkaNotificationDispatchIngress(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			@Value("${bank.notifications.kafka.dispatch-topic:bank.notifications.dispatch}") String dispatchTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.dispatchTopic = dispatchTopic;
	}

	@Override
	public void submit(DispatchNotificationCommand command) {
		try {
			String json = objectMapper.writeValueAsString(NotificationDispatchMessage.from(command));
			kafkaTemplate.send(dispatchTopic, json).whenComplete((r, ex) -> {
				if (ex != null) {
					log.warn("notification_dispatch_enqueue_failed topic={} event={}", dispatchTopic, command.sourceEventType(), ex);
				}
				else {
					log.debug("notification_dispatch_enqueued topic={} partition={} offset={}",
							dispatchTopic, r.getRecordMetadata().partition(), r.getRecordMetadata().offset());
				}
			});
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("notification_dispatch_serialize_failed", e);
		}
	}
}
