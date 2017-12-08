package com.sap.sse.security.shared;

import java.util.UUID;

public interface RolePermissionModel {
    Iterable<WildcardPermission> getPermissions(UUID roleId);
    Iterable<Role> getRoles();
    
    boolean implies(Role role, WildcardPermission permission);
    
    /**
     * @param role
     *            the role; its name can, e.g., be of the form "role_title:tenant". The tenant is an optional parameter.
     *            It restricts permissions with a * as the instance id to data objects where the tenant parameter equals
     *            the tenant owner of the data object. E.g.: event-admin:tw2016 -> {event:edit:*, regatta:edit:*} This
     *            role would grant the user edit permission for every event and regatta where the tenant owner is
     *            "tw2016".
     * @param permission
     *            E.g. "regatta:edit:tw2016-dyas" (would return true if "tw2016-dyas" would have "tw2016" as the tenant
     *            owner)
     * @param ownership
     *            Ownership of the data object for which the {@code permission} is requested
     */
    boolean implies(Role role, WildcardPermission permission, Ownership ownership);
}
