package io.github.alexistrejo11.bank.notifications.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationChannel;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationStatus;
import io.github.alexistrejo11.bank.notifications.domain.port.out.HtmlEmailSenderPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.HtmlEmailSenderPort.HtmlEmailMessage;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationKafkaPublisherPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.NotificationKafkaPublisherPort.NotificationKafkaEnvelope;
import io.github.alexistrejo11.bank.notifications.domain.port.out.SmsSenderPort;
import io.github.alexistrejo11.bank.notifications.domain.port.out.SmsSenderPort.SmsMessage;
import io.github.alexistrejo11.bank.notifications.infrastructure.email.ThymeleafEmailBodyRenderer;
import io.github.alexistrejo11.bank.notifications.infrastructure.persistence.entity.NotificationEntity;
import io.github.alexistrejo11.bank.notifications.infrastructure.persistence.repository.NotificationJpaRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDispatchService {

	private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

	private static final ObjectMapper METADATA_JSON = new ObjectMapper();

	private final NotificationJpaRepository notificationRepository;
	private final ThymeleafEmailBodyRenderer bodyRenderer;
	private final NotificationKafkaPublisherPort kafkaPublisherPort;
	private final HtmlEmailSenderPort htmlEmailSenderPort;
	private final SmsSenderPort smsSenderPort;
	private final String devRedirectEmail;
	private final String stubSmsE164;

	public NotificationDispatchService(
			NotificationJpaRepository notificationRepository,
			ThymeleafEmailBodyRenderer bodyRenderer,
			NotificationKafkaPublisherPort kafkaPublisherPort,
			HtmlEmailSenderPort htmlEmailSenderPort,
			SmsSenderPort smsSenderPort,
			@Value("${bank.notifications.dev-redirect-email:}") String devRedirectEmail,
			@Value("${bank.notifications.stub-sms-e164:+15555550100}") String stubSmsE164
	) {
		this.notificationRepository = notificationRepository;
		this.bodyRenderer = bodyRenderer;
		this.kafkaPublisherPort = kafkaPublisherPort;
		this.htmlEmailSenderPort = htmlEmailSenderPort;
		this.smsSenderPort = smsSenderPort;
		this.devRedirectEmail = devRedirectEmail == null ? "" : devRedirectEmail.trim();
		this.stubSmsE164 = stubSmsE164 == null || stubSmsE164.isBlank() ? "+15555550100" : stubSmsE164.trim();
	}

	@Transactional
	public void dispatch(UUID userId, String sourceEventType, GenericEmailContent content, Map<String, Object> metadata) {
		UUID id = UUID.randomUUID();
		Instant now = Instant.now();
		String html = bodyRenderer.render(content);
		String metaJson = toJson(metadata);
		String subject = content.title() + " · Bank";
		String recipientHint = devRedirectEmail.isEmpty() ? "unresolved:profile-email" : devRedirectEmail;

		NotificationEntity entity = new NotificationEntity(
				id,
				userId,
				NotificationChannel.EMAIL,
				content.templateKey().name(),
				NotificationStatus.PENDING,
				sourceEventType,
				subject,
				html,
				recipientHint,
				metaJson,
				null,
				now,
				now,
				null
		);
		notificationRepository.save(entity);

		try {
			kafkaPublisherPort.publish(new NotificationKafkaEnvelope(
					id,
					sourceEventType,
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

			entity.setStatus(NotificationStatus.DISPATCHED);
			entity.setDispatchedAt(Instant.now());
		}
		catch (RuntimeException ex) {
			log.warn("notification_dispatch_failed id={} event={}", id, sourceEventType, ex);
			entity.setStatus(NotificationStatus.FAILED);
			entity.setErrorMessage(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
		}
		entity.setUpdatedAt(Instant.now());
		notificationRepository.save(entity);
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
