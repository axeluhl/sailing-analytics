package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;

public class UserDTO implements IsSerializable {
    private String name;
    private String email;
    private String fullName;
    private String company;
    private String locale;
    private List<AccountDTO> accounts;
    private Set<UUID> roles;
    private RolePermissionModelDTO rolePermissionModel;
    private Set<WildcardPermission> permissions;
    private boolean emailValidated;

    UserDTO() {} // for serialization only

    public UserDTO(String name, String email, String fullName, String company, String locale, boolean emailValidated,
            List<AccountDTO> accounts, Iterable<UUID> roles, RolePermissionModelDTO rolePermissionModelDTO,
            Iterable<String> stringPermissions) {
        this.name = name;
        this.email = email;
        this.fullName = fullName;
        this.company = company;
        this.locale = locale;
        this.emailValidated = emailValidated;
        this.accounts = accounts;
        this.roles = new HashSet<>();
        Util.addAll(roles, this.roles);
        this.rolePermissionModel = rolePermissionModelDTO;
        this.permissions = new HashSet<>();
        for (String permission : stringPermissions) {
            this.permissions.add(new WildcardPermission(permission, true));
        }
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCompany() {
        return company;
    }
    
    public String getLocale() {
        return locale;
    }

    public Iterable<UUID> getRoles() {
        return roles;
    }
    
    public UUID getRoleIdByName(String name) {
        for (Role role : rolePermissionModel.getRoles()) {
            if (name.equals(role.getName())) {
                return (UUID) role.getId();
            }
        }
        return null;
    }
    
    public Iterable<String> getStringRoles() {
        ArrayList<String> result = new ArrayList<>();
        for (UUID id : roles) {
            result.add(rolePermissionModel.getName(id));
        }
        return result;
    }
    
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
    
    /**
     * Returns the "raw" permissions explicitly set for this user. This does not include permissions
     * inferred by any {@link PermissionsForRoleProvider} for the {@link #getRoles() roles} that this
     * user has. Use {@link #getAllPermissions(PermissionsForRoleProvider)} for that.
     */
    public Iterable<WildcardPermission> getPermissions() {
        return this.permissions;
    }
    
    /**
     * Same as {@link #getPermissions()}, but returning the permissions in their string representation,
     * as specified by {@link WildcardPermission#toString()}.
     */
    public Iterable<String> getStringPermissions() {
        List<String> result = new ArrayList<>();
        for (WildcardPermission wp : getPermissions()) {
            result.add(wp.toString());
        }
        return result;
    }
    
    /**
     * Returns all permissions this user has, including those inferred from the user's {@link #getRoles() roles} by the
     * <code>permissionForRoleProvider</code> and including the {@link #getStringPermissions()} that are set explicitly
     * for this user.
     * 
     * @param permissionsForRoleProvider
     *            may be <code>null</code> in which case only the {@link #getStringPermissions() explicit permissions}
     *            set for this user will be returned.
     * @return a set of permissions with no duplicates, all in the format parsable by
     *         {@link WildcardPermission#WildcardPermission(String)}
     */
    public Iterable<WildcardPermission> getAllPermissions(PermissionsForRoleProvider permissionsForRoleProvider) {
        Set<WildcardPermission> result = new LinkedHashSet<>();
        Util.addAll(permissions, result);
        if (rolePermissionModel != null) {
            for (UUID role : getRoles()) {
                Util.addAll(rolePermissionModel.getPermissions(role), result);
            }
        }
        return result;
    }
    
    public boolean hasPermission(String permission) {
        return hasPermission(new WildcardPermission(permission));
    }
    
    public boolean hasPermission(WildcardPermission permission) {
        return hasPermission(permission, null, null);
    }
    
    public boolean hasPermission(String permission, AccessControlListDTO acl, OwnerDTO owner) {
        return hasPermission(new WildcardPermission(permission), acl, owner);
    }
    
    public boolean hasPermission(WildcardPermission permission, AccessControlListDTO acl, OwnerDTO owner) {
        ArrayList<UserGroup> userGroups = new ArrayList<>();
        if (acl != null) {
            userGroups = new ArrayList<>(acl.getUserGroupPermissionMap().keySet());
        }
        return PermissionChecker.isPermitted(permission, name, userGroups, permissions, roles, rolePermissionModel, owner, acl);
    }

    public List<AccountDTO> getAccounts() {
        return accounts;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailValidated() {
        return emailValidated;
    }

}
