package io.github.alexistrejo11.bank.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public final class RateLimitResponseWriter {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private RateLimitResponseWriter() {
	}

	public static void writeTooManyRequests(
			HttpServletResponse response,
			int retryAfterSeconds,
			Long limitOptional,
			Long remainingOptional
	) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setHeader("Retry-After", Integer.toString(retryAfterSeconds));
		if (limitOptional != null) {
			response.setHeader("X-RateLimit-Limit", Long.toString(limitOptional));
		}
		if (remainingOptional != null) {
			response.setHeader("X-RateLimit-Remaining", Long.toString(remainingOptional));
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "about:blank");
		body.put("title", "Too Many Requests");
		body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
		body.put("detail", "Rate limit exceeded. Retry after " + retryAfterSeconds + " seconds.");
		response.getOutputStream().write(MAPPER.writeValueAsBytes(body));
	}
}
