package az.bokt.tenant.service;

import az.bokt.auth.domain.Permission;
import az.bokt.auth.domain.Role;
import az.bokt.auth.domain.User;
import az.bokt.auth.repo.RoleRepository;
import az.bokt.auth.repo.UserRepository;
import az.bokt.common.domain.EntityStatus;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.common.util.HashUtil;
import az.bokt.notification.service.NotificationService;
import az.bokt.tenant.domain.Branch;
import az.bokt.tenant.domain.CardType;
import az.bokt.tenant.domain.Currency;
import az.bokt.tenant.domain.OrgBin;
import az.bokt.tenant.domain.OrgCard;
import az.bokt.tenant.domain.OrgCurrency;
import az.bokt.tenant.domain.Organisation;
import az.bokt.common.tenant.TenantContext;
import az.bokt.tenant.dto.CreateBranchWithModeratorRequest;
import az.bokt.tenant.dto.RegisterOrganisationRequest;
import az.bokt.tenant.repo.BranchRepository;
import az.bokt.tenant.repo.CurrencyRepository;
import az.bokt.tenant.repo.OrgBinRepository;
import az.bokt.tenant.repo.OrgCardRepository;
import az.bokt.tenant.repo.OrgCurrencyRepository;
import az.bokt.tenant.repo.OrganisationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Регистрация и настройка NBCO супер-админом (cross-tenant), поэтому tenant_id на дочерних
 * сущностях проставляется ЯВНО. Заводит организацию, валюту AZN, две карты (материнскую и
 * выдачи), BIN-ы, головной филиал, 5 ролей и первого пользователя-директора (SMS с паролем).
 */
@Service
public class OrganisationService {

    private static final String AZN_CODE = "944";

    private final OrganisationRepository organisationRepo;
    private final CurrencyRepository currencyRepo;
    private final OrgCardRepository orgCardRepo;
    private final OrgCurrencyRepository orgCurrencyRepo;
    private final OrgBinRepository orgBinRepo;
    private final BranchRepository branchRepo;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public OrganisationService(OrganisationRepository organisationRepo,
                               CurrencyRepository currencyRepo,
                               OrgCardRepository orgCardRepo,
                               OrgCurrencyRepository orgCurrencyRepo,
                               OrgBinRepository orgBinRepo,
                               BranchRepository branchRepo,
                               RoleRepository roleRepo,
                               UserRepository userRepo,
                               PasswordEncoder passwordEncoder,
                               NotificationService notificationService) {
        this.organisationRepo = organisationRepo;
        this.currencyRepo = currencyRepo;
        this.orgCardRepo = orgCardRepo;
        this.orgCurrencyRepo = orgCurrencyRepo;
        this.orgBinRepo = orgBinRepo;
        this.branchRepo = branchRepo;
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Transactional
    public Organisation register(RegisterOrganisationRequest req) {
        if (organisationRepo.existsByLogin(req.login())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Организация с таким логином уже существует");
        }

        // 1. Организация (тенант)
        Organisation org = new Organisation();
        org.setName(req.name());
        org.setLogin(req.login());
        org.setFrontId(req.frontId());
        org.setActive(true);
        org = organisationRepo.save(org);
        Long tenantId = org.getId();

        // 2. Валюта организации — AZN
        Currency azn = currencyRepo.findByCode(AZN_CODE)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "Валюта AZN не найдена в справочнике"));
        OrgCurrency oc = new OrgCurrency();
        oc.setTenantId(tenantId);
        oc.setCurrencyId(azn.getId());
        orgCurrencyRepo.save(oc);

        // 3. Материнская карта (источник CHARGE)
        OrgCard mother = new OrgCard();
        mother.setTenantId(tenantId);
        mother.setType(CardType.MOTHER);
        mother.setPan(req.motherCardPan());
        mother.setExpiry(req.motherCardExpiry());
        mother.setMerchant(req.merchant());
        mother.setStatus(EntityStatus.ACTIVE);
        mother.setBalanceMinor(0);
        orgCardRepo.save(mother);

        // 4. Карта выдачи (TOPUP) с начальным лимитом
        OrgCard payout = new OrgCard();
        payout.setTenantId(tenantId);
        payout.setType(CardType.PAYOUT);
        payout.setPan(req.payoutCardPan());
        payout.setExpiry(req.payoutCardExpiry());
        payout.setMerchant(req.merchant());
        payout.setStatus(EntityStatus.ACTIVE);
        payout.setBalanceMinor(toMinor(req.payoutInitialBalance()));
        orgCardRepo.save(payout);

        // 5. BIN-ы
        if (req.bins() != null) {
            for (String bin : req.bins()) {
                OrgBin ob = new OrgBin();
                ob.setTenantId(tenantId);
                ob.setBin(bin);
                orgBinRepo.save(ob);
            }
        }

        // 6. Головной филиал
        Branch head = new Branch();
        head.setTenantId(tenantId);
        head.setName(req.headBranchName() == null ? "Head Office" : req.headBranchName());
        head.setStatus(EntityStatus.ACTIVE);
        branchRepo.save(head);

        // 7. Пять ролей; директор получает роль director
        Role directorRole = seedDefaultRoles(tenantId);

