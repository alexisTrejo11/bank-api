package io.github.alexisTrejo11.bank.security.token.jwt;

import io.github.alexistrejo11.bank.shared.shared_kernel.auth.IamUserPrincipal;
import io.github.alexisTrejo11.bank.security.token.ParsedAccessToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenService jwtTokenService;

	public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
		this.jwtTokenService = jwtTokenService;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			try {
				ParsedAccessToken parsed = jwtTokenService.parseAndValidateAccessToken(header.substring(7));
				IamUserPrincipal principal = new IamUserPrincipal(parsed.userId(), parsed.email(), parsed.permissions());
				var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
				MDC.put("userId", parsed.userId().value().toString());
			} catch (Exception ignored) {
				// Invalid token: remain unauthenticated; Spring Security enforces access rules.
			}
		}
		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove("userId");
		}
	}
}
