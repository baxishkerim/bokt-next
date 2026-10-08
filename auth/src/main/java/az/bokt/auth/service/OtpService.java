package az.bokt.auth.service;

import az.bokt.auth.domain.OtpChallenge;
import az.bokt.auth.domain.User;
import az.bokt.auth.repo.OtpChallengeRepository;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.common.util.HashUtil;
import az.bokt.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Логика OTP при входе. В отличие от статичного OTP в BOKT_USERS, код генерируется
 * на каждую попытку, живёт ограниченное время и хранится в виде хэша.
 */
@Service
public class OtpService {

    private final OtpChallengeRepository otpRepo;
    private final NotificationService notificationService;

    @Value("${bokt.otp.length:6}")
    private int otpLength;

    @Value("${bokt.otp.ttl-minutes:5}")
    private int ttlMinutes;

    public OtpService(OtpChallengeRepository otpRepo, NotificationService notificationService) {
        this.otpRepo = otpRepo;
        this.notificationService = notificationService;
    }

    /** Создать challenge, отправить SMS, вернуть reference и TTL. */
    @Transactional
    public Challenge issueForLogin(User user) {
        String code = HashUtil.numericOtp(otpLength);
        String reference = HashUtil.randomToken(24);

        OtpChallenge challenge = new OtpChallenge();
        challenge.setReference(reference);
        challenge.setUserId(user.getId());
        challenge.setCodeHash(HashUtil.sha256Hex(code));
        challenge.setExpiresAt(Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES));
        otpRepo.save(challenge);

        notificationService.sendLoginOtp(user.getPhone(), code, ttlMinutes);

        return new Challenge(reference, ttlMinutes * 60L);
    }

    /** Проверить код. Возвращает userId при успехе, иначе бросает. */
    @Transactional
    public Long verify(String reference, String code) {
        OtpChallenge challenge = otpRepo.findByReference(reference)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        if (!challenge.isUsable()) {
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        challenge.setAttempts(challenge.getAttempts() + 1);

        if (!HashUtil.matches(code, challenge.getCodeHash())) {
            otpRepo.save(challenge);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        challenge.setConsumed(true);
        otpRepo.save(challenge);
        return challenge.getUserId();
    }

    public record Challenge(String reference, long ttlSeconds) {}
}
