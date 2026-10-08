package az.bokt.auth.config;

import az.bokt.auth.jwt.JwtProperties;
import az.bokt.auth.security.JwtAuthenticationFilter;
import az.bokt.common.error.ApiError;
import az.bokt.common.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности: stateless, JWT, права через @PreAuthorize.
 * Своя реализация вместо Keycloak — публичные эндпоинты только для входа/refresh.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, ObjectMapper objectMapper) {
        this.jwtFilter = jwtFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/verify-otp",
                                "/api/auth/refresh",
                                "/actuator/health/**",
                                "/actuator/info"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> writeError(res, ErrorCode.UNAUTHENTICATED, req.getRequestURI()))
                        .accessDeniedHandler((req, res, e) -> writeError(res, ErrorCode.ACCESS_DENIED, req.getRequestURI()))
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Делегирующий энкодер: по умолчанию bcrypt, с префиксом алгоритма в хэше
     * ({bcrypt}$2a$...), что позволяет позже без миграции перейти на argon2/scrypt.
     * Замена sha256-из-онлайн-сервиса и md5 из старого BOKT.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private void writeError(jakarta.servlet.http.HttpServletResponse res, ErrorCode code, String path) throws java.io.IOException {
        res.setStatus(code.status().value());
        res.setContentType("application/json;charset=UTF-8");
        ApiError body = ApiError.of(code, code.defaultMessage(), path, org.slf4j.MDC.get("correlationId"));
        objectMapper.writeValue(res.getWriter(), body);
    }
}
