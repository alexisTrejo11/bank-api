package io.github.alexistrejo11.bank.infrastructure.logging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BankLoggingProperties.class)
public class BankLoggingConfiguration {
}
