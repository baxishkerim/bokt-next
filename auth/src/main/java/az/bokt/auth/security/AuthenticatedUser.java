package az.bokt.auth.security;

/** Аутентифицированный пользователь — principal в SecurityContext. */
public record AuthenticatedUser(
        Long userId,
        Long tenantId,
        String username,
        boolean superAdmin
) {}
