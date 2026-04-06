package io.github.alexistrejo11.bank.notifications.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationTemplateKey;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationDispatchMessageJsonTest {

	@Test
	void round_trip_json_matches_command() throws Exception {
		var om = new ObjectMapper();
		UUID uid = UUID.randomUUID();
		var content = new GenericEmailContent("t", "l", List.of("a"), null, null, NotificationTemplateKey.GENERIC_ALERT);
		var msg = new NotificationDispatchMessage(uid, "TransferCompletedEvent", content, Map.of("k", "v"));
		String json = om.writeValueAsString(msg);
		var read = om.readValue(json, NotificationDispatchMessage.class);
		assertThat(read.toCommand().sourceEventType()).isEqualTo("TransferCompletedEvent");
		assertThat(read.toCommand().userId()).isEqualTo(uid);
		assertThat(read.toCommand().content().templateKey()).isEqualTo(NotificationTemplateKey.GENERIC_ALERT);
		assertThat(read.toCommand().metadata()).containsEntry("k", "v");
	}
}
