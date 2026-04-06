package io.github.alexistrejo11.bank.iam.infrastructure.config;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(BankHttpProperties.class)
public class BankHttpCorsConfiguration {

	@Bean
	public CorsConfigurationSource bankCorsConfigurationSource(BankHttpProperties properties) {
		List<String> origins = properties.getCors().getAllowedOrigins();
		if (origins == null || origins.isEmpty()) {
			return request -> null;
		}
		var c = new CorsConfiguration();
		c.setAllowedOrigins(origins);
		c.setAllowedMethods(properties.getCors().getAllowedMethods());
		c.setAllowedHeaders(properties.getCors().getAllowedHeaders());
		c.setAllowCredentials(properties.getCors().isAllowCredentials());
		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", c);
		return source;
	}
}
