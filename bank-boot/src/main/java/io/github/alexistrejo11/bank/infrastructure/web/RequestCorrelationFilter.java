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
		String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		String module = resolveModule(pathWithinApplication(request));
		MDC.put("traceId", traceId);
		MDC.put("requestId", requestId);
		MDC.put("spanId", spanId);
		MDC.put("module", module);
		response.setHeader("X-Trace-Id", traceId);
		response.setHeader("X-Request-Id", requestId);
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			MDC.remove("traceId");
			MDC.remove("requestId");
			MDC.remove("spanId");
			MDC.remove("module");
		}
	}

	static String resolveModule(String path) {
		if (path == null || path.isBlank()) {
			return "bank";
		}
		if (path.startsWith("/api/v1/")) {
			String rest = path.substring("/api/v1/".length());
			int slash = rest.indexOf('/');
			String segment = slash > 0 ? rest.substring(0, slash) : rest;
			return segment.isBlank() ? "api" : segment;
		}
		if (path.startsWith("/actuator")) {
			return "actuator";
		}
		return "bank";
	}

	private static String pathWithinApplication(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String context = request.getContextPath();
		if (context != null && !context.isEmpty() && uri.startsWith(context)) {
			return uri.substring(context.length());
		}
		return uri != null ? uri : "";
	}
}
