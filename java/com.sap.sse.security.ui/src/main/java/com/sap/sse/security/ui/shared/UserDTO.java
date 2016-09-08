package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.shared.WildcardPermission;

public class UserDTO implements IsSerializable {
    private String name;
    private String email;
    private String fullName;
    private String company;
    private String locale;
    private List<AccountDTO> accounts;
    private Set<String> roles;
    private Set<WildcardPermission> permissions;
    private boolean emailValidated;

    UserDTO() {} // for serialization only

    public UserDTO(String name, String email, String fullName, String company, String locale, boolean emailValidated,
            List<AccountDTO> accounts, Iterable<String> roles, Iterable<String> stringPermissions) {
        this.name = name;
        this.email = email;
        this.fullName = fullName;
        this.company = company;
        this.locale = locale;
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

    public String getFullName() {
        return fullName;
    }

    public String getCompany() {
        return company;
    }
    
    public String getLocale() {
        return locale;
    }

    public Iterable<String> getRoles() {
        return roles;
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
        return permissions;
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
    public Iterable<String> getAllPermissions(PermissionsForRoleProvider permissionsForRoleProvider) {
        Set<String> result = new LinkedHashSet<>();
        Util.addAll(getStringPermissions(), result);
        if (permissionsForRoleProvider != null) {
            for (String role : getRoles()) {
                Util.addAll(permissionsForRoleProvider.getPermissions(role), result);
            }
        }
        return result;
    }
    
    public boolean hasPermission(String permission, PermissionsForRoleProvider permissionsForRoleProvider) {
        return hasPermission(new WildcardPermission(permission), permissionsForRoleProvider);
    }
    
    public boolean hasPermission(WildcardPermission permission, PermissionsForRoleProvider permissionsForRoleProvider) {
        for (String stringPermission : getAllPermissions(permissionsForRoleProvider)) {
            if (new WildcardPermission(stringPermission).implies(permission)) {
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
