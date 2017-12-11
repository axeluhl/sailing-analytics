package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecurityUserImpl;

public class UserDTO extends SecurityUserImpl implements IsSerializable {
    private static final long serialVersionUID = -4807678211983511872L;
    
    private String email;
    private String fullName;
    private String company;
    private String locale;
    private List<AccountDTO> accounts;
    private boolean emailValidated;

    // for GWT serialization only
    @Deprecated
    UserDTO() {
        super();
    }

    public UserDTO(String name, String email, String fullName, String company, String locale, boolean emailValidated,
            List<AccountDTO> accounts, Iterable<Role> roles, Tenant defaultTenant, Iterable<WildcardPermission> permissions) {
        super(name, roles, defaultTenant, permissions);
        this.email = email;
        this.fullName = fullName;
        this.company = company;
        this.locale = locale;
        this.emailValidated = emailValidated;
        this.accounts = accounts;
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

    public UUID getRoleIdByName(String name) {
        for (Role role : getRoles()) {
            if (name.equals(role.getName())) {
                return (UUID) role.getId();
            }
        }
        return null;
    }
    
    public Iterable<String> getStringRoles() {
        ArrayList<String> result = new ArrayList<>();
        for (Role role : getRoles()) {
            result.add(role.getName());
        }
        return result;
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
    public Iterable<WildcardPermission> getAllPermissions() {
        Set<WildcardPermission> result = new LinkedHashSet<>();
        Util.addAll(getPermissions(), result);
        for (Role role : getRoles()) {
            Util.addAll(role.getPermissions(), result);
        }
        return result;
    }
    
    public boolean hasPermission(String permission) {
        return hasPermission(new WildcardPermission(permission));
    }
    
    public boolean hasPermission(String permission, AccessControlList acl, Ownership ownership) {
        return hasPermission(new WildcardPermission(permission), acl, ownership);
    }
    
    public boolean hasPermission(WildcardPermission permission, AccessControlList acl, Ownership ownership) {
        ArrayList<UserGroup> groupsTheUserBelongsTo = new ArrayList<>();
        if (acl != null) {
            groupsTheUserBelongsTo = new ArrayList<>(acl.getActionsByUserGroup().keySet());
        }
        return PermissionChecker.isPermitted(permission, this, groupsTheUserBelongsTo, getRoles(), ownership, acl);
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
