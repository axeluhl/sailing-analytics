package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.WildcardPermission;

public class UserDTO implements IsSerializable {
    private String name;
    private String email;
    private List<AccountDTO> accounts;
    private Set<String> roles;
    private Set<WildcardPermission> permissions;
    private boolean emailValidated;

    UserDTO() {} // for serialization only

    public UserDTO(String name, String email, boolean emailValidated, List<AccountDTO> accounts, Iterable<String> roles, Iterable<String> stringPermissions) {
        this.name = name;
        this.email = email;
        this.emailValidated = emailValidated;
        this.accounts = accounts;
        this.roles = new HashSet<>();
        Util.addAll(roles, this.roles);
        this.permissions = new HashSet<>();
        for (String stringPermission : stringPermissions) {
            this.permissions.add(new WildcardPermission(stringPermission));
        }
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
    
    public Iterable<WildcardPermission> getPermissions() {
        return permissions;
    }
    
    public Iterable<String> getStringPermissions() {
        List<String> result = new ArrayList<>();
        for (WildcardPermission wp : getPermissions()) {
            result.add(wp.toString());
        }
        return result;
    }
    
    public boolean hasPermission(String permission) {
        return hasPermission(new WildcardPermission(permission));
    }
    
    public boolean hasPermission(WildcardPermission permission) {
        for (WildcardPermission p : permissions) {
            if (p.implies(permission)) {
                return true;
            }
        }
        return false;
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
