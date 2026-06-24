package io.github.alexisTrejo11.bank.security;

import io.github.alexisTrejo11.bank.security.token.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			CorsConfigurationSource bankCorsConfigurationSource) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(c -> c.configurationSource(bankCorsConfigurationSource))
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a
						.requestMatchers(
								"/actuator/**",
								"/api/v1/auth/register",
								"/api/v1/auth/login",
								"/api/v1/auth/refresh",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/api-docs/**",
								"/v3/api-docs/**",
								"/.well-known/jwks.json",
								"/health",
								"/error"
							)
						.permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/accounts")
						.hasAuthority("accounts:write")
						.requestMatchers(HttpMethod.GET, "/api/v1/accounts/*/balance")
						.hasAuthority("accounts:read")
						.requestMatchers(HttpMethod.GET, "/api/v1/accounts/*/ledger")
						.hasAuthority("accounts:read")
						.requestMatchers(HttpMethod.POST, "/api/v1/loans/*/repayments/*/pay")
						.hasAuthority("loans:write")
						.requestMatchers(HttpMethod.POST, "/api/v1/loans/*/approve")
						.hasAuthority("loans:write")
						.requestMatchers(HttpMethod.POST, "/api/v1/loans")
						.hasAuthority("loans:write")
						.requestMatchers(HttpMethod.GET, "/api/v1/loans/*")
						.hasAuthority("loans:read")
						.requestMatchers(HttpMethod.POST, "/api/v1/payments/transfers/*/reverse")
						.hasAuthority("payments:write")
						.requestMatchers(HttpMethod.POST, "/api/v1/payments/transfers")
						.hasAuthority("payments:write")
						.requestMatchers(HttpMethod.GET, "/api/v1/audit/records")
						.hasAuthority("audit:read")
						.requestMatchers(HttpMethod.GET, "/api/v1/notifications/monitoring/records")
						.hasAuthority("notifications:read")
						.requestMatchers(HttpMethod.GET, "/api/v1/notifications/monitoring/summary")
						.hasAuthority("notifications:read")
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
