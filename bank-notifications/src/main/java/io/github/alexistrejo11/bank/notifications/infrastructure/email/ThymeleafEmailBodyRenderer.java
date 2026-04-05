package io.github.alexistrejo11.bank.notifications.infrastructure.email;

import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationTemplateKey;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
public class ThymeleafEmailBodyRenderer {

	private final SpringTemplateEngine templateEngine;

	public ThymeleafEmailBodyRenderer(SpringTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public String render(GenericEmailContent content) {
		Context ctx = new Context();
		ctx.setVariable("title", content.title());
		ctx.setVariable("lead", content.lead());
		ctx.setVariable("detailLines", content.detailLines());
		ctx.setVariable("ctaLabel", content.ctaLabel());
		ctx.setVariable("ctaUrl", content.ctaUrl());
		ctx.setVariable("alert", content.templateKey() == NotificationTemplateKey.GENERIC_ALERT);
		return templateEngine.process("notifications/email/generic", ctx);
	}
}
