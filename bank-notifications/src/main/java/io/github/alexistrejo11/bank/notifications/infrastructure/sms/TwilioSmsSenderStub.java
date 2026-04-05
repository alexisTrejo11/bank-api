package io.github.alexistrejo11.bank.notifications.infrastructure.sms;

import io.github.alexistrejo11.bank.notifications.domain.port.out.SmsSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Reserved for Twilio Programmable SMS ({@code com.twilio:twilio}).
 * Replace this class with an implementation that calls {@code Message.creator(...).create()}.
 */
@Component
public class TwilioSmsSenderStub implements SmsSenderPort {

	private static final Logger log = LoggerFactory.getLogger(TwilioSmsSenderStub.class);

	@Override
	public void send(SmsMessage message) {
		log.info(
				"twilio_sms_stub notificationId={} to={} bodyChars={}",
				message.notificationId(),
				mask(message.toE164()),
				message.body() != null ? message.body().length() : 0
		);
	}

	private static String mask(String e164) {
		if (e164 == null || e164.length() < 4) {
			return "****";
		}
		return "…" + e164.substring(e164.length() - 4);
	}
}
