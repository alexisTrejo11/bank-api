package io.github.alexistrejo11.bank.infrastructure.env;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads a {@code .env} file (KEY=value) from the process working directory (or its parent)
 * Uses {@code addLast} so OS environment variables, JVM system properties, and {@code application*.yml}
 * take precedence over file entries (see Spring Boot externalized configuration order).
 */
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

	static final String PROPERTY_SOURCE_NAME = "dotenvFile";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String customPath = System.getenv("BANK_DOTENV_PATH");
		Dotenv dotenv = customPath != null && !customPath.isBlank()
				? Dotenv.configure().directory(customPath).ignoreIfMissing().load()
				: loadDotenvFromWorkingDirOrParent();
		Map<String, Object> map = new LinkedHashMap<>();
		dotenv.entries().forEach(e -> map.put(e.getKey(), e.getValue()));
		if (!map.isEmpty()) {
			environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, map));
		}
	}

	private static Dotenv loadDotenvFromWorkingDirOrParent() {
		if (Files.isRegularFile(Path.of(".env"))) {
			return Dotenv.configure().directory("./").ignoreIfMissing().load();
		}
		if (Files.isRegularFile(Path.of("../.env"))) {
			return Dotenv.configure().directory("../").ignoreIfMissing().load();
		}
		return Dotenv.configure().ignoreIfMissing().load();
	}
}
