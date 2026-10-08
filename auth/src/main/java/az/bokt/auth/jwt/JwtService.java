package az.bokt.auth.jwt;

import az.bokt.auth.domain.Permission;
import az.bokt.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Выпуск и разбор access-токенов (JWT, RS256).
 * <p>
 * В токен кладём: subject=userId, tenantId, username, superAdmin и список authorities.
 * Права можно было бы резолвить и на бэкенде, но для method-security удобнее иметь их в токене;
 * короткий TTL (минуты) ограничивает окно, в течение которого отозванное право ещё действует.
 */
@Service
public class JwtService {

    public static final String CLAIM_TENANT = "tenantId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_SUPER_ADMIN = "superAdmin";
    public static final String CLAIM_BRANCH = "branchId";
    public static final String CLAIM_AUTHORITIES = "authorities";

    private final RsaKeyProvider keys;
    private final JwtProperties props;

    public JwtService(RsaKeyProvider keys, JwtProperties props) {
        this.keys = keys;
        this.props = props;
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.accessTokenTtl());
        List<String> authorities = user.effectivePermissions().stream()
                .map(Permission::authority)
                .toList();

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_TENANT, user.getTenantId())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_SUPER_ADMIN, user.isSuperAdmin())
                .claim(CLAIM_BRANCH, user.getBranchId())
                .claim(CLAIM_AUTHORITIES, authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(keys.privateKey(), Jwts.SIG.RS256)
                .compact();
    }

    /** Разбор и верификация токена. Бросает {@link JwtException} при невалидности/истечении. */
    public ParsedToken parse(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(keys.publicKey())
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(token);
        Claims c = jws.getPayload();

        Long tenantId = c.get(CLAIM_TENANT, Number.class) == null
                ? null : c.get(CLAIM_TENANT, Number.class).longValue();
        @SuppressWarnings("unchecked")
        List<String> authorities = c.get(CLAIM_AUTHORITIES, List.class);
        boolean superAdmin = Boolean.TRUE.equals(c.get(CLAIM_SUPER_ADMIN, Boolean.class));
        Long branchId = c.get(CLAIM_BRANCH, Number.class) == null
                ? null : c.get(CLAIM_BRANCH, Number.class).longValue();

        return new ParsedToken(
                Long.valueOf(c.getSubject()),
                tenantId,
                c.get(CLAIM_USERNAME, String.class),
                superAdmin,
                branchId,
                authorities == null ? Set.of() : Set.copyOf(authorities)
        );
    }

    public record ParsedToken(
            Long userId,
            Long tenantId,
            String username,
            boolean superAdmin,
            Long branchId,
            Set<String> authorities
    ) {}
}
