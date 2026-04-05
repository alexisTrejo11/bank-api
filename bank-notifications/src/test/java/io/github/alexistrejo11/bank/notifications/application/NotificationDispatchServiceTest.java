package io.github.alexistrejo11.bank.notifications.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationTemplateKey;
import io.github.alexistrejo11.bank.notifications.domain.port.out.HtmlEmailSenderPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationKafkaPublisherPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.SmsSenderPort;
import io.github.alexistrejo11.bank.notifications.infrastructure.email.ThymeleafEmailBodyRenderer;
import io.github.alexistrejo11.bank.notifications.infrastructure.persistence.repository.NotificationJpaRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

	@Mock
	NotificationJpaRepository notificationJpaRepository;

	@Mock
	ThymeleafEmailBodyRenderer bodyRenderer;

	@Mock
	NotificationKafkaPublisherPort kafkaPublisherPort;

	@Mock
	HtmlEmailSenderPort htmlEmailSenderPort;

	@Mock
	SmsSenderPort smsSenderPort;

	@Test
	@DisplayName("dispatch persists twice, publishes kafka stub, skips email when no dev redirect, sends sms stub")
	void dispatch_pipeline() {
		when(bodyRenderer.render(any())).thenReturn("<html/>");
		var content = new GenericEmailContent("T", "L", List.of(), null, null, NotificationTemplateKey.GENERIC_MESSAGE);
		var service = new NotificationDispatchService(
				notificationJpaRepository,
				bodyRenderer,
				kafkaPublisherPort,
				htmlEmailSenderPort,
				smsSenderPort,
				"",
				"+15555550100"
		);

		service.dispatch(null, "TransferCompletedEvent", content, Map.of("k", "v"));

		verify(kafkaPublisherPort).publish(any());
		verify(smsSenderPort).send(any());
		verify(htmlEmailSenderPort, never()).send(any());
		verify(notificationJpaRepository, times(2)).save(any());
	}
}
