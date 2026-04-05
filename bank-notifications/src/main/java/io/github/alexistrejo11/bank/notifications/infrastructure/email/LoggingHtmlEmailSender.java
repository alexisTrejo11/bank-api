package io.github.alexistrejo11.bank.notifications.infrastructure.email;

import io.github.alexistrejo11.bank.notifications.domain.port.out.HtmlEmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs the rendered HTML until SMTP or a provider (SendGrid, SES) is configured.
 */
@Component
public class LoggingHtmlEmailSender implements HtmlEmailSenderPort {

	private static final Logger log = LoggerFactory.getLogger(LoggingHtmlEmailSender.class);

	@Override
	public void send(HtmlEmailMessage message) {
		log.info(
				"email_delivery_stub notificationId={} to={} subject={} htmlChars={}",
				message.notificationId(),
				message.toAddress(),
				message.subject(),
				message.htmlBody() != null ? message.htmlBody().length() : 0
		);
	}
}
