package io.github.alexistrejo11.bank.iam.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return path.startsWith("/api/v1/auth/register")
				|| path.startsWith("/api/v1/auth/login")
				|| path.startsWith("/api/v1/auth/refresh")
				|| path.startsWith("/actuator")
				|| path.startsWith("/error");
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String token = header.substring(7);
		try {
			ParsedAccessToken parsed = jwtTokenService.parseAndValidateAccessToken(token);
			IamUserPrincipal principal = new IamUserPrincipal(parsed.userId(), parsed.email(), parsed.permissions());
			var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			filterChain.doFilter(request, response);
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
