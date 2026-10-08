package az.bokt.api.config;

import az.bokt.auth.domain.User;
import az.bokt.auth.repo.UserRepository;
import az.bokt.common.domain.EntityStatus;
import az.bokt.common.util.HashUtil;
import az.bokt.tenant.domain.Organisation;
import az.bokt.tenant.repo.OrganisationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Первичная инициализация: платформенный тенант + супер-админ.
 * Выполняется один раз (если организаций ещё нет). Пароль супер-админа берётся из
 * конфигурации bokt.bootstrap.super-admin-password; если не задан — генерируется и
 * печатается в лог ОДИН раз (обязательно сменить после первого входа).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String PLATFORM_LOGIN = "platform";
    private static final String SUPER_ADMIN_USERNAME = "superadmin";

    private final OrganisationRepository organisationRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${bokt.bootstrap.super-admin-password:}")
    private String configuredPassword;

    public DataInitializer(OrganisationRepository organisationRepo,
                           UserRepository userRepo,
                           PasswordEncoder passwordEncoder) {
        this.organisationRepo = organisationRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (organisationRepo.findByLogin(PLATFORM_LOGIN).isPresent()) {
            return;
        }

        Organisation platform = new Organisation();
        platform.setName("Platform");
        platform.setLogin(PLATFORM_LOGIN);
        platform.setActive(true);
        platform.setPlatform(true);
        platform = organisationRepo.save(platform);

        String rawPassword = (configuredPassword == null || configuredPassword.isBlank())
                ? HashUtil.randomToken(9)
                : configuredPassword;

        User admin = new User();
        admin.setTenantId(platform.getId());
        admin.setUsername(SUPER_ADMIN_USERNAME);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setStatus(EntityStatus.ACTIVE);
        admin.setSuperAdmin(true);
        admin.setPasswordExpiry(LocalDate.now().plusYears(1));
        userRepo.save(admin);

        if (configuredPassword == null || configuredPassword.isBlank()) {
            log.warn("""
                    =====================================================================
                    Создан супер-админ платформы:
                      orgLogin = {}
                      username = {}
                      password = {}
                    СМЕНИТЕ пароль после первого входа. Значение больше не будет показано.
                    =====================================================================""",
                    PLATFORM_LOGIN, SUPER_ADMIN_USERNAME, rawPassword);
        } else {
            log.info("Супер-админ платформы создан (orgLogin={}, username={})", PLATFORM_LOGIN, SUPER_ADMIN_USERNAME);
        }
    }
}
