package io.github.alexistrejo11.bank.infrastructure.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(RateLimitingProperties.class)
public class RateLimitingConfiguration {

	@Bean
	@ConditionalOnProperty(prefix = "bank.rate-limiting", name = "enabled", havingValue = "true")
	@ConditionalOnBean(StringRedisTemplate.class)
	RedisTokenBucketRateLimiter redisTokenBucketRateLimiter(
			StringRedisTemplate stringRedisTemplate,
			RateLimitingProperties properties
	) {
		return new RedisTokenBucketRateLimiter(stringRedisTemplate, properties);
	}

	@Bean
	@ConditionalOnBean(RedisTokenBucketRateLimiter.class)
	FilterRegistrationBean<GlobalRateLimitFilter> globalRateLimitFilterRegistration(
			RedisTokenBucketRateLimiter rateLimiter,
			RateLimitingProperties properties
	) {
		GlobalRateLimitFilter filter = new GlobalRateLimitFilter(rateLimiter, properties);
		FilterRegistrationBean<GlobalRateLimitFilter> bean = new FilterRegistrationBean<>(filter);
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
		return bean;
	}

	@Bean
	@ConditionalOnBean(RedisTokenBucketRateLimiter.class)
	AnnotatedRateLimitInterceptor annotatedRateLimitInterceptor(
			RedisTokenBucketRateLimiter rateLimiter,
			RateLimitingProperties properties
	) {
		return new AnnotatedRateLimitInterceptor(rateLimiter, properties);
	}

	@Bean
	@ConditionalOnBean(AnnotatedRateLimitInterceptor.class)
	WebMvcConfigurer rateLimitWebMvcConfigurer(AnnotatedRateLimitInterceptor interceptor) {
		return new WebMvcConfigurer() {
			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(interceptor).addPathPatterns("/api/**");
			}
		};
	}
}
