package io.github.alexistrejo11.bank.notifications.domain.model;

import java.util.List;

/**
 * View model for Thymeleaf generic templates (title, body lines, optional CTA).
 */
public record GenericEmailContent(
		String title,
		String lead,
		List<String> detailLines,
		String ctaLabel,
		String ctaUrl,
		NotificationTemplateKey templateKey
) {
	public GenericEmailContent {
		detailLines = detailLines == null ? List.of() : List.copyOf(detailLines);
	}
}
