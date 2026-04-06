package io.github.alexistrejo11.bank.notifications.domain.port.out;

import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;

public interface EmailTemplateRendererPort {

	String renderHtml(GenericEmailContent content);
}
