package io.github.alexistrejo11.bank.infrastructure.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank.logging")
public record BankLoggingProperties(
		File file,
		Loki loki,
		Logstash logstash,
		Access access,
		Levels levels
) {

	public record File(
			String directory,
			int maxFileSizeMb,
			int maxHistoryDays,
			long totalSizeCapMb
	) {
	}

	public record Loki(String url, boolean enabled) {
	}

	public record Logstash(boolean enabled, String host, int port) {
	}

	public record Access(boolean enabled) {
	}

	public record Levels(boolean sqlDebug) {
	}
}
