package az.bokt.auth.security;

import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Утилита доступа к текущему аутентифицированному пользователю. */
public final class CurrentUser {

    private CurrentUser() {}

    public static Optional<AuthenticatedUser> find() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser u) {
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public static AuthenticatedUser require() {
        return find().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
    }

    public static Long requireUserId() {
        return require().userId();
    }
}
