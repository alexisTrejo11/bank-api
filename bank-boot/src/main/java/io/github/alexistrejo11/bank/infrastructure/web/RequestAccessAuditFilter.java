package io.github.alexistrejo11.bank.infrastructure.web;

import io.github.alexistrejo11.bank.infrastructure.logging.BankLoggingProperties;
import io.github.alexistrejo11.bank.shared.shared_kernel.auth.IamUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Writes one structured line per HTTP request to the {@code ACCESS} logger (file only, not console).
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 20)
public class RequestAccessAuditFilter extends OncePerRequestFilter {

	private static final Logger ACCESS = LoggerFactory.getLogger("ACCESS");

	private final BankLoggingProperties loggingProperties;

	public RequestAccessAuditFilter(BankLoggingProperties loggingProperties) {
		this.loggingProperties = loggingProperties;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!loggingProperties.access().enabled()) {
			return true;
		}
		String path = pathWithinApplication(request);
		return path.startsWith("/actuator")
				|| path.startsWith("/swagger-ui")
				|| path.startsWith("/api-docs")
				|| path.startsWith("/v3/api-docs");
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		long startedNanos = System.nanoTime();
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			long durationMs = (System.nanoTime() - startedNanos) / 1_000_000L;
			String userId = resolveUserId();
			ACCESS.info(
					"eventCategory=ACCESS method={} path={} status={} durationMs={} userId={} clientIp={} traceId={} requestId={} module={}",
					request.getMethod(),
					pathWithinApplication(request),
					response.getStatus(),
					durationMs,
					userId != null ? userId : "-",
					clientIp(request),
					nullToDash(MDC.get("traceId")),
					nullToDash(MDC.get("requestId")),
					nullToDash(MDC.get("module")));
		}
	}

	private static String pathWithinApplication(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String context = request.getContextPath();
		if (context != null && !context.isEmpty() && uri.startsWith(context)) {
			return uri.substring(context.length());
		}
		return uri != null ? uri : "";
	}

	private static String clientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			int comma = forwarded.indexOf(',');
			return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
		}
		return request.getRemoteAddr() != null ? request.getRemoteAddr() : "-";
	}

	private static String nullToDash(String value) {
		return value != null && !value.isBlank() ? value : "-";
	}

	private static String resolveUserId() {
		String fromMdc = MDC.get("userId");
		if (fromMdc != null && !fromMdc.isBlank()) {
			return fromMdc;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof IamUserPrincipal principal) {
			return principal.userId().value().toString();
		}
		return "-";
	}
}
