package io.github.alexistrejo11.bank.infrastructure.logging;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Applies optional SQL debug logging levels before Logback initializes.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class BankLoggingProfileActivator implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		if (isTrue(environment.getProperty("bank.logging.levels.sql-debug"))) {
			environment.getPropertySources().addFirst(new MapPropertySource("bankLoggingLevels", Map.of(
					"logging.level.org.hibernate.SQL", "DEBUG",
					"logging.level.org.hibernate.orm.jdbc.bind", "TRACE")));
		}
	}

	private static boolean isTrue(String value) {
		return value != null && (value.equalsIgnoreCase("true") || value.equals("1"));
	}
}
