package com.sap.sse.security.ui.shared;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;

public class UserDTO implements IsSerializable {
    private String name;
    private String email;
    private List<AccountDTO> accounts;
    private Set<String> roles = new HashSet<>();
    private Set<String> permissions = new HashSet<>();
    private boolean emailValidated;

    UserDTO() {} // for serialization only

    public UserDTO(String name, String email, boolean emailValidated, List<AccountDTO> accounts) {
        this.name = name;
        this.email = email;
        this.emailValidated = emailValidated;
        this.accounts = accounts;
    }

    public String getName() {
        return name;
    }

    public Iterable<String> getRoles() {
        return roles;
    }
    
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
    
    public void addRoles(Iterable<String> roles) {
        Util.addAll(roles, this.roles);
    }
    
    public void setRoles(Iterable<String> newRoleList) {
        this.roles.clear();
        Util.addAll(newRoleList, this.roles);
    }
    
    public Iterable<String> getPermissions() {
        return permissions;
    }
    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void addPermissions(Iterable<String> permissions) {
        Util.addAll(permissions, this.permissions);
    }

    public void setPermissions(Iterable<String> newPermissionList) {
        this.permissions.clear();
        Util.addAll(newPermissionList, this.permissions);
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
