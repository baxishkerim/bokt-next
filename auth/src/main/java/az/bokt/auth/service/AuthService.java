package az.bokt.auth.service;

import az.bokt.auth.domain.RefreshToken;
import az.bokt.auth.domain.User;
import az.bokt.auth.dto.OtpChallengeResponse;
import az.bokt.auth.dto.TokenResponse;
import az.bokt.auth.jwt.JwtProperties;
import az.bokt.auth.jwt.JwtService;
import az.bokt.auth.repo.RefreshTokenRepository;
import az.bokt.auth.repo.UserRepository;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.common.tenant.TenantLookup;
import az.bokt.common.util.HashUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Аутентификация без Keycloak: двухшаговый вход (пароль → OTP по SMS), выдача JWT
 * access-токена и непрозрачного refresh-токена с ротацией и отзывом.
 */
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final TenantLookup tenantLookup;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepo,
                       RefreshTokenRepository refreshRepo,
                       TenantLookup tenantLookup,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,
                       JwtService jwtService,
                       JwtProperties jwtProperties) {
        this.userRepo = userRepo;
        this.refreshRepo = refreshRepo;
        this.tenantLookup = tenantLookup;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    /** Шаг 1: проверка логина организации, имени пользователя и пароля → отправка OTP. */
    @Transactional
    public OtpChallengeResponse login(String orgLogin, String username, String rawPassword) {
        TenantLookup.TenantRef tenant = tenantLookup.findByLogin(orgLogin)
                .filter(TenantLookup.TenantRef::active)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS));

        User user = userRepo.findByTenantIdAndUsername(tenant.id(), username)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
        }
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (user.isPasswordExpired()) {
            throw new BusinessException(ErrorCode.PASSWORD_EXPIRED);
        }

        OtpService.Challenge ch = otpService.issueForLogin(user);
        return new OtpChallengeResponse(ch.reference(), ch.ttlSeconds());
    }

    /** Шаг 2: проверка OTP → выпуск пары токенов. */
    @Transactional
    public TokenResponse verifyOtp(String reference, String code, String ip, String userAgent) {
        Long userId = otpService.verify(reference, code);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));
        return issueTokens(user, ip, userAgent);
    }

    /** Обновление токенов по refresh-токену с ротацией (старый отзывается). */
    @Transactional
    public TokenResponse refresh(String rawRefreshToken, String ip, String userAgent) {
        String hash = HashUtil.sha256Hex(rawRefreshToken);
        RefreshToken stored = refreshRepo.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (!stored.isActive()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        stored.setRevoked(true);
        refreshRepo.save(stored);

        User user = userRepo.findById(stored.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        return issueTokens(user, ip, userAgent);
    }

    /** Выход: отзыв всех refresh-токенов пользователя. */
    @Transactional
    public void logout(Long userId) {
        refreshRepo.revokeAllForUser(userId);
    }

    private TokenResponse issueTokens(User user, String ip, String userAgent) {
        String accessToken = jwtService.issueAccessToken(user);

        String rawRefresh = HashUtil.randomToken(48);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(HashUtil.sha256Hex(rawRefresh));
        rt.setExpiresAt(Instant.now().plus(jwtProperties.refreshTokenTtl()));
        rt.setIp(ip);
        rt.setUserAgent(userAgent);
        refreshRepo.save(rt);

        return TokenResponse.bearer(accessToken, rawRefresh, jwtProperties.accessTokenTtl().toSeconds());
    }
}
