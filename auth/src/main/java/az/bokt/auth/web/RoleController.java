package az.bokt.auth.web;

import az.bokt.auth.domain.Permission;
import az.bokt.auth.dto.CreateRoleRequest;
import az.bokt.auth.dto.RoleResponse;
import az.bokt.auth.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Управление ролями. Создание/изменение/удаление — только супер-админ (ROLE_MANAGE):
 * набор ролей задаётся при регистрации NBCO, директор роли не создаёт.
 * Чтение списка ролей доступно и тем, кто управляет пользователями (USER_MANAGE),
 * чтобы назначать роли сотрудникам.
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGE','USER_MANAGE')")
    public List<RoleResponse> list() {
        return roleService.list().stream().map(RoleResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest req) {
        return RoleResponse.from(roleService.create(req));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public RoleResponse updatePermissions(@PathVariable Long id, @RequestBody Set<Permission> permissions) {
        return RoleResponse.from(roleService.updatePermissions(id, permissions));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
