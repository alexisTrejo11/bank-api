package io.github.alexistrejo11.bank.infrastructure.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alexistrejo11.bank.shared.ratelimit.RateLimitProfile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class RateLimitingPropertiesTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withUserConfiguration(TestConfig.class)
			.withPropertyValues(
					"bank.rate-limiting.enabled=true",
					"bank.rate-limiting.global.capacity=100",
					"bank.rate-limiting.global.refill-per-second=2.5",
					"bank.rate-limiting.profiles.strict.capacity=5",
					"bank.rate-limiting.profiles.strict.refill-per-second=0.05"
			);

	@Test
	void binds_global_and_profile_overrides() {
		runner.run(ctx -> {
			RateLimitingProperties p = ctx.getBean(RateLimitingProperties.class);
			assertThat(p.isEnabled()).isTrue();
			assertThat(p.getGlobal().getCapacity()).isEqualTo(100);
			assertThat(p.getGlobal().getRefillPerSecond()).isEqualTo(2.5);
			assertThat(p.specFor(RateLimitProfile.STRICT).getCapacity()).isEqualTo(5);
			assertThat(p.specFor(RateLimitProfile.STRICT).getRefillPerSecond()).isEqualTo(0.05);
			assertThat(p.specFor(RateLimitProfile.STANDARD).getCapacity()).isEqualTo(48);
		});
	}

	@Configuration
	@EnableConfigurationProperties(RateLimitingProperties.class)
	static class TestConfig {
	}
}
