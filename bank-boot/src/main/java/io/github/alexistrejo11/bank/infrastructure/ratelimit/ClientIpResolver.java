package io.github.alexistrejo11.bank.infrastructure.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpResolver {

	private static final String X_FORWARDED_FOR = "X-Forwarded-For";

	private ClientIpResolver() {
	}

	public static String resolve(HttpServletRequest request) {
		String forwarded = request.getHeader(X_FORWARDED_FOR);
		if (forwarded != null && !forwarded.isBlank()) {
			int comma = forwarded.indexOf(',');
			String first = comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
			if (!first.isEmpty()) {
				return first;
			}
		}
		String remote = request.getRemoteAddr();
		return remote != null ? remote : "unknown";
	}
}
