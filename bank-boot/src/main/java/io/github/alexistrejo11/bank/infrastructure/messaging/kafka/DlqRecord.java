package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import java.time.Instant;

public record DlqRecord(
		Instant receivedAt,
		String originalTopic,
		String consumerGroup,
		String errorMessage,
		String payloadPreview
) {
}
