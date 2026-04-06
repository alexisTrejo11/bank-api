package io.github.alexistrejo11.bank.infrastructure.env;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads a {@code .env} file (KEY=value) from the process working directory into the Spring environment.
 * Uses {@code addLast} so OS environment variables, JVM system properties, and {@code application*.yml}
 * take precedence over file entries (see Spring Boot externalized configuration order).
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

	static final String PROPERTY_SOURCE_NAME = "dotenvFile";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String customPath = System.getenv("BANK_DOTENV_PATH");
		Dotenv dotenv = customPath != null && !customPath.isBlank()
				? Dotenv.configure().directory(customPath).ignoreIfMissing().load()
				: Dotenv.configure().directory("./").ignoreIfMissing().load();
		Map<String, Object> map = new LinkedHashMap<>();
		dotenv.entries().forEach(e -> map.put(e.getKey(), e.getValue()));
		if (!map.isEmpty()) {
			environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, map));
		}
	}
}
