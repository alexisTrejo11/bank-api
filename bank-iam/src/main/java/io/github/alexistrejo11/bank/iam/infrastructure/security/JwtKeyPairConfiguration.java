package io.github.alexistrejo11.bank.iam.infrastructure.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class JwtKeyPairConfiguration {

	private static final Logger log = LoggerFactory.getLogger(JwtKeyPairConfiguration.class);

	@Bean
	KeyPair iamJwtKeyPair(
			@Value("${bank.security.jwt.private-key-pem:}") String privateKeyPem,
			@Value("${bank.security.jwt.public-key-pem:}") String publicKeyPem
	) {
		if (StringUtils.hasText(privateKeyPem) && StringUtils.hasText(publicKeyPem)) {
			return RsaKeyPairLoader.fromPkcs8Pem(privateKeyPem.trim(), publicKeyPem.trim());
		}
		log.warn(
				"bank_security_jwt_using_ephemeral_rsa — set BANK_SECURITY_JWT_PRIVATE_KEY_PEM and BANK_SECURITY_JWT_PUBLIC_KEY_PEM "
						+ "(PEM strings) for stable issuer keys across restarts and clustered nodes."
		);
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048);
			return gen.generateKeyPair();
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
