package io.github.alexistrejo11.bank.iam.infrastructure.security;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Loads an RSA key pair from PEM text (PKCS#8 private key and SPKI public key).
 */
public final class RsaKeyPairLoader {

	private RsaKeyPairLoader() {
	}

	public static KeyPair fromPkcs8Pem(String privateKeyPem, String publicKeyPem) {
		try {
			byte[] privDer = decodePem(privateKeyPem, "PRIVATE KEY");
			byte[] pubDer = decodePem(publicKeyPem, "PUBLIC KEY");
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privDer));
			PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pubDer));
			return new KeyPair(publicKey, privateKey);
		}
		catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Invalid RSA PEM; expected PKCS#8 private key and X.509 public key", e);
		}
	}

	private static byte[] decodePem(String pem, String label) {
		String stripped = pem
				.replace("-----BEGIN " + label + "-----", "")
				.replace("-----END " + label + "-----", "")
				.replaceAll("\\s", "");
		return Base64.getDecoder().decode(stripped);
	}
}
