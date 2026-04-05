package io.github.alexistrejo11.bank.notifications.domain.port.out;

import java.util.UUID;

public interface HtmlEmailSenderPort {

	void send(HtmlEmailMessage message);

	record HtmlEmailMessage(UUID notificationId, String toAddress, String subject, String htmlBody) {
	}
}
