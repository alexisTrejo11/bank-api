package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationKafkaPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.notifications.kafka.enabled", havingValue = "true")
public class KafkaBrokerNotificationKafkaPublisher implements NotificationKafkaPublisherPort {

	private static final Logger log = LoggerFactory.getLogger(KafkaBrokerNotificationKafkaPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String pipelineTopic;

	public KafkaBrokerNotificationKafkaPublisher(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			@Value("${bank.notifications.kafka.pipeline-topic:bank.notifications.pipeline}") String pipelineTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.pipelineTopic = pipelineTopic;
	}

	@Override
	public void publish(NotificationKafkaEnvelope envelope) {
		try {
			String json = objectMapper.writeValueAsString(envelope);
			kafkaTemplate.send(pipelineTopic, envelope.notificationId().toString(), json)
					.whenComplete((r, ex) -> {
						if (ex != null) {
							log.warn("kafka_pipeline_publish_failed notificationId={}", envelope.notificationId(), ex);
						}
					});
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("kafka_pipeline_serialize_failed", e);
		}
	}
}
