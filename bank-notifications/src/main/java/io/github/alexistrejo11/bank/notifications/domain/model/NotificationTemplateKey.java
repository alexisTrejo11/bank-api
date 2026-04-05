package io.github.alexistrejo11.bank.notifications.domain.model;

/**
 * Small set of generic templates; payloads vary by {@code sourceEventType} in persistence.
 */
public enum NotificationTemplateKey {
	GENERIC_MESSAGE,
	GENERIC_ALERT
}