        // 8. Первый пользователь — директор (учётные данные по SMS)
        createDirector(tenantId, directorRole, req);

        return org;
    }

    /** Создаёт роли director/moderator/operator, возвращает роль director. */
    private Role seedDefaultRoles(Long tenantId) {
        Role director = role(tenantId, "director", "Директор: вся организация, создание модераторов, карты, отчёты", Set.of(
                Permission.USER_MANAGE, Permission.CARD_MANAGE, Permission.CLIENT_MANAGE,
                Permission.CREDIT_VIEW, Permission.CREDIT_ARCHIVE_VIEW, Permission.CREDIT_BY_PAN,
                Permission.REVERSAL_EXECUTE, Permission.FILE_IMPORT, Permission.REPORT_VIEW));

        // Модератор = начальник филиала: подтверждает кредиты и создаёт операторов своего филиала
        role(tenantId, "moderator", "Модератор (филиал): подтверждение кредитов, создание операторов", Set.of(
                Permission.USER_MANAGE, Permission.CREDIT_APPROVE, Permission.CREDIT_CANCEL,
                Permission.CREDIT_VIEW, Permission.CLIENT_MANAGE, Permission.CARD_LIST, Permission.REPORT_VIEW));

        role(tenantId, "operator", "Оператор (филиал): клиенты и заявки", Set.of(
                Permission.CREDIT_ADD, Permission.CREDIT_VIEW, Permission.CLIENT_MANAGE, Permission.CARD_LIST));

        return director;
    }

    private Role role(Long tenantId, String name, String description, Set<Permission> perms) {
        Role r = new Role();
        r.setTenantId(tenantId);
        r.setName(name);
        r.setDescription(description);
        r.setPermissions(perms);
        return roleRepo.save(r);
    }

    private void createDirector(Long tenantId, Role directorRole, RegisterOrganisationRequest req) {
        String rawPassword = HashUtil.randomToken(9);
        String otp = HashUtil.numericOtp(6);

        User director = new User();
        director.setTenantId(tenantId);
        director.setUsername(req.directorUsername());
        director.setPasswordHash(passwordEncoder.encode(rawPassword));
        director.setFirstName(req.directorFirstName());
        director.setLastName(req.directorLastName());
        director.setPhone(req.directorPhone());
        director.setStatus(EntityStatus.ACTIVE);
        director.setPasswordExpiry(LocalDate.now().plusMonths(3));
        director.setRoles(Set.of(directorRole));
        userRepo.save(director);

        notificationService.sendCredentials(req.directorPhone(), req.directorUsername(), rawPassword, otp);
    }

    @Transactional
    public void setActive(Long organisationId, boolean active) {
        Organisation org = organisationRepo.findById(organisationId)
                .orElseThrow(() -> BusinessException.notFound("Организация"));
        org.setActive(active);
        organisationRepo.save(org);
    }

    @Transactional(readOnly = true)
    public List<Organisation> list() {
        return organisationRepo.findAll();
    }

    /**
     * Создать филиал вместе с его модератором (начальником филиала) — одной операцией.
     * Филиал без начальника не создаётся. Модератору назначается роль "moderator",
     * его branchId = созданный филиал, учётные данные уходят по SMS.
     * Один модератор на филиал (создаётся ровно один здесь; дальше операторов добавляют отдельно).
     */
    @Transactional
    public BranchWithModerator addBranchWithModerator(CreateBranchWithModeratorRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        if (userRepo.existsByTenantIdAndUsername(tenantId, req.moderatorUsername())) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "Пользователь с таким username уже существует");
        }

        Role moderatorRole = roleRepo.findByTenantIdAndName(tenantId, "moderator")
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT,
                        "Роль moderator не найдена для организации"));

        // 1. филиал
        Branch branch = new Branch();
        branch.setTenantId(tenantId);
        branch.setName(req.branchName());
        branch.setFrontId(req.frontId());
        branch.setStatus(EntityStatus.ACTIVE);
        branch = branchRepo.save(branch);

        // 2. модератор филиала
        String rawPassword = HashUtil.randomToken(9);
        String otp = HashUtil.numericOtp(6);

        User moderator = new User();
        moderator.setTenantId(tenantId);
        moderator.setUsername(req.moderatorUsername());
        moderator.setPasswordHash(passwordEncoder.encode(rawPassword));
        moderator.setFirstName(req.moderatorFirstName());
        moderator.setLastName(req.moderatorLastName());
        moderator.setPhone(req.moderatorPhone());
        moderator.setBranchId(branch.getId());
        moderator.setStatus(EntityStatus.ACTIVE);
        moderator.setPasswordExpiry(LocalDate.now().plusMonths(3));
        moderator.setRoles(Set.of(moderatorRole));
        moderator = userRepo.save(moderator);

        notificationService.sendCredentials(req.moderatorPhone(), req.moderatorUsername(), rawPassword, otp);

        return new BranchWithModerator(branch, moderator.getId(), moderator.getUsername());
    }

    /** Результат создания филиала с модератором (без раскрытия пароля). */
    public record BranchWithModerator(Branch branch, Long moderatorId, String moderatorUsername) {}

    @Transactional(readOnly = true)
    public List<Branch> listBranches() {
        return branchRepo.findAllByOrderByNameAsc();
    }

    private long toMinor(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
