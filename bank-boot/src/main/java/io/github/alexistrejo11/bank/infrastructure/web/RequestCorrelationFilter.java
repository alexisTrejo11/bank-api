package io.github.alexistrejo11.bank.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String traceId = Optional.ofNullable(request.getHeader("X-Trace-Id")).filter(s -> !s.isBlank())
				.orElseGet(() -> UUID.randomUUID().toString());
		String requestId = Optional.ofNullable(request.getHeader("X-Request-Id")).filter(s -> !s.isBlank())
				.orElse(traceId);
		MDC.put("traceId", traceId);
		MDC.put("requestId", requestId);
		MDC.put("module", "bank");
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			MDC.remove("traceId");
			MDC.remove("requestId");
			MDC.remove("module");
		}
	}
}
