package io.github.alexistrejo11.bank.notifications.domain.port.out;

import java.util.UUID;

/**
 * Outbound SMS (planned: Twilio REST API). No SDK on classpath yet.
 */
public interface SmsSenderPort {

	void send(SmsMessage message);

	record SmsMessage(UUID notificationId, String toE164, String body) {
	}
}
