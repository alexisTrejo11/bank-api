package io.github.alexistrejo11.bank.iam.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return SecurityWebPaths.shouldSkipJwtAuthentication(pathWithinApplication(request));
	}

	private static String pathWithinApplication(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String context = request.getContextPath();
		if (context != null && !context.isEmpty() && uri.startsWith(context)) {
			return uri.substring(context.length());
		}
		return uri != null ? uri : "";
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			Authentication existing = SecurityContextHolder.getContext().getAuthentication();
			if (existing != null && !(existing instanceof AnonymousAuthenticationToken)) {
				filterChain.doFilter(request, response);
				return;
			}
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String token = header.substring(7);
		try {
			ParsedAccessToken parsed = jwtTokenService.parseAndValidateAccessToken(token);
			IamUserPrincipal principal = new IamUserPrincipal(parsed.userId(), parsed.email(), parsed.permissions());
			var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			MDC.put("userId", parsed.userId().value().toString());
			try {
				filterChain.doFilter(request, response);
			}
			finally {
				MDC.remove("userId");
			}
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
