package io.github.alexistrejo11.bank.iam.infrastructure.security;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

	private final KeyPair keyPair;
	private final JwtBlocklistStore blocklistStore;

	public JwtTokenService(KeyPair iamJwtKeyPair, JwtBlocklistStore blocklistStore) {
		this.keyPair = iamJwtKeyPair;
		this.blocklistStore = blocklistStore;
	}

	public String createAccessToken(UserId userId, String email, Set<String> roles, Set<String> permissions, Duration ttl) {
		Instant now = Instant.now();
		Instant exp = now.plus(ttl);
		String jti = UUID.randomUUID().toString();
		return Jwts.builder()
				.id(jti)
				.subject(userId.value().toString())
				.claim("email", email)
				.claim("roles", roles.stream().sorted().toList())
				.claim("permissions", permissions.stream().sorted().toList())
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
				.compact();
	}

	public ParsedAccessToken parseAndValidateAccessToken(String token) {
		ParsedAccessToken parsed = parseAccessTokenIgnoringBlocklist(token);
		if (blocklistStore.isBlocked(parsed.jti())) {
			throw new JwtException("Token revoked");
		}
		return parsed;
	}

	public ParsedAccessToken parseAccessTokenIgnoringBlocklist(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(keyPair.getPublic())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		String jti = claims.getId();
		UserId userId = UserId.of(UUID.fromString(claims.getSubject()));
		String email = claims.get("email", String.class);
		@SuppressWarnings("unchecked")
		List<String> rolesList = claims.get("roles", List.class);
		@SuppressWarnings("unchecked")
		List<String> permsList = claims.get("permissions", List.class);
		Set<String> roles = rolesList == null ? Set.of() : new HashSet<>(rolesList);
		Set<String> permissions = permsList == null ? Set.of() : new HashSet<>(permsList);
		Instant expiresAt = claims.getExpiration().toInstant();
		return new ParsedAccessToken(userId, email, jti, roles, permissions, expiresAt);
	}

	public Duration remainingTtl(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(keyPair.getPublic())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		long expMs = claims.getExpiration().getTime();
		return Duration.ofMillis(Math.max(0, expMs - System.currentTimeMillis()));
	}
}
