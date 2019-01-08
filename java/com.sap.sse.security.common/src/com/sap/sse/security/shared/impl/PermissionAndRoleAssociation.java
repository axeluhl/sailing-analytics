package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;

public class PermissionAndRoleAssociation {
    public static TypeRelativeObjectIdentifier get(Role role, User userWithRole) {
        String ownerTenantString = "null";
        UserGroup ownerTenant = role.getQualifiedForTenant();
        if (ownerTenant != null) {
            ownerTenantString = UserGroupImpl.getTypeRelativeObjectIdentifierAsString(ownerTenant);
        }
        String ownerUserString = "null";
        User ownerUser = role.getQualifiedForUser();
        if (ownerUser != null) {
            ownerUserString = ownerUser.getIdentifier().getTypeRelativeObjectIdentifier().toString();
        }
        String roleDefinitionString = RoleDefinitionImpl.getTypeRelativeObjectIdentifierAsString(role.getRoleDefinition());
        String userWithRoleString = userWithRole.getIdentifier().getTypeRelativeObjectIdentifier().toString();
        TypeRelativeObjectIdentifier associationTypeRelativeId = new TypeRelativeObjectIdentifier(userWithRoleString, roleDefinitionString,
                ownerUserString, ownerTenantString);
        return associationTypeRelativeId;
    }

    public static TypeRelativeObjectIdentifier get(WildcardPermission permission, User userWithPermission) {
        String permissionDefinitionString = permission.toString();
        TypeRelativeObjectIdentifier userWithRoleString = userWithPermission.getIdentifier()
                .getTypeRelativeObjectIdentifier();
        TypeRelativeObjectIdentifier associationTypeRelativeId = new TypeRelativeObjectIdentifier(
                userWithRoleString.toString(), permissionDefinitionString);
        return associationTypeRelativeId;
    }
}

