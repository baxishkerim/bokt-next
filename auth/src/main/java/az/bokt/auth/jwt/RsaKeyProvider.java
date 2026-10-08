package az.bokt.auth.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Поставщик ключей для подписи JWT (RS256).
 * <p>
 * В проде задаём пару ключей PEM-строками в конфиге (bokt.jwt.private-key-pem / public-key-pem).
 * Публичный ключ можно отдавать другим сервисам для верификации токенов без шаринга секрета —
 * это преимущество асимметричной подписи над HMAC.
 * Если ключи не заданы — генерируем временную пару (только для локальной разработки).
 */
@Component
public class RsaKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyProvider.class);

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaKeyProvider(JwtProperties props) {
        if (hasText(props.privateKeyPem()) && hasText(props.publicKeyPem())) {
            this.privateKey = parsePrivateKey(props.privateKeyPem());
            this.publicKey = parsePublicKey(props.publicKeyPem());
            log.info("JWT signing keys loaded from configuration");
        } else {
            KeyPair pair = generate();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
            log.warn("JWT signing keys are NOT configured — generated an EPHEMERAL RSA pair. "
                    + "Tokens will be invalidated on restart. Configure bokt.jwt.*-key-pem for production.");
        }
    }

    public PrivateKey privateKey() {
        return privateKey;
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    private KeyPair generate() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot generate RSA key pair", e);
        }
    }

    private PrivateKey parsePrivateKey(String pem) {
        try {
            byte[] der = Base64.getDecoder().decode(stripPem(pem));
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid RSA private key PEM", e);
        }
    }

    private PublicKey parsePublicKey(String pem) {
        try {
            byte[] der = Base64.getDecoder().decode(stripPem(pem));
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid RSA public key PEM", e);
        }
    }

    private String stripPem(String pem) {
        return pem.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
