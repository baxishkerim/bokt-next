package az.bokt.auth.web;

import az.bokt.auth.dto.LoginRequest;
import az.bokt.auth.dto.OtpChallengeResponse;
import az.bokt.auth.dto.RefreshRequest;
import az.bokt.auth.dto.TokenResponse;
import az.bokt.auth.dto.VerifyOtpRequest;
import az.bokt.auth.security.AuthenticatedUser;
import az.bokt.auth.security.CurrentUser;
import az.bokt.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Шаг 1: логин/пароль → OTP по SMS. */
    @PostMapping("/login")
    public OtpChallengeResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.orgLogin(), req.username(), req.password());
    }

    /** Шаг 2: подтверждение OTP → токены. */
    @PostMapping("/verify-otp")
    public TokenResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest req, HttpServletRequest http) {
        return authService.verifyOtp(req.reference(), req.code(), clientIp(http), userAgent(http));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest http) {
        return authService.refresh(req.refreshToken(), clientIp(http), userAgent(http));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout(CurrentUser.requireUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthenticatedUser me() {
        return CurrentUser.require();
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest req) {
        return req.getHeader("User-Agent");
    }
}
