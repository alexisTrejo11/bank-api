package io.github.alexistrejo11.bank.notifications.application.handler.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.notifications.domain.command.DispatchNotificationCommand;
import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationTemplateKey;
import io.github.alexistrejo11.bank.notifications.domain.port.out.EmailTemplateRendererPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.HtmlEmailSenderPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationKafkaPublisherPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationLogRepository;
import io.github.alexistrejo11.bank.notifications.domain.port.out.SmsSenderPort;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DispatchNotificationHandlerTest {

	@Mock
	NotificationLogRepository notificationLogRepository;

	@Mock
	EmailTemplateRendererPort emailTemplateRenderer;

	@Mock
	NotificationKafkaPublisherPort kafkaPublisherPort;

	@Mock
	HtmlEmailSenderPort htmlEmailSenderPort;

	@Mock
	SmsSenderPort smsSenderPort;

	@Test
	@DisplayName("dispatch persists twice, publishes kafka stub, skips email when no dev redirect, sends sms stub")
	void dispatch_pipeline() {
		when(emailTemplateRenderer.renderHtml(any())).thenReturn("<html/>");
		when(notificationLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		var content = new GenericEmailContent("T", "L", List.of(), null, null, NotificationTemplateKey.GENERIC_MESSAGE);
		var handler = new DispatchNotificationHandler(
				notificationLogRepository,
				emailTemplateRenderer,
				kafkaPublisherPort,
				htmlEmailSenderPort,
				smsSenderPort,
				"",
				"+15555550100"
		);

		handler.handle(new DispatchNotificationCommand(null, "TransferCompletedEvent", content, Map.of("k", "v")));

		verify(kafkaPublisherPort).publish(any());
		verify(smsSenderPort).send(any());
		verify(htmlEmailSenderPort, never()).send(any());
		verify(notificationLogRepository, times(2)).save(any());
	}
}
