package io.github.alexistrejo11.bank.notifications.application.handler.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexistrejo11.bank.notifications.domain.command.DispatchNotificationCommand;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationChannel;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationLogRecord;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationStatus;
import io.github.alexistrejo11.bank.notifications.domain.port.out.EmailTemplateRendererPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.HtmlEmailSenderPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.HtmlEmailSenderPort.HtmlEmailMessage;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationKafkaPublisherPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationKafkaPublisherPort.NotificationKafkaEnvelope;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationLogRepository;
import io.github.alexistrejo11.bank.notifications.domain.port.out.SmsSenderPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.SmsSenderPort.SmsMessage;
import io.github.alexistrejo11.bank.notifications.domain.service.NotificationContentFactory;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DispatchNotificationHandler {

	private static final Logger log = LoggerFactory.getLogger(DispatchNotificationHandler.class);

	private static final ObjectMapper METADATA_JSON = new ObjectMapper();

	private final NotificationLogRepository notificationLogRepository;
	private final EmailTemplateRendererPort emailTemplateRenderer;
	private final NotificationKafkaPublisherPort kafkaPublisherPort;
	private final HtmlEmailSenderPort htmlEmailSenderPort;
	private final SmsSenderPort smsSenderPort;
	private final String devRedirectEmail;
	private final String stubSmsE164;

	public DispatchNotificationHandler(
			NotificationLogRepository notificationLogRepository,
			EmailTemplateRendererPort emailTemplateRenderer,
			NotificationKafkaPublisherPort kafkaPublisherPort,
			HtmlEmailSenderPort htmlEmailSenderPort,
			SmsSenderPort smsSenderPort,
			@Value("${bank.notifications.dev-redirect-email:}") String devRedirectEmail,
			@Value("${bank.notifications.stub-sms-e164:+15555550100}") String stubSmsE164
	) {
		this.notificationLogRepository = notificationLogRepository;
		this.emailTemplateRenderer = emailTemplateRenderer;
		this.kafkaPublisherPort = kafkaPublisherPort;
		this.htmlEmailSenderPort = htmlEmailSenderPort;
		this.smsSenderPort = smsSenderPort;
		this.devRedirectEmail = devRedirectEmail == null ? "" : devRedirectEmail.trim();
		this.stubSmsE164 = stubSmsE164 == null || stubSmsE164.isBlank() ? "+15555550100" : stubSmsE164.trim();
	}

	@Transactional
	public void handle(DispatchNotificationCommand command) {
		UUID id = UUID.randomUUID();
		Instant now = Instant.now();
		var content = command.content();
		String html = emailTemplateRenderer.renderHtml(content);
		String metaJson = toJson(command.metadata());
		String subject = content.title() + " · Bank";
		String recipientHint = devRedirectEmail.isEmpty() ? "unresolved:profile-email" : devRedirectEmail;

		NotificationLogRecord pending = new NotificationLogRecord(
				id,
				command.userId(),
				NotificationChannel.EMAIL,
				content.templateKey().name(),
				NotificationStatus.PENDING,
				command.sourceEventType(),
				subject,
				html,
				recipientHint,
				metaJson,
				null,
				now,
				now,
				null
		);
		NotificationLogRecord entityState = notificationLogRepository.save(pending);

		try {
			kafkaPublisherPort.publish(new NotificationKafkaEnvelope(
					id,
					command.sourceEventType(),
					NotificationChannel.EMAIL.name(),
					subject,
					truncate(html, 240)
			));

			String emailTo = devRedirectEmail.isEmpty() ? null : devRedirectEmail;
			if (emailTo != null) {
				htmlEmailSenderPort.send(new HtmlEmailMessage(id, emailTo, subject, html));
			}
			else {
				log.debug("notification_email_skipped_no_dev_redirect notificationId={}", id);
			}

			String smsBody = NotificationContentFactory.smsSummary(content);
			smsSenderPort.send(new SmsMessage(id, stubSmsE164, smsBody));

			entityState = withDispatchSuccess(entityState);
		}
		catch (RuntimeException ex) {
			log.warn("notification_dispatch_failed id={} event={}", id, command.sourceEventType(), ex);
			entityState = withDispatchFailure(entityState, ex);
		}
		notificationLogRepository.save(entityState);
	}

	private static NotificationLogRecord withDispatchSuccess(NotificationLogRecord e) {
		Instant at = Instant.now();
		return new NotificationLogRecord(
				e.id(), e.userId(), e.channel(), e.templateKey(), NotificationStatus.DISPATCHED,
				e.sourceEventType(), e.subject(), e.bodyHtml(), e.recipientHint(), e.metadataJson(),
				e.errorMessage(), e.createdAt(), at, at
		);
	}

	private static NotificationLogRecord withDispatchFailure(NotificationLogRecord e, RuntimeException ex) {
		Instant at = Instant.now();
		String err = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
		return new NotificationLogRecord(
				e.id(), e.userId(), e.channel(), e.templateKey(), NotificationStatus.FAILED,
				e.sourceEventType(), e.subject(), e.bodyHtml(), e.recipientHint(), e.metadataJson(),
				err, e.createdAt(), at, e.dispatchedAt()
		);
	}

	private String toJson(Map<String, Object> metadata) {
		try {
			return METADATA_JSON.writeValueAsString(metadata);
		}
		catch (JsonProcessingException e) {
			return "{\"error\":\"metadata_serialization_failed\"}";
		}
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return "";
		}
		return s.length() <= max ? s : s.substring(0, max) + "...";
	}
}
