package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import io.github.alexistrejo11.bank.shared.messaging.BankKafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Observes DLQ messages: ERROR log + in-memory buffer for {@code GET /admin/dlq}.
 */
@Component
@ConditionalOnProperty(prefix = "bank.kafka", name = "enabled", havingValue = "true")
public class DlqMonitorConsumer {

	private static final Logger log = LoggerFactory.getLogger(DlqMonitorConsumer.class);

	private final DlqRingBuffer ringBuffer;
	private final MeterRegistry meterRegistry;

	public DlqMonitorConsumer(DlqRingBuffer ringBuffer, MeterRegistry meterRegistry) {
		this.ringBuffer = ringBuffer;
		this.meterRegistry = meterRegistry;
	}

	@KafkaListener(
			topics = BankKafkaTopics.DLQ,
			groupId = "dlq-monitor-cg",
			containerFactory = "dlqStringKafkaListenerContainerFactory"
	)
	public void onDlq(ConsumerRecord<String, String> record) {
		String originalTopic = headerUtf8(record, "kafka_dlt-original-topic").orElse("unknown");
		String err = headerUtf8(record, "kafka_dlt-exception-message").orElse("unknown");
		String group = headerUtf8(record, "kafka_dlt-original-consumer-group").orElse("unknown");
		String payload = record.value() != null && record.value().length() > 512
				? record.value().substring(0, 512) + "…"
				: String.valueOf(record.value());

		log.error(
				"dlq_message originalTopic={} consumerGroup={} errorMessage={} payloadPreview={}",
				originalTopic,
				group,
				err,
				payload
		);
		Counter.builder("bank.dlq.messages.total")
				.tag("originalTopic", originalTopic)
				.register(meterRegistry)
				.increment();
		ringBuffer.add(new DlqRecord(Instant.now(), originalTopic, group, err, payload));
	}

	private static java.util.Optional<String> headerUtf8(ConsumerRecord<?, ?> record, String key) {
		Header h = record.headers().lastHeader(key);
		if (h == null || h.value() == null) {
			return java.util.Optional.empty();
		}
		return java.util.Optional.of(new String(h.value(), StandardCharsets.UTF_8));
	}
}
