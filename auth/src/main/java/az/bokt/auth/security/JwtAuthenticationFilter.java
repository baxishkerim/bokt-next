package az.bokt.auth.security;

import az.bokt.auth.jwt.JwtService;
import az.bokt.common.tenant.TenantContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Читает Bearer-токен, верифицирует его и наполняет:
 * <ul>
 *   <li>SecurityContext — principal {@link AuthenticatedUser} + authorities (для @PreAuthorize);</li>
 *   <li>{@link TenantContext} — tenantId для Hibernate-фильтра тенанта,
 *       либо кросс-тенантный режим для супер-админа.</li>
 * </ul>
 * TenantContext очищается в {@code finally}, чтобы не протёк в переиспользуемый поток.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length());
        try {
            JwtService.ParsedToken parsed = jwtService.parse(token);

            List<SimpleGrantedAuthority> authorities = parsed.authorities().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            AuthenticatedUser principal = new AuthenticatedUser(
                    parsed.userId(), parsed.tenantId(), parsed.username(), parsed.superAdmin());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            if (parsed.superAdmin()) {
                TenantContext.setCrossTenant();
            } else if (parsed.tenantId() != null) {
                TenantContext.setTenantId(parsed.tenantId());
            }

            chain.doFilter(request, response);
        } catch (JwtException ex) {
            // невалидный/истёкший токен — оставляем контекст пустым, дальше решит authorization
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
