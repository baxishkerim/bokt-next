package az.bokt.auth.service;

import az.bokt.auth.domain.Role;
import az.bokt.auth.dto.CreateRoleRequest;
import az.bokt.auth.repo.RoleRepository;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.common.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/** Роли внутри тенанта: каждая NBCO определяет свои роли и набор прав. */
@Service
public class RoleService {

    private final RoleRepository roleRepo;

    public RoleService(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Transactional
    public Role create(CreateRoleRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        if (roleRepo.existsByTenantIdAndName(tenantId, req.name())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Роль с таким именем уже существует");
        }
        Role role = new Role();
        role.setName(req.name());
        role.setDescription(req.description());
        role.setPermissions(new HashSet<>(req.permissions()));
        return roleRepo.save(role);
    }

    @Transactional(readOnly = true)
    public List<Role> list() {
        return roleRepo.findAll();
    }

    @Transactional
    public Role updatePermissions(Long roleId, java.util.Set<az.bokt.auth.domain.Permission> permissions) {
        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> BusinessException.notFound("Роль"));
        role.setPermissions(new HashSet<>(permissions));
        return roleRepo.save(role);
    }

    @Transactional
    public void delete(Long roleId) {
        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> BusinessException.notFound("Роль"));
        roleRepo.delete(role);
    }
}
