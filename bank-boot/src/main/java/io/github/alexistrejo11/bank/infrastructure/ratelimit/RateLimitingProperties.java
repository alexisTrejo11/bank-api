package io.github.alexistrejo11.bank.infrastructure.ratelimit;

import io.github.alexistrejo11.bank.shared.ratelimit.RateLimitProfile;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank.rate-limiting")
public class RateLimitingProperties {

	private boolean enabled = false;

	/**
	 * When true, Redis errors do not block traffic (availability over strict limiting).
	 */
	private boolean failOpen = true;

	private final GlobalBucket global = new GlobalBucket();

	private final Map<RateLimitProfile, TokenBucketSpec> profiles = new EnumMap<>(RateLimitProfile.class);

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isFailOpen() {
		return failOpen;
	}

	public void setFailOpen(boolean failOpen) {
		this.failOpen = failOpen;
	}

	public GlobalBucket getGlobal() {
		return global;
	}

	public Map<RateLimitProfile, TokenBucketSpec> getProfiles() {
		return profiles;
	}

	public TokenBucketSpec specFor(RateLimitProfile profile) {
		TokenBucketSpec fromYaml = profiles.get(profile);
		if (fromYaml != null) {
			return fromYaml;
		}
		return switch (profile) {
			case STANDARD -> new TokenBucketSpec(48, 0.8);
			case STRICT -> new TokenBucketSpec(12, 0.2);
			case SENSITIVE_OPERATIONS -> new TokenBucketSpec(6, 0.1);
		};
	}

	public static class GlobalBucket {

		/** Maximum tokens (burst). */
		private int capacity = 64;

		/** Steady-state refill rate (tokens per second). */
		private double refillPerSecond = 1.0;

		public int getCapacity() {
			return capacity;
		}

		public void setCapacity(int capacity) {
			this.capacity = capacity;
		}

		public double getRefillPerSecond() {
			return refillPerSecond;
		}

		public void setRefillPerSecond(double refillPerSecond) {
			this.refillPerSecond = refillPerSecond;
		}
	}

	public static class TokenBucketSpec {

		private int capacity = 30;
		private double refillPerSecond = 0.5;

		public TokenBucketSpec() {
		}

		public TokenBucketSpec(int capacity, double refillPerSecond) {
			this.capacity = capacity;
			this.refillPerSecond = refillPerSecond;
		}

		public int getCapacity() {
			return capacity;
		}

		public void setCapacity(int capacity) {
			this.capacity = capacity;
		}

		public double getRefillPerSecond() {
			return refillPerSecond;
		}

		public void setRefillPerSecond(double refillPerSecond) {
			this.refillPerSecond = refillPerSecond;
		}
	}
}
