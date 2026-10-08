package az.bokt.auth.service;

import az.bokt.auth.domain.Permission;
import az.bokt.auth.domain.Role;
import az.bokt.auth.domain.User;
import az.bokt.auth.dto.CreateUserRequest;
import az.bokt.auth.repo.RoleRepository;
import az.bokt.auth.repo.UserRepository;
import az.bokt.auth.security.CurrentUser;
import az.bokt.common.domain.EntityStatus;
import az.bokt.common.domain.Gender;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.common.tenant.TenantContext;
import az.bokt.common.util.HashUtil;
import az.bokt.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/** Управление пользователями NBCO. Создание — с генерацией пароля и OTP и отправкой по SMS. */
@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepo,
                       RoleRepository roleRepo,
                       PasswordEncoder passwordEncoder,
                       NotificationService notificationService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Transactional
    public User create(CreateUserRequest req) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            // Супер-админ платформы не привязан к организации и не создаёт сотрудников:
            // он заводит только директора через регистрацию NBCO.
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "Создавать сотрудников может только пользователь организации (директор/модератор), а не супер-админ платформы");
        }

        if (userRepo.existsByTenantIdAndUsername(tenantId, req.username())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Пользователь с таким username уже существует");
        }

        User creator = userRepo.findById(CurrentUser.requireUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));

        Set<Role> roles = resolveRoles(req.roleIds());

        // Защита от эскалации: чувствительные (административные) права нельзя выдать,
        // если их нет у самого создающего. Операционные права (работа с кредитами/клиентами)
        // назначаются свободно — иначе директор не смог бы создать модератора, а модератор оператора.
        if (!creator.isSuperAdmin()) {
            Set<Permission> creatorPerms = creator.effectivePermissions();
            Set<Permission> elevated = java.util.EnumSet.of(
                    Permission.TENANT_MANAGE, Permission.ROLE_MANAGE,
                    Permission.USER_MANAGE, Permission.CARD_MANAGE);
            for (Role r : roles) {
                for (Permission p : r.getPermissions()) {
                    if (elevated.contains(p) && !creatorPerms.contains(p)) {
                        throw new BusinessException(ErrorCode.ACCESS_DENIED,
                                "Нельзя выдать административное право, которого нет у вас: " + p.name());
                    }
                }
            }
        }

        // Филиал: если у создающего есть филиал (модератор) — новый пользователь в тот же филиал;
        // если филиала нет (директор) — берётся из запроса (директор выбирает филиал модератору).
        Long branchId = creator.getBranchId() != null ? creator.getBranchId() : req.branchId();

        String rawPassword = HashUtil.randomToken(9);   // временный пароль
        String otp = HashUtil.numericOtp(6);            // OTP для первого входа отправляется вместе

        User user = new User();
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setMiddleName(req.middleName());
        user.setBirthDate(req.birthDate());
        user.setGender(req.gender() == null ? Gender.UNKNOWN : req.gender());
        user.setPhone(req.phone());
        user.setBranchId(branchId);
        user.setStatus(EntityStatus.ACTIVE);
        user.setPasswordExpiry(LocalDate.now().plusMonths(3));
        user.setRoles(roles);

        User saved = userRepo.save(user);

        notificationService.sendCredentials(req.phone(), req.username(), rawPassword, otp);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) {
        // фильтр тенанта наложится автоматически (аспект), поэтому findAll безопасен
        return userRepo.findAll(pageable);
    }

    @Transactional
    public void setStatus(Long userId, EntityStatus status) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Пользователь"));
        user.setStatus(status);
        userRepo.save(user);
    }

    /** Сброс пароля: новый временный пароль отправляется по SMS. */
    @Transactional
    public void resetPassword(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Пользователь"));
        String rawPassword = HashUtil.randomToken(9);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPasswordExpiry(LocalDate.now().plusMonths(3));
        userRepo.save(user);
        notificationService.sendCredentials(user.getPhone(), user.getUsername(), rawPassword, HashUtil.numericOtp(6));
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        Set<Role> roles = new HashSet<>();
        for (Long id : roleIds) {
            roles.add(roleRepo.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "Роль не найдена: " + id)));
        }
        return roles;
    }
}
