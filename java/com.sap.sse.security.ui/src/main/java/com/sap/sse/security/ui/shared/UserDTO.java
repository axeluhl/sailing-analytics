package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
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
    private List<UserGroup> groups;

    // for GWT serialization only
    @Deprecated
    UserDTO() {
        super();
    }

    /**
     * @param groups may be {@code null} which is equivalent to passing an empty groups collection
     */
    public UserDTO(String name, String email, String fullName, String company, String locale, boolean emailValidated,
            List<AccountDTO> accounts, Iterable<Role> roles, Tenant defaultTenant, Iterable<WildcardPermission> permissions,
            Iterable<UserGroup> groups) {
        super(name, roles, defaultTenant, permissions);
        this.email = email;
        this.fullName = fullName;
        this.company = company;
        this.locale = locale;
        this.emailValidated = emailValidated;
        this.accounts = accounts;
        this.groups = new ArrayList<>();
        Util.addAll(groups, this.groups);
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

    public Iterable<String> getStringRoles() {
        ArrayList<String> result = new ArrayList<>();
        for (Role role : getRoles()) {
            result.add(role.toString());
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
    
    public List<UserGroup> getUserGroups() {
        return groups;
    }
    
    public boolean hasPermission(String permission) {
        return hasPermission(new WildcardPermission(permission));
    }

    public boolean hasPermission(WildcardPermission permission, Ownership ownership, AccessControlList acl) {
        return hasPermission(permission, ownership, getUserGroups(), acl);
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
