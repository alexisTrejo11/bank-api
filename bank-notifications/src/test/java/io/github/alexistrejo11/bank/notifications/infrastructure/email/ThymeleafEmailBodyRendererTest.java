package io.github.alexistrejo11.bank.notifications.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alexistrejo11.bank.notifications.domain.model.GenericEmailContent;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationTemplateKey;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class ThymeleafEmailBodyRendererTest {

	private ThymeleafEmailBodyRenderer renderer;

	@BeforeEach
	void setUp() {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML");
		resolver.setCheckExistence(true);
		resolver.setCacheable(false);
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);
		renderer = new ThymeleafEmailBodyRenderer(engine);
	}

	@Test
	@DisplayName("renders generic message template with title and details")
	void renders_message() {
		var content = new GenericEmailContent(
				"Hello",
				"Lead line",
				List.of("One", "Two"),
				null,
				null,
				NotificationTemplateKey.GENERIC_MESSAGE
		);
		String html = renderer.render(content);
		assertThat(html).contains("Hello").contains("Lead line").contains("<li>One</li>");
	}

	@Test
	@DisplayName("alert template uses warm header styling")
	void renders_alert() {
		var content = new GenericEmailContent(
				"Alert",
				"Something needs attention",
				List.of(),
				null,
				null,
				NotificationTemplateKey.GENERIC_ALERT
		);
		String html = renderer.render(content);
		assertThat(html).contains("#ea580c").contains("Alert");
	}
}
